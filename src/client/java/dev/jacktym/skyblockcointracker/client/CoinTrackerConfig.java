package dev.jacktym.skyblockcointracker.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CoinTrackerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cointracker.json");

    private boolean enabled = false;
    private int overlayX = 10;
    private int overlayY = 10;
    private float scale = 1.0f;
    private int timeoutMinutes = 5;

    private static CoinTrackerConfig instance;

    public static CoinTrackerConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static CoinTrackerConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                CoinTrackerConfig config = GSON.fromJson(json, CoinTrackerConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CoinTrackerConfig config = new CoinTrackerConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); }

    public int getOverlayX() { return overlayX; }
    public void setOverlayX(int x) { this.overlayX = x; save(); }

    public int getOverlayY() { return overlayY; }
    public void setOverlayY(int y) { this.overlayY = y; save(); }

    public void setOverlayPosition(int x, int y) {
        this.overlayX = x;
        this.overlayY = y;
        save();
    }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; save(); }

    public int getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(int minutes) { this.timeoutMinutes = minutes; save(); }

    public int getTimeoutSeconds() { return timeoutMinutes * 60; }
}
