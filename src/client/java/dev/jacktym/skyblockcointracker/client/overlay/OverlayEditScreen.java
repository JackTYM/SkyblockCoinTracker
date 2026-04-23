package dev.jacktym.skyblockcointracker.client.overlay;

import dev.jacktym.skyblockcointracker.client.CoinTrackerConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class OverlayEditScreen extends Screen {
    private int overlayX;
    private int overlayY;
    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    public OverlayEditScreen() {
        super(Text.literal("Edit Overlay Position"));
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        this.overlayX = config.getOverlayX();
        this.overlayY = config.getOverlayY();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw overlay preview with border
        int overlayWidth = CoinOverlay.INSTANCE.getWidth();
        int overlayHeight = CoinOverlay.INSTANCE.getHeight();
        float scale = CoinTrackerConfig.getInstance().getScale();
        int scaledWidth = (int)(overlayWidth * scale);
        int scaledHeight = (int)(overlayHeight * scale);

        // Border
        context.fill(overlayX - 2, overlayY - 2, overlayX + scaledWidth + 2, overlayY + scaledHeight + 2, 0xFFFFFFFF);
        context.fill(overlayX - 1, overlayY - 1, overlayX + scaledWidth + 1, overlayY + scaledHeight + 1, 0xFF000000);

        // Render the actual overlay at this position
        CoinTrackerConfig config = CoinTrackerConfig.getInstance();
        int originalX = config.getOverlayX();
        int originalY = config.getOverlayY();
        config.setOverlayPosition(overlayX, overlayY);

        // Let HUD render handle it - we'll draw placeholder text instead
        context.getMatrices().push();
        context.getMatrices().translate(overlayX, overlayY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(this.textRenderer, "Gained: 1,234,567 coins", 0, 0, 0xFFAA00);
        context.drawTextWithShadow(this.textRenderer, "Time: 12:34", 0, 10, 0xFFAA00);
        context.drawTextWithShadow(this.textRenderer, "Rate: 2,469,134 coins/hr", 0, 20, 0xFFAA00);
        context.getMatrices().pop();

        // Instructions
        String instructions = "Drag overlay or use Arrow Keys (Shift for 10px). Press Escape to save.";
        int textWidth = this.textRenderer.getWidth(instructions);
        context.drawTextWithShadow(this.textRenderer, instructions, (this.width - textWidth) / 2, this.height - 20, 0xFFFFFF);

        // Position display
        String posText = String.format("Position: %d, %d", overlayX, overlayY);
        context.drawTextWithShadow(this.textRenderer, posText, 10, 10, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int overlayWidth = (int)(CoinOverlay.INSTANCE.getWidth() * CoinTrackerConfig.getInstance().getScale());
            int overlayHeight = (int)(CoinOverlay.INSTANCE.getHeight() * CoinTrackerConfig.getInstance().getScale());

            if (mouseX >= overlayX && mouseX <= overlayX + overlayWidth &&
                mouseY >= overlayY && mouseY <= overlayY + overlayHeight) {
                dragging = true;
                dragOffsetX = (int)mouseX - overlayX;
                dragOffsetY = (int)mouseY - overlayY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            overlayX = (int)mouseX - dragOffsetX;
            overlayY = (int)mouseY - dragOffsetY;
            clampPosition();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int step = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? 10 : 1;

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> { overlayY -= step; clampPosition(); return true; }
            case GLFW.GLFW_KEY_DOWN -> { overlayY += step; clampPosition(); return true; }
            case GLFW.GLFW_KEY_LEFT -> { overlayX -= step; clampPosition(); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { overlayX += step; clampPosition(); return true; }
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER -> {
                saveAndClose();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clampPosition() {
        int overlayWidth = (int)(CoinOverlay.INSTANCE.getWidth() * CoinTrackerConfig.getInstance().getScale());
        int overlayHeight = (int)(CoinOverlay.INSTANCE.getHeight() * CoinTrackerConfig.getInstance().getScale());

        overlayX = Math.max(0, Math.min(overlayX, this.width - overlayWidth));
        overlayY = Math.max(0, Math.min(overlayY, this.height - overlayHeight));
    }

    private void saveAndClose() {
        CoinTrackerConfig.getInstance().setOverlayPosition(overlayX, overlayY);
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
