package dev.jacktym.skyblockcointracker.mixin.client;

import dev.jacktym.skyblockcointracker.client.CoinTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ScoreboardMixin {
    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void onScoreboardScoreUpdate(ClientboundSetScorePacket packet, CallbackInfo ci) {
        String scoreName = packet.owner();
        CoinTracker.getInstance().onScoreboardUpdate(scoreName);
    }
}
