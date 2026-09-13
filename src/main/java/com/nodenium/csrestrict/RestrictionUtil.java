package com.nodenium.csrestrict;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

public final class RestrictionUtil {
	private RestrictionUtil() {
	}

	public static boolean isOp(ServerPlayer player) {
		return player.level().getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
	}

	/** Live state, not command-name based - can't be bypassed by renaming or aliasing /cs. */
	public static boolean isRestrictedSpectator(ServerPlayer player) {
		return player.isSpectator() && !isOp(player);
	}
}
