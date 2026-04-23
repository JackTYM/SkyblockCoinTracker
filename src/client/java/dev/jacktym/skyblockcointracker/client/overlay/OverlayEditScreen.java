package dev.jacktym.skyblockcointracker.client.overlay;

import dev.jacktym.skyblockcointracker.client.CoinTrackerConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class OverlayEditScreen extends Screen {
    private int overlayX;
    private int overlayY;
    private boolean dragging = false;
    private int lastMouseX;
    private int lastMouseY;

    public OverlayEditScreen() {
        super(Component.literal("Edit Overlay Position"));
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        this.overlayX = config.getOverlayX();
        this.overlayY = config.getOverlayY();
    }

    private boolean isMouseOverOverlay(int mouseX, int mouseY) {
        int overlayWidth = CoinOverlay.INSTANCE.getWidth();
        int overlayHeight = CoinOverlay.INSTANCE.getHeight();
        return mouseX >= overlayX && mouseX <= overlayX + overlayWidth
            && mouseY >= overlayY && mouseY <= overlayY + overlayHeight;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();

        if (button == 0) {
            if (isMouseOverOverlay(mouseX, mouseY)) {
                // Start dragging
                dragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            } else if (dragging) {
                // Click outside while dragging - stop and save
                dragging = false;
                CoinTrackerConfig.getInstance().setOverlayPosition(overlayX, overlayY);
                return true;
            }
        }
        return super.mouseClicked(event, flag);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging) {
            dragging = false;
            CoinTrackerConfig.getInstance().setOverlayPosition(overlayX, overlayY);
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (dragging) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();

            overlayX += mouseX - lastMouseX;
            overlayY += mouseY - lastMouseY;

            // Clamp to screen bounds
            overlayX = Math.max(0, Math.min(overlayX, this.width - CoinOverlay.INSTANCE.getWidth()));
            overlayY = Math.max(0, Math.min(overlayY, this.height - CoinOverlay.INSTANCE.getHeight()));

            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw overlay preview with border
        int overlayWidth = CoinOverlay.INSTANCE.getWidth();
        int overlayHeight = CoinOverlay.INSTANCE.getHeight();

        // Border (highlight green if mouse is over or dragging)
        int borderColor = (isMouseOverOverlay(mouseX, mouseY) || dragging) ? 0xFF00FF00 : 0xFFFFFFFF;
        context.fill(overlayX - 2, overlayY - 2, overlayX + overlayWidth + 2, overlayY + overlayHeight + 2, borderColor);
        context.fill(overlayX - 1, overlayY - 1, overlayX + overlayWidth + 1, overlayY + overlayHeight + 1, 0xFF000000);

        // Draw placeholder text (colors need full ARGB with alpha=0xFF)
        context.drawString(this.font, "Gained: 1,234,567 coins", overlayX, overlayY, 0xFFFFAA00, true);
        context.drawString(this.font, "Time: 12:34", overlayX, overlayY + 10, 0xFFFFAA00, true);
        context.drawString(this.font, "Rate: 2,469,134 coins/hr", overlayX, overlayY + 20, 0xFFFFAA00, true);

        // Instructions
        String instructions = dragging
            ? "Release to place. Escape to cancel."
            : "Click and drag the overlay. Escape to close.";
        int textWidth = this.font.width(instructions);
        context.drawString(this.font, instructions, (this.width - textWidth) / 2, this.height - 30, 0xFFFFFFFF, true);

        // Position display
        String posText = String.format("Position: %d, %d", overlayX, overlayY);
        context.drawString(this.font, posText, 10, 10, 0xFFFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        // Save position when closing
        CoinTrackerConfig.getInstance().setOverlayPosition(overlayX, overlayY);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
