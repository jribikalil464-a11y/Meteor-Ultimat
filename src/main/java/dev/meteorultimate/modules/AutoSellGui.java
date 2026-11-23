package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class AutoSellGui extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSafety = settings.createGroup("Protection");
    private final SettingGroup sgRepeat = settings.createGroup("Repeat");
    private final SettingGroup sgTracking = settings.createGroup("Tracking");

    private final Setting<String> guiTitle = sgGeneral.add(new StringSetting.Builder()
        .name("gui-title")
        .description("The exact title of the server sell menu.")
        .defaultValue("Sell GUI")
        .build()
    );

    private final Setting<Integer> containerRows = sgGeneral.add(new IntSetting.Builder()
        .name("container-rows")
        .description("Number of rows in the sell menu shown in the supplied screenshot.")
        .defaultValue(6)
        .range(1, 6)
        .sliderRange(1, 6)
        .build()
    );

    private final Setting<List<Item>> allowedItems = sgGeneral.add(new ItemListSetting.Builder()
        .name("items-to-sell")
        .description("Only these item types may be moved into the sell menu. Empty means sell nothing.")
        .defaultValue()
        .build()
    );

    private final Setting<List<Item>> blockedItems = sgSafety.add(new ItemListSetting.Builder()
        .name("never-sell")
        .description("Items in this list are never moved, even if they are also in the sale list.")
        .defaultValue()
        .build()
    );

    private final Setting<Integer> minStackCount = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-stack-count")
        .description("Only sell stacks with at least this many items.")
        .defaultValue(1).range(1, 64).sliderRange(1, 64).build()
    );

    private final Setting<Boolean> protectHotbar = sgSafety.add(new BoolSetting.Builder()
        .name("protect-hotbar")
        .description("Never move items from the player's hotbar.")
        .defaultValue(true).build()
    );

    private final Setting<Boolean> protectCustomItems = sgSafety.add(new BoolSetting.Builder()
        .name("protect-custom-named-items")
        .description("Never move items with a custom display name.")
        .defaultValue(true).build()
    );

    private final Setting<Boolean> protectDamagedItems = sgSafety.add(new BoolSetting.Builder()
        .name("protect-damaged-items")
        .description("Never move damaged items.")
        .defaultValue(true).build()
    );

    private final Setting<Integer> maxSellSlots = sgGeneral.add(new IntSetting.Builder()
        .name("maximum-sell-slots")
        .description("Maximum number of GUI slots to fill in one sale batch.")
        .defaultValue(54).range(1, 54).sliderRange(1, 54).build()
    );

    private final Setting<Integer> clickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("click-delay")
        .description("Ticks to wait between inventory clicks for server synchronization.")
        .defaultValue(4)
        .range(2, 10)
        .sliderRange(2, 10)
        .build()
    );

    private final Setting<Boolean> previewOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("preview-only")
        .description("List eligible stacks and stop without moving or selling anything.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> closeWhenDone = sgGeneral.add(new BoolSetting.Builder()
        .name("close-to-sell")
        .description("Close the recognized Sell GUI after selected items have been placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> repeat = sgRepeat.add(new BoolSetting.Builder()
        .name("repeat")
        .description("Reopen the sell menu periodically. Requires the server's actual open-menu command.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> openCommand = sgRepeat.add(new StringSetting.Builder()
        .name("open-command")
        .description("Server command that opens Sell GUI, without the leading slash. Leave blank until verified.")
        .defaultValue("")
        .build()
    );

    private final Setting<Integer> intervalSeconds = sgRepeat.add(new IntSetting.Builder()
        .name("interval-seconds")
        .description("Delay between completed sell cycles.")
        .defaultValue(60)
        .range(15, 3600)
        .sliderRange(15, 300)
        .build()
    );

    private final Setting<Boolean> showCycleLog = sgTracking.add(new BoolSetting.Builder()
        .name("cycle-log")
        .description("Show a local message when a batch is submitted; this is not server-confirmed earnings.")
        .defaultValue(true).build()
    );

    private final Setting<Boolean> balanceReminder = sgTracking.add(new BoolSetting.Builder()
        .name("balance-reminder")
        .description("Remind you to verify your balance after a sale batch.")
        .defaultValue(false).build()
    );

    private enum Stage { IDLE, PICKED_UP, PLACE_PENDING, CLOSING, WAITING_FOR_REOPEN }

    private Stage stage = Stage.IDLE;
    private int waitTicks;
    private int sourceSlot = -1;
    private int destinationSlot = -1;
    private int expectedSourceCount;
    private int movedSlots;
    private long reopenAtTick;
    private long ticks;

    public AutoSellGui() {
        super(MeteorUltimate.CATEGORY, "auto-sell-gui",
            "Moves only selected items into the recognized Sell GUI, then closes it to submit the sale.");
    }

    @Override
    public void onActivate() {
        resetCycle();
        ticks = 0;
        reopenAtTick = 0;
        if (!allowedItems.get().isEmpty()) {
            for (Item item : allowedItems.get()) {
                if (blockedItems.get().contains(item)) {
                    warning("An item is in both the allowlist and denylist; denylist takes priority.");
                    break;
                }
            }
        }
    }

    @Override
    public void onDeactivate() {
        resetCycle();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ticks++;

        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        Screen screen = mc.currentScreen;
        if (screen == null) {
            handleClosedScreen();
            return;
        }

        if (!isSellScreen(screen, mc.player.currentScreenHandler)) {
            // Never interact with an unrelated menu.
            if (stage != Stage.IDLE && stage != Stage.WAITING_FOR_REOPEN) {
                error("Unexpected screen detected. Auto Sell stopped for safety.");
                toggle();
            }
            return;
        }

        ScreenHandler handler = mc.player.currentScreenHandler;
        int containerSlots = containerRows.get() * 9;

        if (stage == Stage.WAITING_FOR_REOPEN) stage = Stage.IDLE;

        if (waitTicks > 0) {
            waitTicks--;
            return;
        }

        if (stage == Stage.PICKED_UP) {
            if (!handler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(handler.syncId, destinationSlot, 0,
                    SlotActionType.PICKUP, mc.player);
                stage = Stage.PLACE_PENDING;
                waitTicks = clickDelay.get();
            } else {
                error("The selected stack was not picked up. Stopping without closing the menu.");
                toggle();
            }
            return;
        }

        if (stage == Stage.PLACE_PENDING) {
            if (!handler.getCursorStack().isEmpty()) {
                error("The server did not accept the placement. Stopping to avoid losing items.");
                toggle();
                return;
            }

            ItemStack source = handler.getSlot(sourceSlot).getStack();
            if (source.getCount() >= expectedSourceCount) {
                error("Inventory did not update after the click. Stopping for safety.");
                toggle();
                return;
            }

            movedSlots++;
            stage = Stage.IDLE;
            sourceSlot = -1;
            destinationSlot = -1;
            if (movedSlots >= maxSellSlots.get()) finishBatch();
            return;
        }

        if (stage == Stage.CLOSING) return;

        if (allowedItems.get().isEmpty()) {
            info("No sale items selected. Add items to 'Items to sell' first.");
            return;
        }

        if (previewOnly.get()) {
            int candidates = countEligiblePlayerStacks(handler, containerSlots);
            info("Preview only: " + candidates + " eligible stack(s). Nothing was moved or sold.");
            toggle();
            return;
        }

        int src = findEligiblePlayerSlot(handler, containerSlots);
        if (src >= 0) {
            if (movedSlots >= maxSellSlots.get()) {
                finishBatch();
                return;
            }
            ItemStack stack = handler.getSlot(src).getStack();
            int dst = findEmptyContainerSlot(handler, containerSlots, stack);
            if (dst < 0) {
                // Full menu or no compatible destination slot: submit only the existing batch.
                finishBatch();
                return;
            }

            sourceSlot = src;
            destinationSlot = dst;
            expectedSourceCount = stack.getCount();

            mc.interactionManager.clickSlot(handler.syncId, src, 0,
                SlotActionType.PICKUP, mc.player);
            stage = Stage.PICKED_UP;
            waitTicks = clickDelay.get();
            return;
        }

        // No more eligible items remain in the player inventory.
        if (movedSlots > 0) {
            if (closeWhenDone.get()) finishBatch();
            else {
                info("Batch prepared. Auto Sell stopped; review the GUI and submit it manually.");
                toggle();
            }
        } else {
            info("No eligible items found. Sell GUI left open and no items were changed.");
            toggle();
        }
    }

    private boolean isSellScreen(Screen screen, ScreenHandler handler) {
        if (!(handler instanceof GenericContainerScreenHandler generic)) return false;
        if (generic.getRows() != containerRows.get()) return false;
        return screen.getTitle().getString().trim().equalsIgnoreCase(guiTitle.get().trim());
    }

    private int findEligiblePlayerSlot(ScreenHandler handler, int containerSlots) {
        int end = Math.min(containerSlots + 36, handler.slots.size());
        for (int i = containerSlots; i < end; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (protectHotbar.get() && isHotbarSlot(i, containerSlots)) continue;
            if (!stack.isEmpty()
                && allowedItems.get().contains(stack.getItem())
                && !blockedItems.get().contains(stack.getItem())
                && stack.getCount() >= minStackCount.get()
                && !isProtectedStack(stack)) return i;
        }
        return -1;
    }

    private int countEligiblePlayerStacks(ScreenHandler handler, int containerSlots) {
        int count = 0;
        int end = Math.min(containerSlots + 36, handler.slots.size());
        for (int i = containerSlots; i < end; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (protectHotbar.get() && isHotbarSlot(i, containerSlots)) continue;
            if (!stack.isEmpty()
                && allowedItems.get().contains(stack.getItem())
                && !blockedItems.get().contains(stack.getItem())
                && stack.getCount() >= minStackCount.get()
                && !isProtectedStack(stack)) count++;
        }
        return count;
    }

    private boolean isHotbarSlot(int slot, int containerSlots) {
        int hotbarStart = containerSlots + 27;
        return slot >= hotbarStart && slot < hotbarStart + 9;
    }

    private boolean isProtectedStack(ItemStack stack) {
        if (protectCustomItems.get() && stack.hasCustomName()) return true;
        if (protectDamagedItems.get() && stack.isDamageable() && stack.getDamage() > 0) return true;
        return false;
    }

    private int findEmptyContainerSlot(ScreenHandler handler, int containerSlots, ItemStack stack) {
        for (int i = 0; i < containerSlots && i < handler.slots.size(); i++) {
            var slot = handler.getSlot(i);
            // Skip decorative/control slots that reject item insertion.
            if (slot.getStack().isEmpty() && slot.canInsert(stack) && slot.getMaxItemCount(stack) > 0) return i;
        }
        return -1;
    }

    private void finishBatch() {
        if (mc.player == null) return;

        if (!closeWhenDone.get()) {
            // Leave the GUI untouched and hand control back to the player for manual submission.
            info("Auto Sell stopped. Close the Sell GUI manually when you are ready to submit the batch.");
            toggle();
            return;
        }

        stage = Stage.CLOSING;
        // This is the normal handled-screen close action (the same server-side close path as ESC).
        mc.player.closeHandledScreen();
        resetCycle();

        if (repeat.get() && !openCommand.get().isBlank()) {
            reopenAtTick = ticks + intervalSeconds.get() * 20L;
            stage = Stage.WAITING_FOR_REOPEN;
        } else {
            if (showCycleLog.get()) info("Sell batch submitted by closing the Sell GUI. Server success is not verified.");
            if (balanceReminder.get()) info("Check the server balance or sale confirmation to verify the result.");
        }
    }

    private void handleClosedScreen() {
        if (stage != Stage.WAITING_FOR_REOPEN) return;
        if (!repeat.get() || openCommand.get().isBlank()) {
            stage = Stage.IDLE;
            return;
        }

        if (ticks >= reopenAtTick) {
            String command = openCommand.get().trim();
            if (command.startsWith("/")) command = command.substring(1);
            if (!command.isBlank()) {
                mc.player.networkHandler.sendChatCommand(command);
                // Wait for the server to open the GUI; do not issue repeated commands.
                reopenAtTick = Long.MAX_VALUE;
            }
        }
    }

    private void resetCycle() {
        stage = Stage.IDLE;
        waitTicks = 0;
        sourceSlot = -1;
        destinationSlot = -1;
        expectedSourceCount = 0;
        movedSlots = 0;
    }
}
