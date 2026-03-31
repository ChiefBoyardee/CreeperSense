package com.creepersense.fabric;

import com.creepersense.client.CreeperSenseClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

public final class CreeperSenseFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(CreeperSenseFabricClient::onClientTick);
        HudRenderCallback.EVENT.register(CreeperSenseFabricClient::onHudRender);
    }

    private static void onClientTick(Minecraft minecraft) {
        CreeperSenseClient.tick(minecraft);
    }

    private static void onHudRender(net.minecraft.client.gui.GuiGraphics drawContext, net.minecraft.client.DeltaTracker tickCounter) {
        CreeperSenseClient.renderHud(drawContext, tickCounter.getGameTimeDeltaPartialTick(true));
    }
}
