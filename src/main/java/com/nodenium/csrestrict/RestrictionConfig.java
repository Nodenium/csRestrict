package com.nodenium.csrestrict;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Loaded from config/csrestrict.json. watchedCommands only controls logging; see RestrictionUtil for enforcement. */
public final class RestrictionConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile RestrictionConfig instance;

	public List<String> watchedCommands = List.of("cs");

	public static RestrictionConfig get() {
		RestrictionConfig result = instance;
		if (result == null) {
			synchronized (RestrictionConfig.class) {
				result = instance;
				if (result == null) {
					instance = result = load();
				}
			}
		}
		return result;
	}

	private static RestrictionConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("csrestrict.json");
		try {
			if (Files.exists(path)) {
				RestrictionConfig loaded = GSON.fromJson(Files.readString(path), RestrictionConfig.class);
				if (loaded != null) {
					return loaded;
				}
			}
		} catch (IOException e) {
			CsRestrictMod.LOGGER.warn("Failed to read config/csrestrict.json, using defaults", e);
		}

		RestrictionConfig defaults = new RestrictionConfig();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(defaults));
		} catch (IOException e) {
			CsRestrictMod.LOGGER.warn("Failed to write default config/csrestrict.json", e);
		}
		return defaults;
	}
}
