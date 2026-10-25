package com.aizistral.enigmaticlegacy.objects;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Condition that checks whether item is instance of IPerhaps, and if it is,
 * whether or not it is enabled. Used to disable recipes for items disabled
 * in config.
 * @author Integral
 *
 * Port: the loader-neutral test. Fabric wraps it as a {@code fabric:load_conditions} resource
 * condition and NeoForge as a {@code neoforge:conditions} ICondition; both read
 * {@code {"type": "enigmaticlegacy:is_enabled", "item": ...}}.
 */
public final class EnabledCondition {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "is_enabled");

	private EnabledCondition() {}

	public static boolean test(Identifier item) {
		// TODO Fabric port: check OmniconfigHandler.bonusWoolRecipesEnabled and
		// OmniconfigHandler.isItemEnabled(item) once the config is ported.
		if (item.toString().equals(EnigmaticLegacy.MODID + ":bonuswoolrecipes"))
			return true;
		else
			return BuiltInRegistries.ITEM.containsKey(item);
	}
}
