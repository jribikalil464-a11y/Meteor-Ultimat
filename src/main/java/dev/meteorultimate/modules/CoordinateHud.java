package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

/** Compact coordinates and dimension readout in Meteor's module list. */
public class CoordinateHud extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Boolean> showY = sg.add(new BoolSetting.Builder()
        .name("show-y").description("Include the vertical coordinate.").defaultValue(true).build());
    private final Setting<Boolean> showDimension = sg.add(new BoolSetting.Builder()
        .name("show-dimension").description("Include the current dimension name.").defaultValue(true).build());

    public CoordinateHud() {
        super(MeteorUltimate.CATEGORY, "coordinate-hud", "Shows current coordinates and dimension in the module list.");
    }

    @Override public String getInfoString() {
        if (mc.player == null || mc.world == null) return "No world";
        String coords = showY.get()
            ? String.format("%d %d %d", mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ())
            : String.format("%d %d", mc.player.getBlockX(), mc.player.getBlockZ());
        if (!showDimension.get()) return coords;
        String dimension = mc.world.getRegistryKey().getValue().getPath();
        return coords + " | " + dimension;
    }
}
