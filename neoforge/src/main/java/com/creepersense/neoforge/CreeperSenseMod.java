package com.creepersense.neoforge;

import com.creepersense.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class CreeperSenseMod {

    public CreeperSenseMod(@SuppressWarnings("unused") IEventBus modEventBus) {
        Constants.LOG.info("{} loaded (NeoForge)", Constants.MOD_NAME);
    }
}
