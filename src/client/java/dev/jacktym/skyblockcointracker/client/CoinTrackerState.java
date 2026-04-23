package dev.jacktym.skyblockcointracker.client;

public class CoinTrackerState {
    private long startPurse = 0;
    private long currentPurse = 0;
    private long pausedPurse = 0;
    private int elapsedSeconds = 0;
    private int secondsSinceChange = 0;
    private boolean paused = false;
    private boolean inactive = false;

    public void reset() {
        startPurse = currentPurse;
        elapsedSeconds = 0;
        secondsSinceChange = 0;
        paused = false;
        inactive = false;
    }

    public void updatePurse(long newPurse) {
        if (paused) return;

        if (currentPurse != newPurse) {
            if (inactive) {
                // Was inactive, now active again - reset
                startPurse = newPurse;
                elapsedSeconds = 0;
                inactive = false;
            }
            secondsSinceChange = 0;
        }
        currentPurse = newPurse;

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
    }

    public void pause() {
        if (!paused) {
            paused = true;
            pausedPurse = currentPurse;
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
        return currentPurse - startPurse;
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
