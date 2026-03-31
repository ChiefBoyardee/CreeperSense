package com.creepersense.client;

import com.creepersense.Tuning;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class CreeperBehindDetector {
    private CreeperBehindDetector() {}

    public static DetectionResult detect(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return new DetectionResult(0f, List.of());
        }

        Player player = mc.player;
        AABB box = player.getBoundingBox().inflate(Tuning.SEARCH_RADIUS);
        float bestIntensity = 0f;
        ArrayList<DetectedCreeper> creepers = new ArrayList<>();

        for (Creeper creeper : mc.level.getEntitiesOfClass(Creeper.class, box)) {
            if (!creeper.isAlive()) continue;

            Scored s = scoreCreeper(player, creeper);
            if (s.intensity > bestIntensity) bestIntensity = s.intensity;
            if (s.intensity > 0f) {
                creepers.add(new DetectedCreeper(creeper.getId(), s.intensity, s.angleFromBehindRad));
            }
        }

        return new DetectionResult(Math.min(1f, bestIntensity), creepers);
    }

    private static Scored scoreCreeper(Player player, Creeper creeper) {
        Vec3 look = player.getLookAngle();
        double lx = look.x;
        double lz = look.z;
        double hLookLen = Math.sqrt(lx * lx + lz * lz);
        if (hLookLen < 1e-6) return Scored.ZERO;
        lx /= hLookLen;
        lz /= hLookLen;

        Vec3 to = creeper.position().subtract(player.position());
        double tx = to.x;
        double tz = to.z;
        double hDist = Math.sqrt(tx * tx + tz * tz);
        if (hDist < 1e-6 || hDist > Tuning.SEARCH_RADIUS) return Scored.ZERO;
        tx /= hDist;
        tz /= hDist;

        double dot = tx * lx + tz * lz;
        if (dot > Tuning.BEHIND_DOT_THRESHOLD) return Scored.ZERO;

        double span = Tuning.SEARCH_RADIUS - Tuning.CLOSE_DISTANCE;
        double distFactor = span > 1e-6
                ? 1.0 - Math.min(1.0, Math.max(0.0, (hDist - Tuning.CLOSE_DISTANCE) / span))
                : 1.0;

        float swell = creeper.getSwelling(1.0F);
        float fuse = Math.min(1f, swell) * Tuning.SWELL_INTENSITY_SCALE;
        float intensity = Math.min(1f, (float) distFactor + fuse);

        double cross = lx * tz - lz * tx;
        double signed = Math.atan2(cross, dot);
        float angleFromBehind = signed >= 0 ? (float) (signed - Math.PI) : (float) (signed + Math.PI);
        return new Scored(intensity, angleFromBehind);
    }

    private record Scored(float intensity, float angleFromBehindRad) {
        private static final Scored ZERO = new Scored(0f, 0f);
    }
}

