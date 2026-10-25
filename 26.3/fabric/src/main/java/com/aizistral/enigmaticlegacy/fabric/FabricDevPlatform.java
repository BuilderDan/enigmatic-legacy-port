package com.aizistral.enigmaticlegacy.fabric;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.dev.DevPlatform;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

/** Port-only: Fabric hooks for the dev playtest harness. */
public class FabricDevPlatform implements DevPlatform {
	@Override
	public void onServerTick(Consumer<MinecraftServer> tick) {
		ServerTickEvents.END_SERVER_TICK.register(tick::accept);
	}

	@Override
	public Optional<Path> modResource(String path) {
		return FabricLoader.getInstance().getModContainer(EnigmaticLegacy.MODID).orElseThrow().findPath(path);
	}

	@Override
	public boolean recipeConditionsPass(JsonObject json) {
		return !json.has(ResourceConditions.CONDITIONS_KEY) || ResourceCondition.CONDITION_CODEC
				.parse(JsonOps.INSTANCE, json.get(ResourceConditions.CONDITIONS_KEY)).getOrThrow().test(null);
	}
}
