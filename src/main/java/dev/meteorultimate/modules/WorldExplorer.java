package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lightweight client-side exploration overlay. It scans only already-loaded chunks.
 * Chunk lookups explicitly use create=false to avoid requesting or generating chunks.
 * Finds are remembered per dimension for the current session so new ones can be announced once.
 */
public class WorldExplorer extends Module {
    private static final int MAX_REMEMBERED_PER_DIMENSION = 5000;
    private static final int MAX_ANNOUNCEMENTS_PER_SCAN = 5;

    private enum Category {
        CHEST("chest"), SHULKER("shulker box"), SPAWNER("spawner");

        final String label;

        Category(String label) {
            this.label = label;
        }
    }

    private record Marker(BlockPos pos, Category category, double distanceSq) {}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargets = settings.createGroup("Targets");
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgPerformance = settings.createGroup("Performance");

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("Maximum distance from the player for markers.")
        .defaultValue(48).range(8, 128).sliderRange(8, 96).build());

    private final Setting<Integer> maxMarkers = sgGeneral.add(new IntSetting.Builder()
        .name("max-markers")
        .description("Maximum number of markers rendered. The nearest ones are kept.")
        .defaultValue(160).range(10, 600).sliderRange(10, 300).build());

    private final Setting<Boolean> announceFinds = sgGeneral.add(new BoolSetting.Builder()
        .name("announce-new-finds")
        .description("Send a chat message with coordinates the first time a spawner or shulker box is found. Chests are never announced.")
        .defaultValue(true).build());

    private final Setting<Integer> scanInterval = sgPerformance.add(new IntSetting.Builder()
        .name("scan-interval-ticks")
        .description("Ticks between scans. Higher values reduce background work.")
        .defaultValue(10).range(5, 100).sliderRange(5, 40).build());

    private final Setting<Boolean> pauseWithScreen = sgPerformance.add(new BoolSetting.Builder()
        .name("pause-while-screen-open")
        .description("Pause scanning while a GUI or chat screen is open.")
        .defaultValue(true).build());

    private final Setting<Boolean> chests = sgTargets.add(new BoolSetting.Builder()
        .name("chests-and-barrels")
        .description("Mark loaded chests, trapped chests and barrels.")
        .defaultValue(true).build());

    private final Setting<Boolean> shulkers = sgTargets.add(new BoolSetting.Builder()
        .name("shulker-boxes")
        .description("Mark loaded shulker boxes.")
        .defaultValue(true).build());

    private final Setting<Boolean> spawners = sgTargets.add(new BoolSetting.Builder()
        .name("spawners")
        .description("Mark loaded mob spawners.")
        .defaultValue(true).build());

    private final Setting<SettingColor> chestColor = sgColors.add(new ColorSetting.Builder()
        .name("chest-color")
        .description("Marker color for chests, trapped chests and barrels.")
        .defaultValue(new SettingColor(255, 170, 0)).build());

    private final Setting<SettingColor> shulkerColor = sgColors.add(new ColorSetting.Builder()
        .name("shulker-color")
        .description("Marker color for shulker boxes.")
        .defaultValue(new SettingColor(255, 0, 255)).build());

    private final Setting<SettingColor> spawnerColor = sgColors.add(new ColorSetting.Builder()
        .name("spawner-color")
        .description("Marker color for mob spawners.")
        .defaultValue(new SettingColor(255, 50, 50)).build());

    private final List<Marker> markers = new ArrayList<>();
    private final List<Marker> candidates = new ArrayList<>();
    private final Map<String, Set<Long>> discovered = new HashMap<>();
    private final int[] foundCounts = new int[Category.values().length];

    private int scanTimer;
    private String infoText = "0";
    private boolean quietStop;

    public WorldExplorer() {
        super(MeteorUltimate.CATEGORY, "world-explorer",
            "Highlights selected storage and spawner blocks in chunks already loaded by the client.");
    }

    @Override
    public void onActivate() {
        clearState();
        scanTimer = 0;
        quietStop = false;
    }

    @Override
    public void onDeactivate() {
        markers.clear();
        candidates.clear();
        infoText = "0";

        int total = 0;
        for (int count : foundCounts) total += count;
        if (!quietStop && mc.player != null && total > 0) {
            sayInfo("Session finds: "
                + foundCounts[Category.CHEST.ordinal()] + " chests, "
                + foundCounts[Category.SHULKER.ordinal()] + " shulker boxes, "
                + foundCounts[Category.SPAWNER.ordinal()] + " spawners.");
        }
        quietStop = false;
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        // Remembered positions belong to one world; never carry them to another server.
        quietStop = true;
        clearState();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) {
            markers.clear();
            infoText = "0";
            return;
        }

        // Rendering only consumes cached positions; scan frequency is configurable.
        if (pauseWithScreen.get() && mc.currentScreen != null) return;
        if (++scanTimer < scanInterval.get()) return;
        scanTimer = 0;

        scan();
    }

    private void scan() {
        candidates.clear();

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();
        int chunkRadius = (radius.get() + 15) / 16;
        int centerChunkX = mc.player.getBlockX() >> 4;
        int centerChunkZ = mc.player.getBlockZ() >> 4;
        double maxDistanceSq = (double) radius.get() * radius.get();

        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                // create=false is essential: never load or request a chunk from this scanner.
                WorldChunk chunk = mc.world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getPos();
                    double dx = pos.getX() + 0.5 - px;
                    double dy = pos.getY() + 0.5 - py;
                    double dz = pos.getZ() + 0.5 - pz;
                    double distanceSq = dx * dx + dy * dy + dz * dz;
                    if (distanceSq > maxDistanceSq) continue;

                    Category category = classify(chunk.getBlockState(pos));
                    if (category != null) candidates.add(new Marker(pos.toImmutable(), category, distanceSq));
                }
            }
        }

        // Keep the nearest markers, not whichever chunks happened to be scanned first.
        candidates.sort(Comparator.comparingDouble(Marker::distanceSq));

        markers.clear();
        int limit = Math.min(candidates.size(), maxMarkers.get());
        for (int i = 0; i < limit; i++) markers.add(candidates.get(i));

        if (markers.isEmpty()) {
            infoText = "0";
        } else {
            infoText = markers.size() + " | " + (int) Math.sqrt(markers.get(0).distanceSq()) + "m";
        }

        registerFinds();
    }

    private Category classify(BlockState state) {
        if (spawners.get() && state.isOf(Blocks.SPAWNER)) return Category.SPAWNER;
        if (shulkers.get() && state.getBlock() instanceof ShulkerBoxBlock) return Category.SHULKER;
        if (chests.get() && (state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST) || state.isOf(Blocks.BARREL))) {
            return Category.CHEST;
        }
        return null;
    }

    private void registerFinds() {
        String dimension = mc.world.getRegistryKey().getValue().toString();
        Set<Long> known = discovered.computeIfAbsent(dimension, key -> new HashSet<>());

        int announced = 0;
        int suppressed = 0;

        for (Marker marker : candidates) {
            if (known.size() >= MAX_REMEMBERED_PER_DIMENSION) break;
            if (!known.add(marker.pos().asLong())) continue;

            foundCounts[marker.category().ordinal()]++;

            // Chests are common, so they are counted but never announced.
            if (!announceFinds.get() || marker.category() == Category.CHEST) continue;

            if (announced >= MAX_ANNOUNCEMENTS_PER_SCAN) {
                suppressed++;
                continue;
            }
            announced++;

            BlockPos pos = marker.pos();
            sayInfo("New " + marker.category().label + " at "
                + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                + ", " + (int) Math.sqrt(marker.distanceSq()) + "m away.");
        }

        if (suppressed > 0) sayInfo(suppressed + " more new finds were not announced.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);
            BlockPos pos = marker.pos();
            SettingColor color = colorFor(marker.category());
            event.renderer.box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                color, color, ShapeMode.Lines, 0);
        }
    }

    private SettingColor colorFor(Category category) {
        return switch (category) {
            case CHEST -> chestColor.get();
            case SHULKER -> shulkerColor.get();
            case SPAWNER -> spawnerColor.get();
        };
    }

    private void clearState() {
        markers.clear();
        candidates.clear();
        discovered.clear();
        Arrays.fill(foundCounts, 0);
        infoText = "0";
    }

    private void sayInfo(String text) {
        // Pass dynamic text as an argument so it can never be read as a format string.
        info("%s", text);
    }

    @Override
    public String getInfoString() {
        return infoText;
    }
}
