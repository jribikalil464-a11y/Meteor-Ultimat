package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/** Warning-only inventory and equipment monitor. It never moves or drops items. */
public class InventoryMonitor extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Boolean> freeSlotsWarning = sg.add(new BoolSetting.Builder()
        .name("low-free-slots-warning").description("Warn when the main inventory has few empty slots.")
        .defaultValue(true).build());
    private final Setting<Integer> freeSlotThreshold = sg.add(new IntSetting.Builder()
        .name("free-slot-threshold").description("Warn at or below this number of empty main-inventory slots.")
        .defaultValue(3).range(0, 36).sliderRange(0, 12).build());
    private final Setting<Boolean> armorWarning = sg.add(new BoolSetting.Builder()
        .name("low-armor-warning").description("Warn when equipped armor has low remaining durability.")
        .defaultValue(true).build());
    private final Setting<Integer> durabilityThreshold = sg.add(new IntSetting.Builder()
        .name("durability-threshold").description("Warn at or below this remaining durability value.")
        .defaultValue(80).range(1, 1000).sliderRange(1, 300).build());
    private final Setting<Integer> alertInterval = sg.add(new IntSetting.Builder()
        .name("alert-interval-seconds").description("Minimum delay between warning messages.")
        .defaultValue(15).range(3, 180).sliderRange(3, 60).build());

    private int cooldown;

    public InventoryMonitor() {
        super(MeteorUltimate.CATEGORY, "inventory-monitor", "Warns about low inventory space and worn armor without changing items.");
    }

    @Override public void onActivate() { cooldown = 0; }

    @EventHandler private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (cooldown > 0) { cooldown--; return; }
        StringBuilder alerts = new StringBuilder();

        if (freeSlotsWarning.get()) {
            int empty = 0;
            for (int i = 0; i < 36; i++) if (mc.player.getInventory().getStack(i).isEmpty()) empty++;
            if (empty <= freeSlotThreshold.get()) append(alerts, "Inventory space is low (" + empty + " empty slots)");
        }

        if (armorWarning.get()) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                ItemStack stack = mc.player.getEquippedStack(slot);
                if (!stack.isEmpty() && stack.isDamageable()) {
                    int remaining = stack.getMaxDamage() - stack.getDamage();
                    if (remaining <= durabilityThreshold.get()) append(alerts, slot.name().toLowerCase() + " durability: " + remaining);
                }
            }
        }

        if (!alerts.isEmpty()) {
            warning(alerts.toString());
            cooldown = alertInterval.get() * 20;
        }
    }

    private static void append(StringBuilder out, String message) {
        if (!out.isEmpty()) out.append(" | ");
        out.append(message);
    }
}
