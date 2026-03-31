package com.creepersense.fabric;

import com.creepersense.client.CreeperSenseClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class CreeperSenseFabricClient implements ClientModInitializer {
    private static KeyMapping OPEN_SETTINGS;

    @Override
    public void onInitializeClient() {
        OPEN_SETTINGS = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.creepersense.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.categories.creepersense"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(CreeperSenseFabricClient::onClientTick);
        HudRenderCallback.EVENT.register(CreeperSenseFabricClient::onHudRender);
    }

    private static void onClientTick(Minecraft minecraft) {
        CreeperSenseClient.tick(minecraft);
        while (OPEN_SETTINGS != null && OPEN_SETTINGS.consumeClick()) {
            CreeperSenseClient.openSettings();
        }
    }

    private static void onHudRender(net.minecraft.client.gui.GuiGraphics drawContext, net.minecraft.client.DeltaTracker tickCounter) {
        CreeperSenseClient.renderHud(drawContext, tickCounter.getGameTimeDeltaPartialTick(true));
    }
}
