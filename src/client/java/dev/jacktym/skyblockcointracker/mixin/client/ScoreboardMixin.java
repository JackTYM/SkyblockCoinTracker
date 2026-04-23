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
