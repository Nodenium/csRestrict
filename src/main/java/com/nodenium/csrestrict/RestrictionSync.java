package com.nodenium.csrestrict;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TabListFilterMixin only filters packets as they're sent, so a player's own already-known
 * tab-list entries don't get cleaned up on entering restricted mode, and any update dropped
 * while restricted leaves that entry stale (wrong gamemode icon, wrong spectator-style
 * render) until something unrelated refreshes it. This tracks restricted state per player
 * and pushes a corrective packet on each transition to fix both.
 */
public final class RestrictionSync {
	private static final Set<UUID> RESTRICTED = ConcurrentHashMap.newKeySet();

	private RestrictionSync() {
	}

	public static void onGameModeChanged(ServerPlayer player) {
		UUID id = player.getUUID();
		boolean restrictedNow = RestrictionUtil.isRestrictedSpectator(player);
		boolean wasRestricted = restrictedNow ? !RESTRICTED.add(id) : RESTRICTED.remove(id);

		if (restrictedNow && !wasRestricted) {
			hideExistingRealPlayers(player);
		} else if (!restrictedNow && wasRestricted) {
			resyncFullList(player);
		}
	}

	public static void onDisconnect(ServerPlayer player) {
		RESTRICTED.remove(player.getUUID());
	}

	private static void hideExistingRealPlayers(ServerPlayer viewer) {
		List<UUID> toRemove = viewer.level().getServer().getPlayerList().getPlayers().stream()
				.filter(p -> p != viewer && !CarpetCompat.isFakePlayer(p))
				.map(ServerPlayer::getUUID)
				.toList();
		if (!toRemove.isEmpty()) {
			viewer.connection.send(new ClientboundPlayerInfoRemovePacket(toRemove));
		}
	}

	private static void resyncFullList(ServerPlayer viewer) {
		viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
				viewer.level().getServer().getPlayerList().getPlayers()));
	}
}
