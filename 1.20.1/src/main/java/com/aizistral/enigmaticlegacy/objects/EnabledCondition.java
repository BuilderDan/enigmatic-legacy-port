package com.aizistral.enigmaticlegacy.objects;

import com.aizistral.enigmaticlegacy.fabric.EnigmaticLegacyFabric;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * Condition that checks whether item is instance of IPerhaps, and if it is,
 * whether or not it is enabled. Used to disable recipes for items disabled
 * in config.
 * @author Integral
 *
 * Fabric port: registered as a Fabric API resource condition
 * ({@code "fabric:load_conditions"}) instead of a Forge ICondition.
 */

public class EnabledCondition {
	private static final ResourceLocation ID = new ResourceLocation(EnigmaticLegacyFabric.MODID, "is_enabled");

	public static void register() {
		ResourceConditions.register(ID, EnabledCondition::test);
	}

	private static boolean test(JsonObject json) {
		ResourceLocation item = new ResourceLocation(GsonHelper.getAsString(json, "item"));

		// TODO Fabric port: check OmniconfigHandler.bonusWoolRecipesEnabled and
		// OmniconfigHandler.isItemEnabled(item) once the config is ported.
		if (item.toString().equals(EnigmaticLegacyFabric.MODID + ":bonuswoolrecipes"))
			return true;
		else
			return BuiltInRegistries.ITEM.containsKey(item);
	}
}
