package com.aizistral.enigmaticlegacy.fabric;

import java.util.List;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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

		LOGGER.info("[devcheck] Done: {} problem(s)", problems);
	}
}
