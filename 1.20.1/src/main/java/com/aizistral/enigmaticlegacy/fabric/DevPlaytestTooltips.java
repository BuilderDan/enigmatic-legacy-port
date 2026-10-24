package com.aizistral.enigmaticlegacy.fabric;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Port-only, dev runs only. After the scenarios, writes the name and tooltip of every item in the
 * mod's creative tabs to test-output/playtest/tooltips.md: the plain page, the Shift page and the
 * Ctrl page (forced through {@link DevHooks}), each with the real client player. Translation keys
 * the loaded language does not have are listed at the top.
 */
public final class DevPlaytestTooltips {
	private static final Logger LOGGER = LoggerFactory.getLogger("EnigmaticLegacy/playtest");

	private DevPlaytestTooltips() {}

	public static void dump(Minecraft client) {
		List<String> out = new ArrayList<>();
		Set<String> missing = new LinkedHashSet<>();
		Set<String> seen = new LinkedHashSet<>();
		int items = 0;

		for (CreativeModeTab tab : new CreativeModeTab[] { EnigmaticTabs.MAIN, EnigmaticTabs.POTIONS }) {
			for (ItemStack stack : tab.getDisplayItems()) {
				String id = BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.hasTag() ? " " + stack.getTag() : "");

				if (!seen.add(id))
					continue;

				items++;
				out.add("## " + id);
				out.add("");
				out.add("Name: " + text(stack.getHoverName(), missing));

				for (String page : new String[] { "plain", "shift", "ctrl" }) {
					DevHooks.shiftDown = page.equals("shift");
					DevHooks.controlDown = page.equals("ctrl");
					List<Component> lines;

					try {
						lines = stack.getTooltipLines(client.player, TooltipFlag.NORMAL);
					} catch (Exception ex) {
						out.add("- " + page + ": **tooltip threw " + ex + "**");
						continue;
					}

					out.add("- " + page + ":");
					// The first line is the name, already listed above.
					for (Component line : lines.subList(Math.min(1, lines.size()), lines.size())) {
						out.add("  - " + text(line, missing));
					}
				}

				out.add("");
			}
		}

		DevHooks.shiftDown = DevHooks.controlDown = false;

		List<String> head = new ArrayList<>();
		head.add("# Tooltip dump");
		head.add("");
		head.add(items + " items. Missing translation keys: " + missing.size());
		missing.forEach(key -> head.add("- `" + key + "`"));
		head.add("");
		head.addAll(out);

		try {
			File file = new File(client.gameDirectory, "../../test-output/playtest/tooltips.md");
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), head, StandardCharsets.UTF_8);
			LOGGER.info("[playtest] Tooltips of {} items written, {} missing keys", items, missing.size());
		} catch (Exception ex) {
			LOGGER.warn("[playtest] Tooltip dump failed", ex);
		}
	}

	private static String text(Component component, Set<String> missing) {
		collectMissing(component, missing);
		return component.getString().replace("\n", " / ");
	}

	private static void collectMissing(Component component, Set<String> missing) {
		if (component.getContents() instanceof TranslatableContents translatable) {
			if (!Language.getInstance().has(translatable.getKey()))
				missing.add(translatable.getKey());

			for (Object arg : translatable.getArgs()) {
				if (arg instanceof Component child)
					collectMissing(child, missing);
			}
		}

		component.getSiblings().forEach(child -> collectMissing(child, missing));
	}
}
