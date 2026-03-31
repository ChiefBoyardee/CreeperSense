package com.creepersense.client;

import com.creepersense.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfig {
    public enum Mode {
        CHEVRONS,
        PERIPHERAL,
        MEME
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "creepersense.json";

    public Mode mode = Mode.CHEVRONS;
    public boolean difficultyScalingEnabled = true;

    /**
     * Meme mode only appears when overall threat intensity >= this value (0..1).
     */
    public float memeModeMinGlobalIntensity = 0.70f;

    private static ClientConfig INSTANCE;

    private ClientConfig() {}

    public static ClientConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static void save() {
        ClientConfig cfg = get();
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(cfg, w);
            }
        } catch (IOException e) {
            Constants.LOG.warn("Failed to save CreeperSense config", e);
        }
    }

    private static ClientConfig load() {
        Path path = configPath();
        if (Files.isRegularFile(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                ClientConfig cfg = GSON.fromJson(r, ClientConfig.class);
                if (cfg != null && cfg.mode != null) {
                    return cfg;
                }
            } catch (IOException | JsonSyntaxException e) {
                Constants.LOG.warn("Failed to load CreeperSense config, using defaults", e);
            }
        }
        return new ClientConfig();
    }

    private static Path configPath() {
        // Works on Fabric + NeoForge: Minecraft.gameDirectory points at the instance root.
        Path root = Minecraft.getInstance().gameDirectory.toPath();
        return root.resolve("config").resolve(FILE_NAME);
    }
}

