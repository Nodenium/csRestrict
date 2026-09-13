package com.nodenium.csrestrict;

import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Soft-dependency detection of Carpet fake players - no compile/runtime dependency on
 * fabric-carpet. Matches carpet.patches.EntityPlayerMPFake by name, falling back to its
 * isAShadow marker field for relocated forks.
 */
public final class CarpetCompat {
	private static final String FAKE_PLAYER_CLASS = "carpet.patches.EntityPlayerMPFake";
	private static final String SHADOW_FIELD = "isAShadow";

	private static final Map<Class<?>, Boolean> CACHE = new ConcurrentHashMap<>();

	private CarpetCompat() {
	}

	public static boolean isFakePlayer(ServerPlayer player) {
		return CACHE.computeIfAbsent(player.getClass(), CarpetCompat::detect);
	}

	private static boolean detect(Class<?> clazz) {
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			if (FAKE_PLAYER_CLASS.equals(c.getName())) {
				return true;
			}
			try {
				Field field = c.getDeclaredField(SHADOW_FIELD);
				if (field.getType() == boolean.class) {
					return true;
				}
			} catch (NoSuchFieldException ignored) {
			}
		}
		return false;
	}
}
