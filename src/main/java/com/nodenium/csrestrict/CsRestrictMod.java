package com.nodenium.csrestrict;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsRestrictMod implements ModInitializer {
	public static final String MOD_ID = "csrestrict";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RestrictionConfig config = RestrictionConfig.get();
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> RestrictionSync.onDisconnect(handler.player));
		LOGGER.info("csRestrict loaded - non-op players in spectator mode can no longer teleport-view or see "
				+ "real players in the tab list (Carpet fake players are unaffected). Watched commands: {}",
				config.watchedCommands);
	}
}
