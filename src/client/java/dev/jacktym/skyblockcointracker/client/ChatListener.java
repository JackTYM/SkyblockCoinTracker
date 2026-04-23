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
