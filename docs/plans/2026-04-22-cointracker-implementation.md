# Coin Tracker Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a Hypixel Skyblock Fabric mod that tracks coin profits/losses over time with a movable HUD overlay.

**Architecture:** Client-side only mod using a mixin to intercept scoreboard packets for coin tracking, Fabric API events for chat/tick/render, and a simple JSON config for persistence. State is held in memory and resets on game restart.

**Tech Stack:** Fabric 1.21.11, Java 21, Gson (bundled), Fabric API (keybinds, events, commands)

---

### Task 1: Utility Classes

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/util/FormatUtil.java`

**Step 1: Create FormatUtil with number and time formatting**

```java
package dev.jacktym.skyblockcointracker.client.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtil {
    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));

    public static String formatCoins(long coins) {
        return COMMA_FORMAT.format(coins);
    }

    public static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public static String formatRate(long gained, int seconds) {
        if (seconds <= 0) return "0";
        long rate = (gained * 3600) / seconds;
        return COMMA_FORMAT.format(rate);
    }
}
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/util/FormatUtil.java
git commit -m "feat: add FormatUtil for number and time formatting"
```

---

### Task 2: State Management

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/CoinTrackerState.java`

**Step 1: Create CoinTrackerState class**

```java
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
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/CoinTrackerState.java
git commit -m "feat: add CoinTrackerState for tracking purse changes"
```

---

### Task 3: Config System

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/CoinTrackerConfig.java`

**Step 1: Create CoinTrackerConfig with Gson serialization**

```java
package dev.jacktym.skyblockcointracker.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CoinTrackerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cointracker.json");

    private boolean enabled = true;
    private int overlayX = 10;
    private int overlayY = 10;
    private float scale = 1.0f;
    private int timeoutMinutes = 5;

    private static CoinTrackerConfig instance;

    public static CoinTrackerConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static CoinTrackerConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                CoinTrackerConfig config = GSON.fromJson(json, CoinTrackerConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CoinTrackerConfig config = new CoinTrackerConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; save(); }

    public int getOverlayX() { return overlayX; }
    public void setOverlayX(int x) { this.overlayX = x; save(); }

    public int getOverlayY() { return overlayY; }
    public void setOverlayY(int y) { this.overlayY = y; save(); }

    public void setOverlayPosition(int x, int y) {
        this.overlayX = x;
        this.overlayY = y;
        save();
    }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; save(); }

    public int getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(int minutes) { this.timeoutMinutes = minutes; save(); }

    public int getTimeoutSeconds() { return timeoutMinutes * 60; }
}
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/CoinTrackerConfig.java
git commit -m "feat: add CoinTrackerConfig with JSON persistence"
```

---

### Task 4: Core Tracker Logic

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/CoinTracker.java`

**Step 1: Create CoinTracker singleton**

```java
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
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/CoinTracker.java
git commit -m "feat: add CoinTracker core logic"
```

---

### Task 5: Scoreboard Mixin

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/mixin/client/ScoreboardMixin.java`
- Modify: `src/client/resources/skyblockcointracker.client.mixins.json`

**Step 1: Create ScoreboardMixin to intercept score updates**

```java
package dev.jacktym.skyblockcointracker.mixin.client;

import dev.jacktym.skyblockcointracker.client.CoinTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ScoreboardMixin {
    @Inject(method = "onScoreboardScoreUpdate", at = @At("TAIL"))
    private void onScoreboardScoreUpdate(ScoreboardScoreUpdateS2CPacket packet, CallbackInfo ci) {
        String scoreName = packet.scoreHolderName();
        CoinTracker.getInstance().onScoreboardUpdate(scoreName);
    }
}
```

**Step 2: Register mixin in config**

Update `src/client/resources/skyblockcointracker.client.mixins.json`:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "dev.jacktym.skyblockcointracker.mixin.client",
  "compatibilityLevel": "JAVA_21",
  "client": [
    "ScoreboardMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  },
  "overwrites": {
    "requireAnnotations": true
  }
}
```

**Step 3: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/mixin/client/ScoreboardMixin.java
git add src/client/resources/skyblockcointracker.client.mixins.json
git commit -m "feat: add ScoreboardMixin to intercept purse updates"
```

---

### Task 6: Overlay Rendering

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/overlay/CoinOverlay.java`

