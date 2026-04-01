package com.creepersense.client;

import com.creepersense.Tuning;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * CreeperSense HUD rendering.
 */
public final class HudPainter {

    private HudPainter() {}

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick, ThreatState state) {
        float a = state.displayIntensity() * Tuning.HUD_MAX_ALPHA;
        if (a < 0.01f) {
            return;
        }
        ClientConfig cfg = ClientConfig.get();
        switch (cfg.mode) {
            case CHEVRONS -> chevrons(graphics, screenWidth, screenHeight, state, partialTick);
            case PERIPHERAL -> peripheral(graphics, screenWidth, screenHeight, state, partialTick);
            case MEME -> {
                if (state.displayIntensity() >= cfg.memeModeMinGlobalIntensity) {
                    memeOverlay(graphics, screenWidth, screenHeight, state, partialTick);
                }
            }
        }
    }

    private static void chevrons(GuiGraphics graphics, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
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

    private static void drawChevron(GuiGraphics g, int x, int y, int size, float rotRad, int argb) {
        // Local-space chevron points "down" (toward +Y). We'll rotate around (x,y).
        // Use pose transforms so the chevron rotates + positions cheaply.
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x + 0.5, y + 0.5, 0);
        pose.mulPose(com.mojang.math.Axis.ZP.rotation(rotRad));
        pose.translate(-(x + 0.5), -(y + 0.5), 0);

        // True chevron: two diagonals meeting at a point (no vertical stem).
        int len = Math.max(8, size);
        int tipY = y + len / 2;
        int thick = Math.max(2, size / 6);

        // Draw two diagonal strokes using small filled squares (fast enough at this size).
        for (int i = 0; i <= len; i++) {
            int py = tipY - i;
            int pxL = x - i;
            int pxR = x + i;
            g.fill(pxL - thick / 2, py - thick / 2, pxL + (thick + 1) / 2, py + (thick + 1) / 2, argb);
            g.fill(pxR - thick / 2, py - thick / 2, pxR + (thick + 1) / 2, py + (thick + 1) / 2, argb);
        }

        pose.popPose();
    }

    private static void peripheral(GuiGraphics g, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
        int cx = screenWidth / 2;
        int cy = screenHeight / 2;

        int margin = Math.max(18, Math.min(screenWidth, screenHeight) / 30);
        int left = margin;
        int right = screenWidth - margin;
        int top = margin;
        int bottom = screenHeight - margin;

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

            int[] p = placeOnInsetRect(cx, cy, dx, dy, left, right, top, bottom);
            int px = p[0];
            int py = p[1];

            float per = Math.min(1f, 0.10f + 0.90f * intensity);
            int ai = (int) (255 * global * per * pulse);
            int rgb = rampRgb(intensity);
            int argb = (ai << 24) | rgb;

            int r = clamp(Math.round(screenHeight / 80f), 5, 10);
            int thick = Math.max(2, r / 4);
            drawRing(g, px, py, r, thick, argb);
        }
    }

    private static int rampRgb(float intensity01) {
        // Match chevron gradient: green -> yellow -> red.
        float p01 = Math.min(1f, Math.max(0f, intensity01));
        int rC, gC, bC;
        if (p01 < 0.55f) {
            float u = p01 / 0.55f;
            rC = (int) (0x55 + (0xF0 - 0x55) * u);
            gC = (int) (0xD6 + (0xD8 - 0xD6) * u);
            bC = (int) (0x4A + (0x4A - 0x4A) * u);
        } else {
            float u = (p01 - 0.55f) / 0.45f;
            rC = (int) (0xF0 + (0xFF - 0xF0) * u);
            gC = (int) (0xD8 + (0x3A - 0xD8) * u);
            bC = (int) (0x4A + (0x2A - 0x4A) * u);
        }
        return (rC << 16) | (gC << 8) | bC;
    }

    private static int[] placeOnInsetRect(int cx, int cy, float dx, float dy, int left, int right, int top, int bottom) {
        float eps = 1e-4f;
        float invDx = 1f / (Math.abs(dx) < eps ? (dx < 0f ? -eps : eps) : dx);
        float invDy = 1f / (Math.abs(dy) < eps ? (dy < 0f ? -eps : eps) : dy);

        float tx = dx > 0f ? (right - cx) * invDx : (left - cx) * invDx;
        float ty = dy > 0f ? (bottom - cy) * invDy : (top - cy) * invDy;
        float t = Math.min(tx, ty);

        int px = Math.round(cx + dx * t);
        int py = Math.round(cy + dy * t);

        px = clamp(px, left, right);
        py = clamp(py, top, bottom);
        return new int[] { px, py };
    }

    private static void drawRing(GuiGraphics g, int cx, int cy, int r, int thick, int argb) {
        int rOuter = r + thick;
        int rOuter2 = rOuter * rOuter;
        int rInner2 = Math.max(0, r - thick) * Math.max(0, r - thick);

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

    private static final ResourceLocation MEME_WARN =
            ResourceLocation.fromNamespaceAndPath("creepersense", "textures/gui/meme/meme-warning-triangle.png");
    private static final ResourceLocation MEME_SPEECH =
            ResourceLocation.fromNamespaceAndPath("creepersense", "textures/gui/meme/meme-speech-bubble-watch-out-bro.png");
    private static final ResourceLocation MEME_CAT =
            ResourceLocation.fromNamespaceAndPath("creepersense", "textures/gui/meme/meme-cat.png");

    private static void memeOverlay(GuiGraphics g, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        ClientConfig cfg = ClientConfig.get();
        float global = clamp01(state.displayIntensity());
        float tNorm = (global - cfg.memeModeMinGlobalIntensity) / Math.max(1e-4f, (1f - cfg.memeModeMinGlobalIntensity));
        float eased = smoothstep01(clamp01(tNorm));
        // Fade in like chevrons/circles, but guarantee full visibility only at "about to explode" intensity.
        float alpha = global >= 0.95f ? 1f : eased;

        int minDim = Math.min(screenWidth, screenHeight);
        int pad = Math.max(10, minDim / 40);

        int warnW = clamp(Math.round(minDim * 0.085f), 34, 64);
        int warnH = Math.round(warnW * (107f / 117f));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        blitScaled(g, MEME_WARN, pad, pad, warnW, warnH, 117, 107);
        blitScaled(g, MEME_WARN, screenWidth - pad - warnW, pad, warnW, warnH, 117, 107);
        blitScaled(g, MEME_WARN, pad, screenHeight - pad - warnH, warnW, warnH, 117, 107);
        blitScaled(g, MEME_WARN, screenWidth - pad - warnW, screenHeight - pad - warnH, warnW, warnH, 117, 107);

        int speechW = Math.min(screenWidth - pad * 2, clamp(Math.round(screenWidth * 0.48f), 240, 520));
        int speechH = Math.round(speechW * (326f / 584f));
        int speechX = (screenWidth - speechW) / 2;
        int speechY = clamp((screenHeight / 2) - (speechH / 2), pad + warnH + pad, screenHeight - pad - speechH - warnH - pad);
        blitScaled(g, MEME_SPEECH, speechX, speechY, speechW, speechH, 584, 326);

        int catW = clamp(Math.round(screenWidth * 0.14f), 54, 110);
        int catH = Math.round(catW * (637f / 450f));
        int catX = pad + warnW + pad;
        int catY = screenHeight - pad - catH - warnH;
        blitScaled(g, MEME_CAT, catX, catY, catW, catH, 450, 637);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    private static void blitScaled(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, int texW, int texH) {
        // Draw the full texture region (texW x texH) scaled into (w x h).
        g.blit(tex, x, y, w, h, 0, 0, texW, texH, texW, texH);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static float smoothstep01(float t) {
        return t * t * (3f - 2f * t);
    }
}
