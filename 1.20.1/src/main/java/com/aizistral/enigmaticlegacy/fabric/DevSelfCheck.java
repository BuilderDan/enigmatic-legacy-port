package com.aizistral.enigmaticlegacy.fabric;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Port-only, dev runs only. Enabled with {@code -Denigmaticlegacy.devcheck=true} (or
 * {@code =exit} to close the client afterwards); see {@code -Pdevcheck} in build.gradle.
 * Once the title screen is up, it builds the main creative tab and, for every stack in it,
 * checks the item name, model and tooltip, logging each result with a "[devcheck]" prefix.
 * This is a smoke test, not a replacement for the manual in-game playtest.
 */
public final class DevSelfCheck {
	private static final Logger LOGGER = LoggerFactory.getLogger("EnigmaticLegacy/devcheck");
	private static final String MODE = System.getProperty("enigmaticlegacy.devcheck", "");
	private static boolean done = false;

	private DevSelfCheck() {}

	public static void register() {
		if (MODE.isEmpty() || MODE.equals("false"))
			return;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (done || client.getOverlay() != null || !(client.screen instanceof TitleScreen))
				return;

			done = true;
			run(client);

			if (MODE.equals("exit")) {
				client.stop();
			}
		});
	}

	private static void run(Minecraft client) {
		int problems = 0;

		CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, false, VanillaRegistries.createLookup());
		List<ItemStack> stacks = List.copyOf(EnigmaticTabs.MAIN.getDisplayItems());
		LOGGER.info("[devcheck] Main tab holds {} stacks", stacks.size());

		long registered = BuiltInRegistries.ITEM.keySet().stream()
				.filter(id -> id.getNamespace().equals(EnigmaticLegacy.MODID)).count();
		LOGGER.info("[devcheck] {} items registered under {}", registered, EnigmaticLegacy.MODID);

		BakedModel missing = client.getModelManager().getMissingModel();

		for (ItemStack stack : stacks) {
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

			if (!I18n.exists(stack.getDescriptionId())) {
				problems++;
				LOGGER.warn("[devcheck] {}: no translation for {}", id, stack.getDescriptionId());
			}

			if (client.getItemRenderer().getModel(stack, null, null, 0) == missing) {
				problems++;
				LOGGER.warn("[devcheck] {}: missing item model", id);
			}

			for (TooltipFlag flag : new TooltipFlag[] { TooltipFlag.NORMAL, TooltipFlag.ADVANCED }) {
				try {
					List<Component> lines = stack.getTooltipLines(null, flag);

					if (flag == TooltipFlag.NORMAL) {
						LOGGER.info("[devcheck] {}: {} tooltip lines", id, lines.size());

						for (Component line : lines) {
							String text = line.getString();

							if (text.contains("tooltip.enigmaticlegacy") || text.contains("enigmaticlegacy.")) {
								problems++;
								LOGGER.warn("[devcheck] {}: untranslated tooltip line: {}", id, text);
							} else {
								LOGGER.info("[devcheck]   | {}", text);
							}
						}
					}
				} catch (Exception ex) {
					problems++;
					LOGGER.error("[devcheck] {}: tooltip threw ({})", id, flag);
					LOGGER.error("[devcheck] stack trace", ex);
				}
			}
		}

		problems += checkRecipes();
		LOGGER.info("[devcheck] Done: {} problem(s)", problems);
	}

	/**
	 * Parses every bundled recipe JSON the way the server would (load conditions first),
	 * so serializer and ingredient errors show up without loading a world.
	 */
	private static int checkRecipes() {
		int problems = 0, parsed = 0, skipped = 0, pending = 0;
		Path dir = FabricLoader.getInstance().getModContainer(EnigmaticLegacy.MODID).orElseThrow()
				.findPath("data/" + EnigmaticLegacy.MODID + "/recipes").orElse(null);
		if (dir == null) {
			LOGGER.error("[devcheck] recipe folder not found");
			return 1;
		}
		try (Stream<Path> files = Files.walk(dir)) {
			for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".json")).sorted()::iterator) {
				String name = dir.relativize(file).toString().replace('\\', '/');
				ResourceLocation id = new ResourceLocation(EnigmaticLegacy.MODID, name.substring(0, name.length() - 5));
				try (Reader reader = Files.newBufferedReader(file)) {
					JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
					if (!ResourceConditions.objectMatchesConditions(json)) {
						skipped++;
						continue;
					}
					RecipeManager.fromJson(id, json);
					parsed++;
					String type = json.get("type").getAsString();
					if (type.startsWith(EnigmaticLegacy.MODID + ":")) {
						LOGGER.info("[devcheck] recipe {} ok ({})", id, type);
					}
				} catch (Exception ex) {
					String msg = String.valueOf(ex.getMessage());
					// Recipes that name our own not-yet-ported items or serializers are expected to
					// fail until those are ported; anything else is a real problem.
					if (msg.contains("item '" + EnigmaticLegacy.MODID + ":") || msg.contains("type '" + EnigmaticLegacy.MODID + ":")) {
						pending++;
						LOGGER.info("[devcheck] recipe {} pending: {}", id, msg);
						continue;
					}
					problems++;
					LOGGER.error("[devcheck] recipe {} failed: {}", id, ex.toString());
				}
			}
		} catch (Exception ex) {
			LOGGER.error("[devcheck] recipe scan failed", ex);
			return problems + 1;
		}
		LOGGER.info("[devcheck] recipes: {} parsed, {} skipped by conditions, {} pending (unported content), {} failed",
				parsed, skipped, pending, problems);
		return problems;
	}
}
