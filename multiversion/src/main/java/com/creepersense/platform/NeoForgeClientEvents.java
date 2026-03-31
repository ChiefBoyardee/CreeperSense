//? if neoforge {
package com.creepersense.platform;

import com.creepersense.Constants;
import com.creepersense.client.CreeperSenseClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {}

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
//?}

