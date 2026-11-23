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
import net.minecraft.item.Items;

/** Lightweight warning-only monitor. It never moves items or changes player controls. */
public class SurvivalMonitor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgChecks = settings.createGroup("Checks");

    private final Setting<Boolean> foodCheck = sgChecks.add(new BoolSetting.Builder()
        .name("low-food-warning")
        .description("Warn when the hunger bar reaches the configured threshold.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> foodThreshold = sgChecks.add(new IntSetting.Builder()
        .name("food-threshold")
        .description("Warn at or below this hunger level (0-20).")
        .defaultValue(8).range(1, 20).sliderRange(1, 20).build());

    private final Setting<Boolean> totemCheck = sgChecks.add(new BoolSetting.Builder()
        .name("totem-warning")
        .description("Warn when the number of Totems in the inventory and offhand is low.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> minimumTotems = sgChecks.add(new IntSetting.Builder()
        .name("minimum-totems")
        .description("Warn when fewer than this number of Totems are available.")
        .defaultValue(1).range(0, 16).sliderRange(0, 8).build());

    private final Setting<Boolean> elytraCheck = sgChecks.add(new BoolSetting.Builder()
        .name("elytra-durability-warning")
        .description("Warn when an equipped or carried Elytra has low remaining durability.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> elytraDurability = sgChecks.add(new IntSetting.Builder()
        .name("elytra-durability-threshold")
        .description("Warn when remaining Elytra durability is at or below this value.")
        .defaultValue(80).range(1, 432).sliderRange(1, 432).build());

    private final Setting<Integer> alertInterval = sgGeneral.add(new IntSetting.Builder()
        .name("alert-interval-seconds")
        .description("Minimum time between repeated warning messages.")
        .defaultValue(10).range(3, 120).sliderRange(3, 30).build());

    private int cooldownTicks;

    public SurvivalMonitor() {
        super(MeteorUltimate.CATEGORY, "survival-monitor",
            "Gives lightweight local warnings for food, Totems and Elytra durability.");
    }

    @Override
    public void onActivate() {
        cooldownTicks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        StringBuilder alerts = new StringBuilder();

        if (foodCheck.get() && mc.player.getHungerManager().getFoodLevel() <= foodThreshold.get()) {
            alerts.append("Food is low");
        }

        if (totemCheck.get()) {
            int count = countTotems();
            if (count < minimumTotems.get()) appendAlert(alerts, "Totems are low (" + count + ")");
        }

        if (elytraCheck.get()) {
            int remaining = findLowestElytraDurability();
            if (remaining >= 0 && remaining <= elytraDurability.get()) {
                appendAlert(alerts, "Elytra durability is low (" + remaining + ")");
            }
        }

        if (alerts.length() > 0) {
            warning(alerts.toString());
            cooldownTicks = alertInterval.get() * 20;
        }
    }

    private int countTotems() {
        int count = mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) ? 1 : 0;
        // Count the 36 main-inventory slots only; the offhand is counted separately.
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) count += stack.getCount();
        }
        return count;
    }

    private int findLowestElytraDurability() {
        int lowest = Integer.MAX_VALUE;
        ItemStack equipped = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (equipped.isOf(Items.ELYTRA)) lowest = remainingDurability(equipped);

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.ELYTRA)) lowest = Math.min(lowest, remainingDurability(stack));
        }
        return lowest == Integer.MAX_VALUE ? -1 : lowest;
    }

    private int remainingDurability(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamage();
    }

    private void appendAlert(StringBuilder alerts, String message) {
        if (alerts.length() > 0) alerts.append(" | ");
        alerts.append(message);
    }
}
