package dev.jacktym.skyblockcointracker.client.overlay;

import dev.jacktym.skyblockcointracker.client.CoinTrackerConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OverlayEditScreen extends Screen {
    private int overlayX;
    private int overlayY;

    public OverlayEditScreen() {
        super(Component.literal("Edit Overlay Position"));
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        this.overlayX = config.getOverlayX();
        this.overlayY = config.getOverlayY();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw overlay preview with border
        int overlayWidth = CoinOverlay.INSTANCE.getWidth();
        int overlayHeight = CoinOverlay.INSTANCE.getHeight();

        // Border
        context.fill(overlayX - 2, overlayY - 2, overlayX + overlayWidth + 2, overlayY + overlayHeight + 2, 0xFFFFFFFF);
        context.fill(overlayX - 1, overlayY - 1, overlayX + overlayWidth + 1, overlayY + overlayHeight + 1, 0xFF000000);

        // Draw placeholder text
        context.drawString(this.font, "Gained: 1,234,567 coins", overlayX, overlayY, 0xFFAA00, true);
        context.drawString(this.font, "Time: 12:34", overlayX, overlayY + 10, 0xFFAA00, true);
        context.drawString(this.font, "Rate: 2,469,134 coins/hr", overlayX, overlayY + 20, 0xFFAA00, true);

        // Instructions
        String instructions = "Use /ct pos <x> <y> to set position. Press Escape to close.";
        int textWidth = this.font.width(instructions);
        context.drawString(this.font, instructions, (this.width - textWidth) / 2, this.height - 30, 0xFFFFFF, true);

        // Position display
        String posText = String.format("Current Position: %d, %d", overlayX, overlayY);
        context.drawString(this.font, posText, 10, 10, 0xFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
