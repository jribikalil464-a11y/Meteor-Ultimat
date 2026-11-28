package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Named waypoints stored in Meteor's persistent module settings.
 * Entry format: name,x,y,z  or  name,x,y,z,dimension  or  name,x,y,z,dimension,category
 * Entries without a dimension apply to every dimension, so old data keeps working.
 */
public class WaypointManager extends Module {
    private static final Color BASE_COLOR = new Color(0, 255, 100, 255);
    private static final Color PORTAL_COLOR = new Color(170, 0, 255, 255);
    private static final Color LOOT_COLOR = new Color(255, 170, 0, 255);
    private static final Color FARM_COLOR = new Color(255, 255, 0, 255);
    private static final Color DANGER_COLOR = new Color(255, 50, 50, 255);
    private static final Color OTHER_COLOR = new Color(0, 200, 255, 255);

    private record Waypoint(String name, int x, int y, int z, String dimension, String category) {}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilter = settings.createGroup("Filter");
    private final SettingGroup sgAdd = settings.createGroup("Add Waypoint");

    private final Setting<String> waypointData = sgGeneral.add(new StringSetting.Builder()
        .name("waypoints")
        .description("Semicolon-separated entries: name,x,y,z or name,x,y,z,dimension,category. "
            + "Dimension: overworld, nether or end (empty = every dimension). "
            + "Categories: base, portal, loot, farm, danger, other. "
            + "Example: Home,120,64,-30,overworld,base;Portal,0,70,0,nether,portal")
        .defaultValue("Home,0,64,0")
        .build()
    );

