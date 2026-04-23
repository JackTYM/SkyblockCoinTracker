package dev.jacktym.skyblockcointracker.client.overlay;

import dev.jacktym.skyblockcointracker.client.CoinTracker;
import dev.jacktym.skyblockcointracker.client.CoinTrackerConfig;
import dev.jacktym.skyblockcointracker.client.CoinTrackerState;
import dev.jacktym.skyblockcointracker.client.util.FormatUtil;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class CoinOverlay implements HudRenderCallback {
    public static final CoinOverlay INSTANCE = new CoinOverlay();

    // Formatting codes
    private static final int GOLD = 0xFFAA00;
    private static final int WHITE = 0xFFFFFF;
    private static final int RED = 0xFF5555;

    private CoinOverlay() {}

    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        if (!config.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        CoinTrackerState state = CoinTracker.getInstance().getState();
        TextRenderer textRenderer = client.textRenderer;

        int x = config.getOverlayX();
        int y = config.getOverlayY();
        float scale = config.getScale();

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        // Line 1: Gained
        long gained = state.getGained();
        String gainedText = FormatUtil.formatCoins(gained);
        drawLine(context, textRenderer, 0, 0, "Gained: ", gainedText + " coins");

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
        drawLine(context, textRenderer, 0, 10, "Time: ", timeText, timeColor);

        // Line 3: Rate
        String rateText = FormatUtil.formatRate(gained, state.getElapsedSeconds()) + " coins/hr";
        drawLine(context, textRenderer, 0, 20, "Rate: ", rateText);

        context.getMatrices().pop();
    }

    private void drawLine(DrawContext context, TextRenderer textRenderer, int x, int y, String label, String value) {
        drawLine(context, textRenderer, x, y, label, value, WHITE);
    }

    private void drawLine(DrawContext context, TextRenderer textRenderer, int x, int y, String label, String value, int valueColor) {
        // Draw label in gold bold
        context.drawTextWithShadow(textRenderer, label, x, y, GOLD);
        int labelWidth = textRenderer.getWidth(label);
        // Draw value
        context.drawTextWithShadow(textRenderer, value, x + labelWidth, y, valueColor);
    }

    public int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        // Approximate width based on longest expected line
        return textRenderer.getWidth("Gained: 999,999,999 coins");
    }

    public int getHeight() {
        return 30; // 3 lines * 10 pixels
    }
}
