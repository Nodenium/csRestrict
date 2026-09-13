package com.nodenium.csrestrict.mixin;

import com.nodenium.csrestrict.CarpetCompat;
import com.nodenium.csrestrict.RestrictionUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Strips real players out of tab-list packets sent to a restricted spectator; Carpet fake players and self stay visible. */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class TabListFilterMixin {

	@ModifyVariable(
			method = {
					"send(Lnet/minecraft/network/protocol/Packet;)V",
					"send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V"
			},
			at = @At("HEAD"), argsOnly = true)
	private Packet<?> csrestrict$filter(Packet<?> packet) {
		if (!(packet instanceof ClientboundPlayerInfoUpdatePacket infoPacket)) {
			return packet;
		}
		if (!(((Object) this) instanceof ServerGamePacketListenerImplAccessor accessor)) {
			return packet;
		}

		ServerPlayer viewer = accessor.csrestrict$getPlayer();
		if (viewer == null || !RestrictionUtil.isRestrictedSpectator(viewer)) {
			return packet;
		}

		Map<UUID, ServerPlayer> online = viewer.level().getServer().getPlayerList().getPlayersByUUID();

		List<ServerPlayer> kept = new ArrayList<>(infoPacket.entries().size());
		boolean anyHidden = false;
		for (ClientboundPlayerInfoUpdatePacket.Entry entry : infoPacket.entries()) {
			ServerPlayer entryPlayer = online.get(entry.profileId());
			if (entryPlayer == null) {
				continue;
			}
			boolean hide = entryPlayer != viewer && !CarpetCompat.isFakePlayer(entryPlayer);
			if (hide) {
				anyHidden = true;
			} else {
				kept.add(entryPlayer);
			}
		}

		if (!anyHidden) {
			return packet;
		}

		return ClientboundPlayerInfoUpdatePacketAccessor.csrestrict$create(infoPacket.actions(), kept);
	}
}