    private final Setting<Boolean> renderMarkers = sgGeneral.add(new BoolSetting.Builder()
        .name("render-markers")
        .description("Render a small marker at every visible waypoint.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-nearest-distance")
        .description("Show the nearest waypoint distance in the module list.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> currentDimensionOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("current-dimension-only")
        .description("Hide waypoints that belong to a different dimension. Waypoints without a dimension are always shown.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> maxRenderDistance = sgGeneral.add(new IntSetting.Builder()
        .name("max-render-distance")
        .description("Do not render markers farther than this many blocks. 0 means no limit.")
        .defaultValue(0).range(0, 100000).sliderRange(0, 2000)
        .build()
    );

    private final Setting<String> searchFilter = sgFilter.add(new StringSetting.Builder()
        .name("search")
        .description("Only show waypoints whose name contains this text. Leave empty to show all.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> categoryFilter = sgFilter.add(new StringSetting.Builder()
        .name("category-filter")
        .description("Only show waypoints of this category, for example base or portal. Leave empty to show all.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> newName = sgAdd.add(new StringSetting.Builder()
        .name("new-name")
        .description("Name used for the next waypoint saved at your position.")
        .defaultValue("Point")
        .build()
    );

    private final Setting<String> newCategory = sgAdd.add(new StringSetting.Builder()
        .name("new-category")
        .description("Category used for the next waypoint saved at your position.")
        .defaultValue("other")
        .build()
    );

    private final Setting<Boolean> addHere = sgAdd.add(new BoolSetting.Builder()
        .name("add-current-position")
        .description("Turn on to save your current position and dimension as a new waypoint. It turns itself off again.")
        .defaultValue(false)
        .onChanged(value -> {
            if (value) addCurrentPosition();
        })
        .build()
    );

    private final List<Waypoint> parsed = new ArrayList<>();
    private final List<Waypoint> visible = new ArrayList<>();
    private final List<Waypoint> rendered = new ArrayList<>();

    private String lastInput;
    private int invalidCount;
    private String firstInvalid = "";
    private int tickTimer;
    private String infoText = "0 waypoints";

    public WaypointManager() {
        super(MeteorUltimate.CATEGORY, "waypoint-manager",
            "Manages multiple named waypoints with dimensions, categories and filters using Meteor's persistent module settings.");
    }

    @Override
    public void onActivate() {
        lastInput = null;
        tickTimer = 10; // force an update on the first tick
        refreshIfNeeded();
        if (invalidCount > 0) {
            warning("%s", invalidCount + " waypoint entries could not be read and were ignored. First one: " + firstInvalid);
        }
    }

    @Override
    public void onDeactivate() {
        visible.clear();
        rendered.clear();
        infoText = "0 waypoints";
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) {
            visible.clear();
            rendered.clear();
            infoText = "0 waypoints";
            return;
        }

        boolean changed = refreshIfNeeded();
        if (!changed && ++tickTimer < 10) return;
        tickTimer = 0;
        updateVisible();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!renderMarkers.get() || mc.world == null) return;
        for (int i = 0; i < rendered.size(); i++) {
            Waypoint wp = rendered.get(i);
            Color color = colorFor(wp.category());
            event.renderer.box(wp.x(), wp.y(), wp.z(),
                wp.x() + 1, wp.y() + 1, wp.z() + 1,
                color, color, ShapeMode.Lines, 0);
        }
    }

    @Override
    public String getInfoString() {
        return infoText;
    }

    /** Re-parses the waypoint text only when it changed. Returns true if it was re-parsed. */
    private boolean refreshIfNeeded() {
        String input = waypointData.get();
        if (input.equals(lastInput)) return false;

        lastInput = input;
        parsed.clear();
        invalidCount = 0;
        firstInvalid = "";

        if (input.isBlank()) return true;

        for (String raw : input.split(";")) {
            String entry = raw.trim();
            if (entry.isEmpty()) continue;

            Waypoint waypoint = parseEntry(entry);
            if (waypoint == null) {
                invalidCount++;
                if (firstInvalid.isEmpty()) firstInvalid = entry;
            } else {
                parsed.add(waypoint);
            }
        }
        return true;
    }

    private Waypoint parseEntry(String entry) {
        String[] fields = entry.split(",", -1);
        if (fields.length < 4 || fields.length > 6) return null;

        String name = fields[0].trim();
        if (name.isEmpty()) return null;

        try {
            int x = Integer.parseInt(fields[1].trim());
            int y = Integer.parseInt(fields[2].trim());
            int z = Integer.parseInt(fields[3].trim());

            String dimension = fields.length >= 5 ? normalizeDimension(fields[4]) : "";
            if (dimension == null) return null;

            String category = fields.length == 6 ? fields[5].trim().toLowerCase(Locale.ROOT) : "other";
            if (category.isEmpty()) category = "other";

            return new Waypoint(name, x, y, z, dimension, category);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Returns "" for every dimension, the canonical dimension id, or null if the text is not valid. */
    private static String normalizeDimension(String raw) {
        String d = raw.trim().toLowerCase(Locale.ROOT);
        return switch (d) {
            case "", "any", "all" -> "";
            case "overworld", "ow", "o" -> "overworld";
            case "nether", "the_nether", "n" -> "the_nether";
            case "end", "the_end", "e" -> "the_end";
            default -> d.matches("[a-z0-9_./-]+") ? d : null; // custom dimensions
        };
    }

    private void updateVisible() {
        visible.clear();
        rendered.clear();

        String dimension = mc.world.getRegistryKey().getValue().getPath();
        String search = searchFilter.get().trim().toLowerCase(Locale.ROOT);
        String category = categoryFilter.get().trim().toLowerCase(Locale.ROOT);
        double maxDistance = maxRenderDistance.get();
        double maxDistanceSq = maxDistance * maxDistance;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        Waypoint nearest = null;
        double nearestSq = Double.MAX_VALUE;

        for (Waypoint wp : parsed) {
            if (currentDimensionOnly.get() && !wp.dimension().isEmpty() && !wp.dimension().equals(dimension)) continue;
            if (!search.isEmpty() && !wp.name().toLowerCase(Locale.ROOT).contains(search)) continue;
            if (!category.isEmpty() && !wp.category().equals(category)) continue;

            double dx = wp.x() + 0.5 - px;
            double dy = wp.y() + 0.5 - py;
            double dz = wp.z() + 0.5 - pz;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            visible.add(wp);
            if (distanceSq < nearestSq) {
                nearestSq = distanceSq;
                nearest = wp;
            }
            if (maxDistance <= 0 || distanceSq <= maxDistanceSq) rendered.add(wp);
        }

        if (visible.isEmpty()) {
            infoText = "0 waypoints";
        } else if (showDistance.get() && nearest != null) {
            infoText = nearest.name() + " " + Math.round(Math.sqrt(nearestSq)) + "m";
        } else {
            infoText = visible.size() + " waypoints";
        }
    }

    private void addCurrentPosition() {
        if (mc.player == null || mc.world == null) {
            warning("Join a world before saving a waypoint.");
            addHere.set(false);
            return;
        }

        refreshIfNeeded();

        String name = uniqueName(sanitize(newName.get(), "Point", false));
        String category = sanitize(newCategory.get(), "other", true);
        String dimension = mc.world.getRegistryKey().getValue().getPath();

        String entry = name + "," + mc.player.getBlockX() + "," + mc.player.getBlockY() + ","
            + mc.player.getBlockZ() + "," + dimension + "," + category;

        String current = waypointData.get().trim();
        String updated;
        if (current.isEmpty()) updated = entry;
        else if (current.endsWith(";")) updated = current + entry;
        else updated = current + ";" + entry;

        waypointData.set(updated);
        info("%s", "Saved waypoint: " + entry);
        addHere.set(false);
    }

    private String sanitize(String raw, String fallback, boolean lowerCase) {
        String text = raw.replace(',', '_').replace(';', '_').trim();
        if (lowerCase) text = text.toLowerCase(Locale.ROOT);
        return text.isEmpty() ? fallback : text;
    }

    private String uniqueName(String base) {
        String candidate = base;
        int n = 2;
        while (nameExists(candidate)) {
            candidate = base + " " + n;
            n++;
        }
        return candidate;
    }

    private boolean nameExists(String name) {
        for (Waypoint wp : parsed) {
            if (wp.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private static Color colorFor(String category) {
        return switch (category) {
            case "base" -> BASE_COLOR;
            case "portal" -> PORTAL_COLOR;
            case "loot" -> LOOT_COLOR;
            case "farm" -> FARM_COLOR;
            case "danger" -> DANGER_COLOR;
            default -> OTHER_COLOR;
        };
    }
}
