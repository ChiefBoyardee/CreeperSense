package com.creepersense.fabric;

import com.creepersense.client.CreeperSenseClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class CreeperSenseFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(CreeperSenseClient::tick);
        Identifier id = Identifier.fromNamespaceAndPath("creepersense", "indicator");
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, id, (graphics, deltaTracker) ->
                CreeperSenseClient.renderHud(graphics, deltaTracker.getGameTimeDeltaPartialTick(true)));
    }
}

