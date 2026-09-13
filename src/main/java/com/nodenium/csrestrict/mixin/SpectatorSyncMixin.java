package com.nodenium.csrestrict.mixin;

import com.nodenium.csrestrict.RestrictionSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Triggers RestrictionSync whenever a gamemode change actually succeeds. */
@Mixin(ServerPlayer.class)
public abstract class SpectatorSyncMixin {

	@Inject(method = "setGameMode", at = @At("RETURN"))
	private void csrestrict$onGameModeChanged(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			RestrictionSync.onGameModeChanged((ServerPlayer) (Object) this);
		}
	}
}
