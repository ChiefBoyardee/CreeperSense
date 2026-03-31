package com.creepersense.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Loader-agnostic façade: call {@link #tick} from client tick and {@link #renderHud} from HUD layer.
 */
public final class CreeperSenseClient {

    private static final ThreatState STATE = new ThreatState();

    private CreeperSenseClient() {}

    public static void tick(Minecraft minecraft) {
        DetectionResult r = CreeperBehindDetector.detect(minecraft);
        STATE.setTargetIntensity(r.maxIntensity());
        STATE.setTargets(r.creepers());
        STATE.smoothTowardTarget();
    }

    public static void renderHud(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        HudPainter.render(graphics, w, h, partialTick, STATE);
    }
}
