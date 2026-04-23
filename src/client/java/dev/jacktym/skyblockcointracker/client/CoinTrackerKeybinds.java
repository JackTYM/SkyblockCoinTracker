package dev.jacktym.skyblockcointracker.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class CoinTrackerKeybinds {
    private static KeyMapping toggleKey;
    private static KeyMapping togglePauseKey;

    public static void register() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.cointracker.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MISC
        ));

        togglePauseKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.cointracker.togglepause",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                CoinTracker.getInstance().toggle();
            }
            while (togglePauseKey.consumeClick()) {
                CoinTracker.getInstance().togglePause();
            }
        });
    }
}
