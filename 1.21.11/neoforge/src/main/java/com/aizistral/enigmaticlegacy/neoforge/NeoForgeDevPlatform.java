package com.aizistral.enigmaticlegacy.neoforge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.dev.DevPlatform;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Port-only: NeoForge hooks for the dev playtest harness. */
public class NeoForgeDevPlatform implements DevPlatform {
	private static final String CONDITIONS_KEY = "neoforge:conditions";

	@Override
	public void onServerTick(Consumer<MinecraftServer> tick) {
		NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> tick.accept(event.getServer()));
	}

	@Override
	public Optional<Path> modResource(String path) {
		for (Path root : ModList.get().getModFileById(EnigmaticLegacy.MODID).getFile().getContents().getContentRoots()) {
			Path file = root.resolve(path);

			if (Files.exists(file))
				return Optional.of(file);
		}

		return Optional.empty();
	}

	@Override
	public boolean recipeConditionsPass(JsonObject json) {
		if (!json.has(CONDITIONS_KEY))
			return true;

		return ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, json.get(CONDITIONS_KEY))
				.map(list -> list.stream().allMatch(condition -> condition.test(ICondition.IContext.EMPTY)))
				.result().orElse(false);
	}
}
