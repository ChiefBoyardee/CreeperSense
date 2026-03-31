package com.creepersense.client;

import com.creepersense.Tuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudPainter {
    private HudPainter() {}

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick, ThreatState state) {
        float a = state.displayIntensity() * Tuning.HUD_MAX_ALPHA;
        if (a < 0.01f) return;

        int ai = (int) (255 * a);
        int base = (ai << 24) | (0x5C << 16) | (0x8A << 8) | 0x3D;

        int strip = Math.max(4, screenWidth / 90);
        graphics.fill(0, 0, strip, screenHeight, base);
        graphics.fill(screenWidth - strip, 0, screenWidth, screenHeight, base);

        chevrons(graphics, screenWidth, screenHeight, state, partialTick);
    }

    private static void chevrons(GuiGraphics graphics, int screenWidth, int screenHeight, ThreatState state, float partialTick) {
        int arcCx = screenWidth / 2;
        int arcCy = (int) (screenHeight / 2f + screenHeight * Tuning.HUD_ARC_CENTER_Y_OFFSET_SCREEN_HEIGHT_FRACTION);
        int r = Math.max(Tuning.HUD_ARC_RADIUS_MIN_PX, (int) (screenHeight * Tuning.HUD_ARC_RADIUS_SCREEN_HEIGHT_FRACTION));

        Minecraft mc = Minecraft.getInstance();
        float t = mc.level != null ? mc.level.getGameTime() + partialTick : 0f;
        float pulse = 0.80f + 0.20f * (float) Math.sin(t * 0.25);

        float global = Math.min(1f, state.displayIntensity());
        int s = Math.max(10, screenHeight / 38);

        int n = Math.min(state.trackedCount(), 12);
        for (int i = 0; i < n; i++) {
            float intensity = state.intensityAt(i);
            if (intensity < 0.04f) continue;

            float angle = state.angleAt(i);
            float sx = (float) Math.sin(angle);
            float cx = (float) Math.cos(angle);
            int px = (int) (arcCx - sx * r);
            int py = (int) (arcCy + cx * r);

            float per = Math.min(1f, 0.10f + 0.90f * intensity);
            int alpha = (int) (255 * global * per * pulse);

            float p01 = Math.min(1f, Math.max(0f, intensity));
            int rC, gC, bC;
            if (p01 < 0.55f) {
                float u = p01 / 0.55f;
                rC = (int) (0x55 + (0xF0 - 0x55) * u);
                gC = (int) (0xD6 + (0xD8 - 0xD6) * u);
                bC = 0x4A;
            } else {
                float u = (p01 - 0.55f) / 0.45f;
                rC = (int) (0xF0 + (0xFF - 0xF0) * u);
                gC = (int) (0xD8 + (0x3A - 0xD8) * u);
                bC = (int) (0x4A + (0x2A - 0x4A) * u);
            }

            int argb = (alpha << 24) | (rC << 16) | (gC << 8) | bC;
            drawChevron(graphics, px, py, s, angle, argb);
        }
    }

    private static void drawChevron(GuiGraphics g, int x, int y, int size, float rotRad, int argb) {
        var pose = g.pose();
        pose.pushPose();
        pose.translate(x + 0.5, y + 0.5, 0);
        pose.mulPose(com.mojang.math.Axis.ZP.rotation(rotRad));
        pose.translate(-(x + 0.5), -(y + 0.5), 0);

        int len = Math.max(8, size);
        int tipY = y + len / 2;
        int thick = Math.max(2, size / 6);

        for (int i = 0; i <= len; i++) {
            int py = tipY - i;
            int pxL = x - i;
            int pxR = x + i;
            g.fill(pxL - thick / 2, py - thick / 2, pxL + (thick + 1) / 2, py + (thick + 1) / 2, argb);
            g.fill(pxR - thick / 2, py - thick / 2, pxR + (thick + 1) / 2, py + (thick + 1) / 2, argb);
        }

        pose.popPose();
    }
}

