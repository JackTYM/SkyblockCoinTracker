package dev.jacktym.skyblockcointracker.client.overlay;

import dev.jacktym.skyblockcointracker.client.CoinTracker;
import dev.jacktym.skyblockcointracker.client.CoinTrackerConfig;
import dev.jacktym.skyblockcointracker.client.CoinTrackerState;
import dev.jacktym.skyblockcointracker.client.util.FormatUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CoinOverlay implements HudRenderCallback {
    public static final CoinOverlay INSTANCE = new CoinOverlay();

    // Colors in ARGB format (alpha must be 0xFF for full opacity)
    private static final int GOLD = 0xFFFFAA00;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int RED = 0xFFFF5555;

    private CoinOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);
    }

    @Override
    public void onHudRender(GuiGraphics context, DeltaTracker tickCounter) {
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        if (!config.isEnabled()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.options.hideGui) return;

        CoinTrackerState state = CoinTracker.getInstance().getState();
        Font font = client.font;

        int x = config.getOverlayX();
        int y = config.getOverlayY();

        // Line 1: Gained
        long gained = state.getGained();
        String gainedText = FormatUtil.formatCoins(gained);
        drawLine(context, font, x, y, "Gained: ", gainedText + " coins");

        // Line 2: Time
        String timeText;
        int timeColor = WHITE;
        if (state.isInactive()) {
            timeText = "Inactive";
            timeColor = RED;
        } else if (state.isPaused()) {
            timeText = FormatUtil.formatTime(state.getElapsedSeconds()) + " (Paused)";
        } else {
            timeText = FormatUtil.formatTime(state.getElapsedSeconds());
        }
        drawLine(context, font, x, y + 10, "Time: ", timeText, timeColor);

        // Line 3: Rate
        String rateText = FormatUtil.formatRate(gained, state.getElapsedSeconds()) + " coins/hr";
        drawLine(context, font, x, y + 20, "Rate: ", rateText);
    }

    private void drawLine(GuiGraphics context, Font font, int x, int y, String label, String value) {
        drawLine(context, font, x, y, label, value, WHITE);
    }

    private void drawLine(GuiGraphics context, Font font, int x, int y, String label, String value, int valueColor) {
        // Draw label in gold bold
        context.drawString(font, label, x, y, GOLD, true);
        int labelWidth = font.width(label);
        // Draw value
        context.drawString(font, value, x + labelWidth, y, valueColor, true);
    }

    public int getWidth() {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        // Approximate width based on longest expected line
        return font.width("Gained: 999,999,999 coins");
    }

    public int getHeight() {
        return 30; // 3 lines * 10 pixels
    }
}