**Step 1: Create CoinOverlay HUD renderer**

```java
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
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/overlay/CoinOverlay.java
git commit -m "feat: add CoinOverlay HUD renderer"
```

---

### Task 7: Edit Mode Screen

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/overlay/OverlayEditScreen.java`

**Step 1: Create OverlayEditScreen with drag and arrow key support**

```java
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
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/overlay/OverlayEditScreen.java
git commit -m "feat: add OverlayEditScreen for drag and arrow key positioning"
```

---

### Task 8: Commands

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/commands/CoinTrackerCommand.java`

**Step 1: Create CoinTrackerCommand with all subcommands**

```java
package dev.jacktym.skyblockcointracker.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import dev.jacktym.skyblockcointracker.client.CoinTracker;
import dev.jacktym.skyblockcointracker.client.CoinTrackerConfig;
import dev.jacktym.skyblockcointracker.client.overlay.OverlayEditScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CoinTrackerCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(CoinTrackerCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        var ctCommand = ClientCommandManager.literal("ct")
            .then(ClientCommandManager.literal("on")
                .executes(ctx -> { CoinTracker.getInstance().enable(); return 1; }))
            .then(ClientCommandManager.literal("off")
                .executes(ctx -> { CoinTracker.getInstance().disable(); return 1; }))
            .then(ClientCommandManager.literal("toggle")
                .executes(ctx -> { CoinTracker.getInstance().toggle(); return 1; }))
            .then(ClientCommandManager.literal("pause")
                .executes(ctx -> { CoinTracker.getInstance().pause(); return 1; }))
            .then(ClientCommandManager.literal("unpause")
                .executes(ctx -> { CoinTracker.getInstance().unpause(); return 1; }))
            .then(ClientCommandManager.literal("togglepause")
                .executes(ctx -> { CoinTracker.getInstance().togglePause(); return 1; }))
            .then(ClientCommandManager.literal("reset")
                .executes(ctx -> { CoinTracker.getInstance().reset(); return 1; }))
            .then(ClientCommandManager.literal("move")
                .executes(ctx -> {
                    MinecraftClient.getInstance().send(() -> 
                        MinecraftClient.getInstance().setScreen(new OverlayEditScreen()));
                    return 1;
                }))
            .then(ClientCommandManager.literal("pos")
                .then(ClientCommandManager.argument("x", IntegerArgumentType.integer(0))
                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int x = IntegerArgumentType.getInteger(ctx, "x");
                            int y = IntegerArgumentType.getInteger(ctx, "y");
                            CoinTrackerConfig.getInstance().setOverlayPosition(x, y);
                            sendFeedback(ctx.getSource(), "Position set to " + x + ", " + y);
                            return 1;
                        }))))
            .then(ClientCommandManager.literal("scale")
                .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0.1f, 5.0f))
                    .executes(ctx -> {
                        float scale = FloatArgumentType.getFloat(ctx, "value");
                        CoinTrackerConfig.getInstance().setScale(scale);
                        sendFeedback(ctx.getSource(), "Scale set to " + scale);
                        return 1;
                    })))
            .then(ClientCommandManager.literal("timeout")
                .then(ClientCommandManager.argument("minutes", IntegerArgumentType.integer(1, 60))
                    .executes(ctx -> {
                        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                        CoinTrackerConfig.getInstance().setTimeoutMinutes(minutes);
                        sendFeedback(ctx.getSource(), "Timeout set to " + minutes + " minutes");
                        return 1;
                    })))
            .then(ClientCommandManager.literal("settime")
                .then(ClientCommandManager.argument("minutes", IntegerArgumentType.integer(0))
                    .executes(ctx -> {
                        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
                        CoinTracker.getInstance().setTime(minutes);
                        return 1;
                    })))
            .then(ClientCommandManager.literal("setgain")
                .then(ClientCommandManager.argument("amount", LongArgumentType.longArg())
                    .executes(ctx -> {
                        long amount = LongArgumentType.getLong(ctx, "amount");
                        CoinTracker.getInstance().setGain(amount);
                        return 1;
                    })))
            .executes(ctx -> {
                // Default: show help
                sendFeedback(ctx.getSource(), "Commands: on, off, toggle, pause, unpause, togglepause, reset, move, pos, scale, timeout, settime, setgain");
                return 1;
            });

        dispatcher.register(ctCommand);
        dispatcher.register(ClientCommandManager.literal("cointracker").redirect(ctCommand.build()));
    }

    private static void sendFeedback(FabricClientCommandSource source, String message) {
        source.sendFeedback(
            Text.literal("[CoinTracker] ").formatted(Formatting.GOLD)
                .append(Text.literal(message).formatted(Formatting.WHITE))
        );
    }
}
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/commands/CoinTrackerCommand.java
git commit -m "feat: add CoinTrackerCommand with all subcommands"
```

