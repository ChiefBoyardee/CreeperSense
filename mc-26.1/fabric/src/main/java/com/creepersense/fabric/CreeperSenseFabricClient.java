package com.creepersense.fabric;

import com.creepersense.client.CreeperSenseClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class CreeperSenseFabricClient implements ClientModInitializer {
    private static KeyMapping OPEN_SETTINGS;
    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = new KeyMapping.Category(Identifier.fromNamespaceAndPath("creepersense", "creepersense"));
        OPEN_SETTINGS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.creepersense.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                category
        ));
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            CreeperSenseClient.tick(minecraft);
            while (OPEN_SETTINGS.consumeClick()) {
                CreeperSenseClient.openSettings();
            }
        });
        Identifier underlayId = Identifier.fromNamespaceAndPath("creepersense", "indicator_underlay");
        HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, underlayId, (graphics, deltaTracker) ->
                CreeperSenseClient.renderHudLayer(graphics, deltaTracker.getGameTimeDeltaPartialTick(true), true));

        Identifier overlayId = Identifier.fromNamespaceAndPath("creepersense", "indicator_overlay");
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR, overlayId, (graphics, deltaTracker) ->
                CreeperSenseClient.renderHudLayer(graphics, deltaTracker.getGameTimeDeltaPartialTick(true), false));
    }
}

