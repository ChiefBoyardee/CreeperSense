package com.creepersense.client;

import com.creepersense.Tuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Soft rear-edge bars + subtle chevron — intensity drives alpha only (no pop-in).
 */
public final class HudPainter {

    private HudPainter() {}

    public static void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, float partialTick, ThreatState state) {
        float a = state.displayIntensity() * Tuning.HUD_MAX_ALPHA;
        if (a < 0.01f) {
            return;
        }
        int ai = (int) (255 * a);
        // Muted moss / creeper hint (avoid loud red to stay “gentle”).
        int base = (ai << 24) | (0x5C << 16) | (0x8A << 8) | 0x3D;

        int strip = Math.max(4, screenWidth / 90);
        int h = screenHeight;

        graphics.fill(0, 0, strip, h, base);
        graphics.fill(screenWidth - strip, 0, screenWidth, h, base);

        chevrons(graphics, screenWidth, screenHeight, state, partialTick);
    }

    private static void chevrons(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
        // Center around the reticle (screen center).
        int arcCx = screenWidth / 2;
        int arcCy = (int) (screenHeight / 2f + screenHeight * Tuning.HUD_ARC_CENTER_Y_OFFSET_SCREEN_HEIGHT_FRACTION);
        int r = Math.max(
                Tuning.HUD_ARC_RADIUS_MIN_PX,
                (int) (screenHeight * Tuning.HUD_ARC_RADIUS_SCREEN_HEIGHT_FRACTION)
        );

        Minecraft mc = Minecraft.getInstance();
        float t = mc.level != null ? mc.level.getGameTime() + partialTick : 0f;
        // Slow, subtle pulse (applied to alpha so it works on both loaders/renderers).
        float pulse = 0.80f + 0.20f * (float) Math.sin(t * 0.25);

        int s = Math.max(10, screenHeight / 38);

        // Yellow chevrons (single per creeper), slightly pulsing.
        float global = Math.min(1f, state.displayIntensity());

        int n = Math.min(state.trackedCount(), 12);
        for (int i = 0; i < n; i++) {
            float intensity = state.intensityAt(i);
            if (intensity < 0.04f) continue;

            float angleFromBehindRad = state.angleAt(i);
            float sx = (float) Math.sin(angleFromBehindRad);
            float cx = (float) Math.cos(angleFromBehindRad);
            int px = (int) (arcCx - sx * r);
            // 0 angleFromBehindRad = "behind" -> place below the reticle.
            int py = (int) (arcCy + cx * r);

            // Per-creeper alpha: heavily distance/intensity-weighted so far targets are noticeably faint.
            float per = Math.min(1f, 0.10f + 0.90f * intensity);
            int ai = (int) (255 * global * per * pulse);

            // Color ramps with intensity: green (far) -> yellow (mid) -> red (close).
            // intensity is already distance-weighted (plus fuse swell), so it works as a "closeness" proxy.
            float p01 = Math.min(1f, Math.max(0f, intensity));
            int rC, gC, bC;
            if (p01 < 0.55f) {
                float u = p01 / 0.55f;
                // Green -> Yellow
                rC = (int) (0x55 + (0xF0 - 0x55) * u);
                gC = (int) (0xD6 + (0xD8 - 0xD6) * u);
                bC = (int) (0x4A + (0x4A - 0x4A) * u);
            } else {
                float u = (p01 - 0.55f) / 0.45f;
                // Yellow -> Red
                rC = (int) (0xF0 + (0xFF - 0xF0) * u);
                gC = (int) (0xD8 + (0x3A - 0xD8) * u);
                bC = (int) (0x4A + (0x2A - 0x4A) * u);
            }
            int rgb = (rC << 16) | (gC << 8) | bC;
            int perArgb = (ai << 24) | rgb;

            drawChevron(graphics, px, py, s, angleFromBehindRad, perArgb);
        }
    }

    private static void drawChevron(GuiGraphicsExtractor g, int x, int y, int size, float rotRad, int argb) {
        // 26.1 GUI extraction uses a different pose stack type; do manual rotation instead.
        int len = Math.max(8, size);
        int tipY = y + len / 2;
        int thick = Math.max(2, size / 6);
        float c = (float) Math.cos(rotRad);
        float s = (float) Math.sin(rotRad);

        // Draw two diagonal strokes using small filled squares (fast enough at this size).
        for (int i = 0; i <= len; i++) {
            int py = tipY - i;
            int pxL = x - i;
            int pxR = x + i;

            blitSquare(g, x, y, pxL, py, c, s, thick, argb);
            blitSquare(g, x, y, pxR, py, c, s, thick, argb);
        }
    }

    private static void blitSquare(
            GuiGraphicsExtractor g,
            int cx,
            int cy,
            int px,
            int py,
            float c,
            float s,
            int thick,
            int argb
    ) {
        float dx = px - cx;
        float dy = py - cy;
        int rx = Math.round(cx + dx * c - dy * s);
        int ry = Math.round(cy + dx * s + dy * c);
        int hw = thick / 2;
        g.fill(rx - hw, ry - hw, rx + (thick + 1) / 2, ry + (thick + 1) / 2, argb);
    }
}
