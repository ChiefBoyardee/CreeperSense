package com.creepersense.forge;

import com.creepersense.Constants;
import com.creepersense.client.CreeperSenseClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public final class CreeperSenseForgeMod {
    public CreeperSenseForgeMod() {
        Constants.LOG.info("{} loaded (Forge 1.20.1)", Constants.MOD_NAME);
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Bus.FORGE)
    public static final class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            CreeperSenseClient.tick(net.minecraft.client.Minecraft.getInstance());
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
    public static final class ClientModBus {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll(Constants.MOD_ID + ":indicator", (gui, guiGraphics, partialTick, screenWidth, screenHeight) ->
                    CreeperSenseClient.renderHud(guiGraphics, partialTick));
        }
    }
}

