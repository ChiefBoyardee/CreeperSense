//? if neoforge {
package com.creepersense.platform;

import com.creepersense.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public final class NeoForgeMod {
    public NeoForgeMod(@SuppressWarnings("unused") IEventBus modEventBus) {
        Constants.LOG.info("{} loaded (NeoForge)", Constants.MOD_NAME);
        Entrypoints.initNeoForge();
    }
}
//?}

