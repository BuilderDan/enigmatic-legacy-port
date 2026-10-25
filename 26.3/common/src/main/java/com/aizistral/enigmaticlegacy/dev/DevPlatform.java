package com.aizistral.enigmaticlegacy.dev;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;

/**
 * Port-only, dev runs only. The few loader hooks the playtest and devcheck harness needs on
 * both sides; each loader lists its implementation in {@code META-INF/services}.
 */
public interface DevPlatform {
	void onServerTick(Consumer<MinecraftServer> tick);

	/** A file or folder inside this mod's own resources, if present. */
	Optional<Path> modResource(String path);

	/** True if the recipe JSON has no load conditions of this loader, or they all pass. */
	boolean recipeConditionsPass(JsonObject json);
}
