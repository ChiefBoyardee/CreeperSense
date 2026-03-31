package com.creepersense.platform;

import com.creepersense.client.CreeperSenseClient;
import net.minecraft.client.Minecraft;

public final class Entrypoints {
    private Entrypoints() {}

    //? if fabric {
    public static void initFabric() {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(CreeperSenseClient::tick);
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(
                (guiGraphics, deltaTracker) -> CreeperSenseClient.renderHud(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true))
        );
    }
    //?}

    //? if neoforge {
    public static void initNeoForge() {
        // NeoForge uses static event subscribers; nothing to do here.
    }
    //?}
}

