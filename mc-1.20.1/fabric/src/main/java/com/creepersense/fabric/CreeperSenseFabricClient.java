package com.creepersense.fabric;

import com.creepersense.client.CreeperSenseClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class CreeperSenseFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(CreeperSenseClient::tick);
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) ->
                CreeperSenseClient.renderHud(guiGraphics, tickDelta));
    }
}

