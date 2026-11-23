package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight client-side exploration overlay. It scans only already-loaded chunks.
 * Chunk lookups explicitly use create=false to avoid requesting or generating chunks.
 */
public class WorldExplorer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargets = settings.createGroup("Targets");
    private final SettingGroup sgPerformance = settings.createGroup("Performance");

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("radius")
        .description("Maximum distance from the player for markers.")
        .defaultValue(48).range(8, 128).sliderRange(8, 96).build());

    private final Setting<Integer> maxMarkers = sgGeneral.add(new IntSetting.Builder()
        .name("max-markers")
        .description("Maximum number of markers cached and rendered.")
        .defaultValue(160).range(10, 600).sliderRange(10, 300).build());

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

    private final List<BlockPos> markers = new ArrayList<>();
    private int scanTimer;

    public WorldExplorer() {
        super(MeteorUltimate.CATEGORY, "world-explorer",
            "Highlights selected storage and spawner blocks in chunks already loaded by the client.");
    }

    @Override
    public void onActivate() {
        markers.clear();
        scanTimer = 0;
    }

    @Override
    public void onDeactivate() {
        markers.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) {
            markers.clear();
            return;
        }

        // Rendering only consumes cached positions; scan frequency is configurable.
        if (pauseWithScreen.get() && mc.currentScreen != null) return;
        if (++scanTimer < scanInterval.get()) return;
        scanTimer = 0;
        markers.clear();

        int chunkRadius = (radius.get() + 15) / 16;
        int centerChunkX = mc.player.getBlockX() >> 4;
        int centerChunkZ = mc.player.getBlockZ() >> 4;
        double maxDistanceSq = (double) radius.get() * radius.get();

        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                if (markers.size() >= maxMarkers.get()) return;

                // create=false is essential: never load or request a chunk from this scanner.
                WorldChunk chunk = mc.world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (markers.size() >= maxMarkers.get()) return;

                    BlockPos pos = blockEntity.getPos();
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) > maxDistanceSq) continue;

                    var state = chunk.getBlockState(pos);
                    boolean isChest = state.isOf(Blocks.CHEST)
                        || state.isOf(Blocks.TRAPPED_CHEST)
                        || state.isOf(Blocks.BARREL)
                        ;
                    boolean isShulker = state.getBlock() instanceof ShulkerBoxBlock;
                    boolean isSpawner = state.isOf(Blocks.SPAWNER);

                    if ((chests.get() && isChest) || (shulkers.get() && isShulker) || (spawners.get() && isSpawner)) {
                        markers.add(pos.toImmutable());
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (BlockPos pos : markers) {
            event.renderer.box(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                Color.MAGENTA, Color.MAGENTA, ShapeMode.Lines, 0);
        }
    }

    @Override
    public String getInfoString() {
        return Integer.toString(markers.size());
    }
}
