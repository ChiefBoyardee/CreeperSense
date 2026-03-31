package com.creepersense.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class CreeperSenseClient {
    private static final ThreatState STATE = new ThreatState();

    private CreeperSenseClient() {}

    public static void tick(Minecraft minecraft) {
        DetectionResult r = CreeperBehindDetector.detect(minecraft);
        STATE.setTargetIntensity(r.maxIntensity());
        STATE.setTargets(r.creepers());
        STATE.smoothTowardTarget();
    }

    public static void renderHud(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        HudPainter.render(graphics, graphics.guiWidth(), graphics.guiHeight(), partialTick, STATE);
    }
}

