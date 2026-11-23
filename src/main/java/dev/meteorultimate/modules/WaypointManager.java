package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-waypoint manager. Waypoints are stored in the module's normal Meteor settings,
 * so Meteor's settings persistence handles saving/loading them between sessions.
 * Format: name,x,y,z;name2,x,y,z
 */
public class WaypointManager extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> waypointData = sgGeneral.add(new StringSetting.Builder()
        .name("waypoints")
        .description("Semicolon-separated entries in the form name,x,y,z. Example: Home,120,64,-30;Portal,0,70,0")
        .defaultValue("Home,0,64,0")
        .build());

    private final Setting<Boolean> renderMarkers = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("render-markers")
        .description("Render a small marker at every valid waypoint.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> showDistance = sgGeneral.add(new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
        .name("show-nearest-distance")
        .description("Show the nearest waypoint distance in the module list.")
        .defaultValue(true)
        .build());

    private final List<Waypoint> parsed = new ArrayList<>();
    private String lastInput = "";

    public WaypointManager() {
        super(MeteorUltimate.CATEGORY, "waypoint-manager",
            "Manages multiple named waypoints using Meteor's persistent module settings.");
    }

    private void refreshIfNeeded() {
        String input = waypointData.get();
        if (input.equals(lastInput)) return;
        lastInput = input;
        parsed.clear();
        if (input == null || input.isBlank()) return;

        for (String entry : input.split(";")) {
            String[] fields = entry.trim().split(",", -1);
            if (fields.length != 4) continue;
            try {
                String name = fields[0].trim();
                if (name.isEmpty()) continue;
                int x = Integer.parseInt(fields[1].trim());
                int y = Integer.parseInt(fields[2].trim());
                int z = Integer.parseInt(fields[3].trim());
                parsed.add(new Waypoint(name, new BlockPos(x, y, z)));
            } catch (NumberFormatException ignored) {
                // Ignore malformed entries instead of interrupting the render loop.
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!renderMarkers.get() || mc.world == null || mc.player == null) return;
        refreshIfNeeded();
        for (Waypoint waypoint : parsed) {
            BlockPos pos = waypoint.pos();
            event.renderer.box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                Color.CYAN, Color.CYAN, meteordevelopment.meteorclient.renderer.ShapeMode.Lines, 0);
        }
    }

    @Override
    public String getInfoString() {
        refreshIfNeeded();
        if (mc.player == null || !showDistance.get()) return Integer.toString(parsed.size());
        if (parsed.isEmpty()) return "0 waypoints";

        Waypoint nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Waypoint waypoint : parsed) {
            double distance = mc.player.squaredDistanceTo(waypoint.pos().toCenterPos());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = waypoint;
            }
        }
        return nearest == null ? "0 waypoints" : nearest.name() + " " + Math.round(Math.sqrt(nearestDistance)) + "m";
    }

    private record Waypoint(String name, BlockPos pos) { }
}
