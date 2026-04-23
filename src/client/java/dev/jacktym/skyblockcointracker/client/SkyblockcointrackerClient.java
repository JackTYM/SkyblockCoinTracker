package dev.jacktym.skyblockcointracker.client;

import dev.jacktym.skyblockcointracker.client.commands.CoinTrackerCommand;
import dev.jacktym.skyblockcointracker.client.overlay.CoinOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SkyblockcointrackerClient implements ClientModInitializer {
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // Load config
        CoinTrackerConfig.getInstance();

        // Load saved session state
        CoinTracker.getInstance().getState().loadSavedState();

        // Register components
        CoinTrackerCommand.register();
        CoinTrackerKeybinds.register();
        CoinOverlay.register();
        ChatListener.register();

        // Register tick event for elapsed time tracking (once per second)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= 20) { // 20 ticks = 1 second
                tickCounter = 0;
                CoinTracker.getInstance().tick();
            }
        });
    }
}
