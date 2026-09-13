package com.nodenium.csrestrict.mixin;

import com.nodenium.csrestrict.CsRestrictMod;
import com.nodenium.csrestrict.RestrictionConfig;
import com.nodenium.csrestrict.RestrictionUtil;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Debug-logs watched command usage (e.g. /cs). Diagnostic only; doesn't gate enforcement. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CommandDetectionMixin {

	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleChatCommand", at = @At("HEAD"))
	private void csrestrict$detectCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
		ServerPlayer sender = this.player;
		if (sender == null) {
			return;
		}

		String command = packet.command();
		String root = command.split(" ", 2)[0];

		if (RestrictionConfig.get().watchedCommands.stream().anyMatch(root::equalsIgnoreCase)) {
			CsRestrictMod.LOGGER.debug("{} ran /{} (currentlySpectator={}, op={})",
					sender.getGameProfile().name(), command, sender.isSpectator(), RestrictionUtil.isOp(sender));
		}
	}
}
