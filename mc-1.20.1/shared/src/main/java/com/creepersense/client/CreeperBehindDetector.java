package com.creepersense.client;

import com.creepersense.Tuning;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/**
 * Client-side only: finds the strongest rear-arc creeper threat for the local player.
 */
public final class CreeperBehindDetector {

    private CreeperBehindDetector() {}

    public static DetectionResult detect(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return new DetectionResult(0f, java.util.List.of());
        }
        Player player = mc.player;
        float radius = effectiveSearchRadius(mc);
        AABB box = player.getBoundingBox().inflate(radius);
        float bestIntensity = 0f;
        ArrayList<DetectedCreeper> creepers = new ArrayList<>();

        for (Creeper creeper : mc.level.getEntitiesOfClass(Creeper.class, box)) {
            if (!creeper.isAlive()) {
                continue;
            }
            Scored s = scoreCreeper(player, creeper, radius);
            if (s.intensity > bestIntensity) {
                bestIntensity = s.intensity;
            }
            if (s.intensity > 0f) {
                creepers.add(new DetectedCreeper(creeper.getId(), s.intensity, s.angleFromBehindRad));
            }
        }
        return new DetectionResult(Math.min(1f, bestIntensity), creepers);
    }

    private static float effectiveSearchRadius(Minecraft mc) {
        float radius = (float) Tuning.SEARCH_RADIUS;
        ClientConfig cfg = ClientConfig.get();
        if (!cfg.difficultyScalingEnabled || mc.level == null) {
            return radius;
        }
        Difficulty d = mc.level.getDifficulty();
        float mult = switch (d) {
            case PEACEFUL -> 0.0f;
            case EASY -> 1.10f;
            case NORMAL -> 1.00f;
            case HARD -> 0.85f;
        };
        return radius * mult;
    }

    private static Scored scoreCreeper(Player player, Creeper creeper, float radius) {
        Vec3 look = player.getLookAngle();
        double lx = look.x;
        double lz = look.z;
        double hLookLen = Math.sqrt(lx * lx + lz * lz);
        if (hLookLen < 1e-6) {
            return Scored.ZERO;
        }
        lx /= hLookLen;
        lz /= hLookLen;

        Vec3 to = creeper.position().subtract(player.position());
        double dy = Math.abs(to.y);
        if (dy >= Tuning.VERTICAL_FADE_END) {
            return Scored.ZERO;
        }
        double tx = to.x;
        double tz = to.z;
        double hDist = Math.sqrt(tx * tx + tz * tz);
        if (hDist < 1e-6 || hDist > radius) {
            return Scored.ZERO;
        }
        tx /= hDist;
        tz /= hDist;

        double dot = tx * lx + tz * lz;
        if (dot > Tuning.BEHIND_DOT_THRESHOLD) {
            return Scored.ZERO;
        }

        double span = radius - Tuning.CLOSE_DISTANCE;
        double distFactor = span > 1e-6
                ? 1.0 - Math.min(1.0, Math.max(0.0, (hDist - Tuning.CLOSE_DISTANCE) / span))
                : 1.0;

        // Tick-phase detection: use full tick for fuse swell interpolation (good enough for intensity).
        float swell = creeper.getSwelling(1.0F);
        float fuse = Math.min(1f, swell) * Tuning.SWELL_INTENSITY_SCALE;

        float intensity = Math.min(1f, (float) distFactor + fuse);
        if (dy > Tuning.VERTICAL_FADE_START) {
            double t = (dy - Tuning.VERTICAL_FADE_START) / (Tuning.VERTICAL_FADE_END - Tuning.VERTICAL_FADE_START);
            double verticalFactor = 1.0 - Math.min(1.0, Math.max(0.0, t));
            intensity *= (float) verticalFactor;
        }

        // Signed angle from look->target in XZ, mapped around the "behind" axis:
        // 0 = behind, + = behind-left, - = behind-right.
        double cross = lx * tz - lz * tx;
        double signed = Math.atan2(cross, dot); // (-pi, pi], behind is near +/-pi
        float angleFromBehind =
                signed >= 0 ? (float) (signed - Math.PI) : (float) (signed + Math.PI);

        return new Scored(intensity, angleFromBehind);
    }

    private record Scored(float intensity, float angleFromBehindRad) {
        private static final Scored ZERO = new Scored(0f, 0f);
    }
}
