package com.creepersense.client;

import com.creepersense.Tuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * CreeperSense HUD rendering.
 */
public final class HudPainter {

    private HudPainter() {}

    public static void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, float partialTick, ThreatState state) {
        float a = state.displayIntensity() * Tuning.HUD_MAX_ALPHA;
        if (a < 0.01f) {
            return;
        }
        ClientConfig cfg = ClientConfig.get();
        switch (cfg.mode) {
            case CHEVRONS -> chevrons(graphics, screenWidth, screenHeight, state, partialTick);
            case PERIPHERAL -> peripheral(graphics, screenWidth, screenHeight, state, partialTick);
            case MEME -> {
                chevrons(graphics, screenWidth, screenHeight, state, partialTick);
                if (state.displayIntensity() >= cfg.memeModeMinGlobalIntensity) {
                    memeOverlay(graphics, screenWidth, screenHeight, state, partialTick);
                }
            }
        }
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

    private static void peripheral(GuiGraphicsExtractor g, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2;
        int margin = Math.max(18, Math.min(screenWidth, screenHeight) / 30);
        int maxR = (Math.min(screenWidth, screenHeight) / 2) - margin;

        Minecraft mc = Minecraft.getInstance();
        float t = mc.level != null ? mc.level.getGameTime() + partialTick : 0f;
        float pulse = 0.85f + 0.15f * (float) Math.sin(t * 0.35);

        float global = Math.min(1f, state.displayIntensity());
        int n = Math.min(state.trackedCount(), 12);

        for (int i = 0; i < n; i++) {
            float intensity = state.intensityAt(i);
            if (intensity < 0.06f) continue;

            float angle = state.angleAt(i);
            float dx = -(float) Math.sin(angle);
            float dy = (float) Math.cos(angle);

            int px = Math.round(cx + dx * maxR);
            int py = Math.round(cy + dy * maxR);

            float per = Math.min(1f, 0.10f + 0.90f * intensity);
            int ai = (int) (255 * global * per * pulse);
            int rgb = 0xF0D84A;
            int argb = (ai << 24) | rgb;

            int r = Math.max(8, Math.min(16, screenHeight / 54));
            int thick = Math.max(2, r / 4);
            drawRing(g, px, py, r, thick, argb);
        }
    }

    private static void drawRing(GuiGraphicsExtractor g, int cx, int cy, int r, int thick, int argb) {
        int rOuter = r + thick;
        int rOuter2 = rOuter * rOuter;
        int rInner = Math.max(0, r - thick);
        int rInner2 = rInner * rInner;

        for (int y = -rOuter; y <= rOuter; y++) {
            int yy = y * y;
            for (int x = -rOuter; x <= rOuter; x++) {
                int d2 = x * x + yy;
                if (d2 <= rOuter2 && d2 >= rInner2) {
                    g.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, argb);
                }
            }
        }
    }

    private static final Identifier MEME_WARN =
            Identifier.fromNamespaceAndPath("creepersense", "textures/gui/meme/meme-warning-triangle.png");
    private static final Identifier MEME_SPEECH =
            Identifier.fromNamespaceAndPath("creepersense", "textures/gui/meme/meme-speech-bubble-watch-out-bro.png");
    private static final Identifier MEME_CAT =
            Identifier.fromNamespaceAndPath("creepersense", "textures/gui/meme/meme-cat.png");

    private static void memeOverlay(GuiGraphicsExtractor g, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        // 26.1 GUI pipeline does not expose shader-color helpers here; keep meme overlays binary-on.

        int warnW = Math.max(44, screenHeight / 14);
        int warnH = Math.round(warnW * (107f / 117f));
        int pad = Math.max(8, screenWidth / 60);

        blitScaled(g, MEME_WARN, pad, pad, warnW, warnH, 117, 107);
        blitScaled(g, MEME_WARN, screenWidth - pad - warnW, pad, warnW, warnH, 117, 107);
        blitScaled(g, MEME_WARN, pad, screenHeight - pad - warnH, warnW, warnH, 117, 107);
        blitScaled(g, MEME_WARN, screenWidth - pad - warnW, screenHeight - pad - warnH, warnW, warnH, 117, 107);

        int speechW = Math.min(screenWidth - pad * 2, (int) (screenWidth * 0.62f));
        int speechH = Math.round(speechW * (326f / 584f));
        int speechX = (screenWidth - speechW) / 2;
        int speechY = Math.max(pad + warnH + pad, (screenHeight / 2) - (speechH / 2) - warnH / 2);
        blitScaled(g, MEME_SPEECH, speechX, speechY, speechW, speechH, 584, 326);

        int catW = Math.min(screenWidth / 6, 120);
        int catH = Math.round(catW * (637f / 450f));
        blitScaled(g, MEME_CAT, pad, screenHeight - pad - catH - warnH, catW, catH, 450, 637);
    }

    private static void blitScaled(GuiGraphicsExtractor g, Identifier tex, int x, int y, int w, int h, int texW, int texH) {
        g.blit(tex, x, y, 0, 0, w, h, texW, texH);
    }
}
