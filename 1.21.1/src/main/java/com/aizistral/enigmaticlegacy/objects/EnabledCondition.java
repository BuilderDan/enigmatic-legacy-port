package com.aizistral.enigmaticlegacy.objects;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.fabric.EnigmaticLegacyFabric;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Condition that checks whether item is instance of IPerhaps, and if it is,
 * whether or not it is enabled. Used to disable recipes for items disabled
 * in config.
 * @author Integral
 *
 * Fabric port: registered as a Fabric API resource condition
 * ({@code "fabric:load_conditions"}) instead of a Forge ICondition.
 * Port: a codec-based ResourceCondition record in 1.21; the JSON format is unchanged.
 */

public record EnabledCondition(ResourceLocation item) implements ResourceCondition {
	private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacyFabric.MODID, "is_enabled");

	public static final MapCodec<EnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("item").forGetter(EnabledCondition::item)
			).apply(instance, EnabledCondition::new));

	public static final ResourceConditionType<EnabledCondition> TYPE = ResourceConditionType.create(ID, CODEC);

	public static void register() {
		ResourceConditions.register(TYPE);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(@Nullable HolderLookup.Provider registryLookup) {
		// TODO Fabric port: check OmniconfigHandler.bonusWoolRecipesEnabled and
		// OmniconfigHandler.isItemEnabled(item) once the config is ported.
		if (this.item.toString().equals(EnigmaticLegacyFabric.MODID + ":bonuswoolrecipes"))
			return true;
		else
			return BuiltInRegistries.ITEM.containsKey(this.item);
	}
}
