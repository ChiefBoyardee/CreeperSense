//? if fabric {
package com.creepersense.platform;

import net.fabricmc.api.ClientModInitializer;

public final class FabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Entrypoints.initFabric();
    }
}
//?}

