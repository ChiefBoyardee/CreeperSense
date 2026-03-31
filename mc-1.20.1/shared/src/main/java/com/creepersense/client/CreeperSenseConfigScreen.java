package com.creepersense.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CreeperSenseConfigScreen extends Screen {
    private final Screen parent;

    public CreeperSenseConfigScreen(Screen parent) {
        super(Component.literal("CreeperSense Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 4;

        ClientConfig cfg = ClientConfig.get();

        addRenderableWidget(
                CycleButton.<ClientConfig.Mode>builder(mode -> Component.literal(switch (mode) {
                            case CHEVRONS -> "Chevrons";
                            case PERIPHERAL -> "Peripheral";
                            case MEME -> "Meme (high danger)";
                        }))
                        .withValues(ClientConfig.Mode.values())
                        .withInitialValue(cfg.mode)
                        .create(cx - 100, y, 200, 20, Component.literal("Mode"), (btn, val) -> {
                            cfg.mode = val;
                            ClientConfig.save();
                        })
        );
        y += 28;

        addRenderableWidget(
                CycleButton.onOffBuilder(cfg.difficultyScalingEnabled)
                        .create(cx - 100, y, 200, 20, Component.literal("Difficulty scaling"), (btn, val) -> {
                            cfg.difficultyScalingEnabled = val;
                            ClientConfig.save();
                        })
        );
        y += 40;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx - 60, Math.min(this.height - 28, y), 120, 20)
                .build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}

