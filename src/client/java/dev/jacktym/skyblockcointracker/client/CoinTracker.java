package dev.jacktym.skyblockcointracker.client;

import dev.jacktym.skyblockcointracker.client.util.FormatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

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

        // Strip Minecraft formatting codes (§ followed by any character)
        String stripped = line.replaceAll("§.", "");

        // Remove anything in parentheses like "(+5)" or "(-10)"
        String withoutParens = stripped.replaceAll("\\(.*?\\)", "");

        // Extract just the number part
        String cleaned = withoutParens.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) {
            return;
        }

        try {
            long purse = Long.parseLong(cleaned);
            state.updatePurse(purse);
        } catch (NumberFormatException e) {
            // Ignore parse errors
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
        state.reset();
        sendMessage("Tracker enabled (timer started)");
    }

    public void disable() {
        CoinTrackerConfig.getInstance().setEnabled(false);
        state.reset();
        sendMessage("Tracker disabled (timer reset)");
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
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(
                Component.literal("[CoinTracker] ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(message).withStyle(ChatFormatting.WHITE)),
                false
            );
        }
    }
}
