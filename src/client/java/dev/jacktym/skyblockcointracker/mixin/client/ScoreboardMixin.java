package dev.jacktym.skyblockcointracker.mixin.client;

import dev.jacktym.skyblockcointracker.client.CoinTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ScoreboardMixin {

    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void onScoreboardScoreUpdate(ClientboundSetScorePacket packet, CallbackInfo ci) {
        // Read the sidebar scoreboard directly
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        Scoreboard scoreboard = client.level.getScoreboard();
        Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) return;

        // Iterate through all scores in the sidebar
        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(sidebar)) {
            String owner = entry.owner();
            PlayerTeam team = scoreboard.getPlayersTeam(owner);

            // Build the display line from team prefix + owner + suffix
            String line;
            if (team != null) {
                String prefix = team.getPlayerPrefix().getString();
                String suffix = team.getPlayerSuffix().getString();
                line = prefix + owner + suffix;
            } else {
                line = owner;
            }

            CoinTracker.getInstance().onScoreboardUpdate(line);
        }
    }
}