---

### Task 9: Keybinds

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/CoinTrackerKeybinds.java`

**Step 1: Create CoinTrackerKeybinds**

```java
package dev.jacktym.skyblockcointracker.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CoinTrackerKeybinds {
    private static KeyBinding toggleKey;
    private static KeyBinding togglePauseKey;

    public static void register() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cointracker.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
            "category.cointracker"
        ));

        togglePauseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cointracker.togglepause",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN, // Unbound by default
            "category.cointracker"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                CoinTracker.getInstance().toggle();
            }
            while (togglePauseKey.wasPressed()) {
                CoinTracker.getInstance().togglePause();
            }
        });
    }
}
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/CoinTrackerKeybinds.java
git commit -m "feat: add keybinds for toggle and togglepause"
```

---

### Task 10: Chat Listener for Profile Switch

**Files:**
- Create: `src/client/java/dev/jacktym/skyblockcointracker/client/ChatListener.java`

**Step 1: Create ChatListener**

```java
package dev.jacktym.skyblockcointracker.client;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

public class ChatListener {
    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            if (text.contains("Switching to profile")) {
                CoinTracker.getInstance().reset();
            }
        });
    }
}
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/ChatListener.java
git commit -m "feat: add ChatListener for profile switch detection"
```

---

### Task 11: Client Entry Point

**Files:**
- Modify: `src/client/java/dev/jacktym/skyblockcointracker/client/SkyblockcointrackerClient.java`

**Step 1: Wire everything together in the client entry point**

```java
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
```

**Step 2: Commit**

```bash
git add src/client/java/dev/jacktym/skyblockcointracker/client/SkyblockcointrackerClient.java
git commit -m "feat: wire up all components in client entry point"
```

---

### Task 12: Language File

**Files:**
- Create: `src/client/resources/assets/skyblockcointracker/lang/en_us.json`

**Step 1: Create language file for keybind names**

```json
{
  "category.cointracker": "Coin Tracker",
  "key.cointracker.toggle": "Toggle Tracker",
  "key.cointracker.togglepause": "Toggle Pause"
}
```

**Step 2: Commit**

```bash
mkdir -p src/client/resources/assets/skyblockcointracker/lang
git add src/client/resources/assets/skyblockcointracker/lang/en_us.json
git commit -m "feat: add language file for keybind names"
```

---

### Task 13: Build and Test

**Step 1: Build the mod**

```bash
./gradlew build
```

Expected: BUILD SUCCESSFUL

**Step 2: Test in game**

1. Launch Minecraft with the mod
2. Join Hypixel Skyblock
3. Verify overlay appears with Gained/Time/Rate
4. Test commands: `/ct off`, `/ct on`, `/ct move`, `/ct reset`
5. Test keybinds in Options > Controls > Coin Tracker
6. Verify profile switch resets tracker

**Step 3: Final commit**

```bash
git add -A
git commit -m "chore: finalize coin tracker mod v1.0"
```

---

## Summary

11 implementation tasks + build/test:

1. FormatUtil (number/time formatting)
2. CoinTrackerState (state management)
3. CoinTrackerConfig (JSON persistence)
4. CoinTracker (core logic)
5. ScoreboardMixin (intercept purse updates)
6. CoinOverlay (HUD rendering)
7. OverlayEditScreen (drag/arrow positioning)
8. CoinTrackerCommand (all /ct commands)
9. CoinTrackerKeybinds (toggle/pause keybinds)
10. ChatListener (profile switch detection)
11. SkyblockcointrackerClient (wire everything)
12. Language file (keybind names)
13. Build and test
