package com.nodenium.csrestrict.mixin;

import com.nodenium.csrestrict.CarpetCompat;
import com.nodenium.csrestrict.CsRestrictMod;
import com.nodenium.csrestrict.RestrictionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Blocks the spectator "jump to player" teleport packet for restricted non-op spectators. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class TeleportBlockMixin {

	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleTeleportToEntityPacket", at = @At("HEAD"), cancellable = true)
	private void csrestrict$blockRealPlayerTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo ci) {
		ServerPlayer requester = this.player;
		if (requester == null || !RestrictionUtil.isRestrictedSpectator(requester)) {
			return;
		}

		Entity target = null;
		for (ServerLevel level : requester.level().getServer().getAllLevels()) {
			target = packet.getEntity(level);
			if (target != null) {
				break;
			}
		}

		if (target instanceof ServerPlayer targetPlayer
				&& targetPlayer != requester
				&& !CarpetCompat.isFakePlayer(targetPlayer)) {
			ci.cancel();
			requester.sendSystemMessage(Component.literal(
					"§cYou can't spectate-teleport to real players while in restricted spectator mode."));
			CsRestrictMod.LOGGER.debug("Blocked spectator teleport: {} -> {}",
					requester.getGameProfile().name(), targetPlayer.getGameProfile().name());
		}
	}
}
