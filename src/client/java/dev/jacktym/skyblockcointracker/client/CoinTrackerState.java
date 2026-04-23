package dev.jacktym.skyblockcointracker.client;

public class CoinTrackerState {
    private long startPurse = 0;
    private long currentPurse = 0;
    private long pausedPurse = 0;
    private long pausedGain = 0;
    private int elapsedSeconds = 0;
    private int secondsSinceChange = 0;
    private boolean paused = false;
    private boolean inactive = false;
    private long savedGainOffset = 0; // Accumulated gain from previous sessions

    public void loadSavedState() {
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        savedGainOffset = config.getSavedGain();
        elapsedSeconds = config.getSavedElapsedSeconds();
    }

    public void saveState() {
        CoinTrackerConfig.getInstance().saveSessionState(getGained(), elapsedSeconds);
    }

    public void reset() {
        startPurse = currentPurse;
        elapsedSeconds = 0;
        secondsSinceChange = 0;
        paused = false;
        inactive = false;
        savedGainOffset = 0;
        CoinTrackerConfig.getInstance().clearSessionState();
    }

    public void updatePurse(long newPurse) {
        // Track if this is a change (before updating currentPurse)
        boolean changed = currentPurse != newPurse;

        // Always track currentPurse, even when paused (so we can calculate delta on unpause)
        currentPurse = newPurse;

        // But don't update tracking state when paused
        if (paused) return;

        if (changed) {
            if (inactive) {
                // Was inactive, now active again - reset
                startPurse = newPurse;
                elapsedSeconds = 0;
                inactive = false;
            }
            secondsSinceChange = 0;
        }

        // Initialize start purse if this is the first update
        if (startPurse == 0) {
            startPurse = newPurse;
        }
    }

    public void tick(int timeoutSeconds) {
        if (paused) return;

        elapsedSeconds++;
        secondsSinceChange++;

        if (secondsSinceChange >= timeoutSeconds) {
            inactive = true;
        }

        // Save state every 10 seconds
        if (elapsedSeconds % 10 == 0) {
            saveState();
        }
    }

    public void pause() {
        if (!paused) {
            paused = true;
            pausedPurse = currentPurse;
            pausedGain = currentPurse - startPurse;
        }
    }

    public void unpause() {
        if (paused) {
            paused = false;
            // Adjust startPurse to ignore changes during pause
            long delta = currentPurse - pausedPurse;
            startPurse += delta;
        }
    }

    public void togglePause() {
        if (paused) {
            unpause();
        } else {
            pause();
        }
    }

    public void setElapsedSeconds(int seconds) {
        this.elapsedSeconds = seconds;
    }

    public void setGain(long gain) {
        this.startPurse = currentPurse - gain;
    }

    public long getGained() {
        // Return frozen gain when paused
        if (paused) {
            return pausedGain + savedGainOffset;
        }
        return currentPurse - startPurse + savedGainOffset;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isInactive() {
        return inactive;
    }

    public long getCurrentPurse() {
        return currentPurse;
    }
}
