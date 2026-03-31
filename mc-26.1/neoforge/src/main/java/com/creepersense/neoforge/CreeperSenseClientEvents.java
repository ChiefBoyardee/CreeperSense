package com.creepersense.neoforge;

import com.creepersense.Constants;
import com.creepersense.client.CreeperSenseClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
// NOTE: 26.1 refactors Gui rendering; these imports/events may need adjustment during mv-port-261.
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class CreeperSenseClientEvents {
    private CreeperSenseClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        CreeperSenseClient.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onHud(RenderGuiEvent.Post event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        CreeperSenseClient.renderHud(event.getGuiGraphics(), partialTick);
    }
}

