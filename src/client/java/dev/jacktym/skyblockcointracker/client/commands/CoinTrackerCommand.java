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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public class CoinTrackerCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(CoinTrackerCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
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
                    Minecraft.getInstance().execute(() ->
                        Minecraft.getInstance().setScreen(new OverlayEditScreen()));
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
            Component.literal("[CoinTracker] ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE))
        );
    }
}
