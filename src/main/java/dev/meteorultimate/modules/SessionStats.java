package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

/** Lightweight local session timer and travelled-distance counter. */
public class SessionStats extends Module {
    private long elapsedTicks;
    private double travelledBlocks;
    private Vec3d previousPosition;

    public SessionStats() {
        super(MeteorUltimate.CATEGORY, "session-stats",
            "Tracks local session duration and approximate distance travelled.");
    }

    @Override
    public void onActivate() {
        elapsedTicks = 0;
        travelledBlocks = 0;
        previousPosition = mc.player == null ? null : mc.player.getPos();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) {
            previousPosition = null;
            return;
        }

        elapsedTicks++;
        Vec3d current = mc.player.getPos();
        if (previousPosition != null) {
            double distance = current.distanceTo(previousPosition);
            // Ignore large discontinuities such as teleports or dimension transitions.
            if (Double.isFinite(distance) && distance <= 8.0) travelledBlocks += distance;
        }
        previousPosition = current;
    }

    @Override
    public String getInfoString() {
        long seconds = elapsedTicks / 20;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainder = seconds % 60;
        return String.format("%02d:%02d:%02d | %.0fm", hours, minutes, remainder, travelledBlocks);
    }
}
