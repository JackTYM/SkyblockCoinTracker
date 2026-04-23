package dev.jacktym.skyblockcointracker.client;

import dev.jacktym.skyblockcointracker.client.util.FormatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CoinTracker {
    private static final CoinTracker INSTANCE = new CoinTracker();

    private final CoinTrackerState state = new CoinTrackerState();

    private CoinTracker() {}

    public static CoinTracker getInstance() {
        return INSTANCE;
    }

    public void onScoreboardUpdate(String line) {
        // Look for "Purse:" or "Piggy:" in the scoreboard line
        if (!line.contains("Purse:") && !line.contains("Piggy:")) {
            return;
        }

        // Parse the coin value - remove formatting codes and extract number
        String cleaned = line.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return;

        try {
            long purse = Long.parseLong(cleaned);
            state.updatePurse(purse);
        } catch (NumberFormatException e) {
            // Ignore malformed values
        }
    }

    public void tick() {
        if (!CoinTrackerConfig.getInstance().isEnabled()) return;
        state.tick(CoinTrackerConfig.getInstance().getTimeoutSeconds());
    }

    public void reset() {
        state.reset();
        sendMessage("Tracker reset!");
    }

    public void enable() {
        CoinTrackerConfig.getInstance().setEnabled(true);
        sendMessage("Tracker enabled");
    }

    public void disable() {
        CoinTrackerConfig.getInstance().setEnabled(false);
        sendMessage("Tracker disabled");
    }

    public void toggle() {
        if (CoinTrackerConfig.getInstance().isEnabled()) {
            disable();
        } else {
            enable();
        }
    }

    public void pause() {
        state.pause();
        sendMessage("Tracker paused");
    }

    public void unpause() {
        state.unpause();
        sendMessage("Tracker unpaused");
    }

    public void togglePause() {
        state.togglePause();
        sendMessage(state.isPaused() ? "Tracker paused" : "Tracker unpaused");
    }

    public void setTime(int minutes) {
        state.setElapsedSeconds(minutes * 60);
        sendMessage("Time set to " + minutes + " minutes");
    }

    public void setGain(long amount) {
        state.setGain(amount);
        sendMessage("Gain set to " + FormatUtil.formatCoins(amount));
    }

    public CoinTrackerState getState() {
        return state;
    }

    private void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("[CoinTracker] ").formatted(Formatting.GOLD)
                    .append(Text.literal(message).formatted(Formatting.WHITE)),
                false
            );
        }
    }
}
