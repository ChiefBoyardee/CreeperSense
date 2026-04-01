package com.creepersense.neoforge;

import com.creepersense.Constants;
import com.creepersense.client.CreeperSenseClient;
import com.creepersense.client.ClientConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
// NOTE: 26.1 refactors Gui rendering; these imports/events may need adjustment during mv-port-261.
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class CreeperSenseClientEvents {
    private CreeperSenseClientEvents() {}

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "creepersense"));

    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.creepersense.open_settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SETTINGS);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        CreeperSenseClient.tick(Minecraft.getInstance());
        while (OPEN_SETTINGS.consumeClick()) {
            CreeperSenseClient.openSettings();
        }
    }

    @SubscribeEvent
    public static void onHudPre(RenderGuiEvent.Pre event) {
        if (ClientConfig.get().mode != ClientConfig.Mode.PERIPHERAL) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        CreeperSenseClient.renderHudLayer(event.getGuiGraphics(), partialTick, true);
    }

    @SubscribeEvent
    public static void onHudPost(RenderGuiEvent.Post event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        CreeperSenseClient.renderHudLayer(event.getGuiGraphics(), partialTick, false);
    }
}

