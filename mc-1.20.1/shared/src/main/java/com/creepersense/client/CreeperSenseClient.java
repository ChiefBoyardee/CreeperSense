package com.creepersense.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

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

    public static void renderHud(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        HudPainter.render(graphics, w, h, partialTick, STATE);
    }

    public static void renderHudLayer(GuiGraphics graphics, float partialTick, boolean underHotbar) {
        ClientConfig.Mode mode = ClientConfig.get().mode;
        boolean isPeripheral = mode == ClientConfig.Mode.PERIPHERAL;
        if (underHotbar != isPeripheral) {
            return;
        }
        renderHud(graphics, partialTick);
    }

    public static void openSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new CreeperSenseConfigScreen(mc.screen));
    }
}
