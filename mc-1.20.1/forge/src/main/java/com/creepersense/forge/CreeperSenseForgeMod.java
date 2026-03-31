package com.creepersense.forge;

import com.creepersense.Constants;
import com.creepersense.client.CreeperSenseClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(Constants.MOD_ID)
public final class CreeperSenseForgeMod {
    public CreeperSenseForgeMod() {
        Constants.LOG.info("{} loaded (Forge 1.20.1)", Constants.MOD_NAME);
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Bus.FORGE)
    public static final class ClientForgeEvents {
        private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
                "key.creepersense.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "key.categories.creepersense"
        );

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            CreeperSenseClient.tick(net.minecraft.client.Minecraft.getInstance());
            while (OPEN_SETTINGS.consumeClick()) {
                CreeperSenseClient.openSettings();
            }
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
    public static final class ClientModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ClientForgeEvents.OPEN_SETTINGS);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll(Constants.MOD_ID + ":indicator", (gui, guiGraphics, partialTick, screenWidth, screenHeight) ->
                    CreeperSenseClient.renderHud(guiGraphics, partialTick));
        }
    }
}

