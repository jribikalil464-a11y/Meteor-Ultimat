package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;

/** A lightweight manually configured waypoint marker; it does not inspect unloaded chunks. */
public class WaypointBeacon extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> label = sgGeneral.add(new StringSetting.Builder()
        .name("label")
        .description("Short name displayed in the module list.")
        .defaultValue("Waypoint")
        .build());

    private final Setting<Integer> x = sgGeneral.add(new IntSetting.Builder()
        .name("x")
        .description("Waypoint X coordinate.")
        .defaultValue(0).range(-30000000, 30000000).sliderRange(-10000, 10000).build());

    private final Setting<Integer> y = sgGeneral.add(new IntSetting.Builder()
        .name("y")
        .description("Waypoint Y coordinate.")
        .defaultValue(64).range(-2048, 2048).sliderRange(-64, 320).build());

    private final Setting<Integer> z = sgGeneral.add(new IntSetting.Builder()
        .name("z")
        .description("Waypoint Z coordinate.")
        .defaultValue(0).range(-30000000, 30000000).sliderRange(-10000, 10000).build());

    private final Setting<Boolean> renderBeacon = sgGeneral.add(new BoolSetting.Builder()
        .name("render-beacon")
        .description("Render a small outline at the configured waypoint.")
        .defaultValue(true).build());

    public WaypointBeacon() {
        super(MeteorUltimate.CATEGORY, "waypoint-beacon",
            "Displays one manually configured waypoint and its distance.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!renderBeacon.get() || mc.world == null || mc.player == null) return;

        double px = x.get();
        double py = y.get();
        double pz = z.get();
        event.renderer.box(px, py, pz, px + 1, py + 1, pz + 1,
            Color.CYAN, Color.CYAN, meteordevelopment.meteorclient.renderer.ShapeMode.Lines, 0);
    }

    @Override
    public String getInfoString() {
        if (mc.player == null) return label.get();
        double distance = Math.sqrt(mc.player.squaredDistanceTo(x.get() + 0.5, y.get() + 0.5, z.get() + 0.5));
        return label.get() + " " + Math.round(distance) + "m";
    }
}
