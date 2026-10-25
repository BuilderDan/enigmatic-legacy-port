package com.aizistral.enigmaticlegacy.fabric.platform;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.objects.EnabledCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

/**
 * Port: {@link EnabledCondition} as a Fabric API resource condition
 * ({@code "fabric:load_conditions"}) instead of a Forge ICondition.
 */
public record FabricEnabledCondition(Identifier item) implements ResourceCondition {
	public static final MapCodec<FabricEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(FabricEnabledCondition::item)
			).apply(instance, FabricEnabledCondition::new));

	public static final ResourceConditionType<FabricEnabledCondition> TYPE = ResourceConditionType.create(EnabledCondition.ID, CODEC);

	public static void register() {
		ResourceConditions.register(TYPE);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryLookup) {
		return EnabledCondition.test(this.item);
	}
}
