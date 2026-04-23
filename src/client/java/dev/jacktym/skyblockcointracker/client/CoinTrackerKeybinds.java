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
