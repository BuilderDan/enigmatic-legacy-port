package com.aizistral.enigmaticlegacy.neoforge.platform;

import com.aizistral.enigmaticlegacy.objects.EnabledCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Port: {@link EnabledCondition} as a NeoForge ICondition ({@code "neoforge:conditions"}).
 */
public record NeoForgeEnabledCondition(Identifier item) implements ICondition {
	public static final MapCodec<NeoForgeEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(NeoForgeEnabledCondition::item)
			).apply(instance, NeoForgeEnabledCondition::new));

	@Override
	public boolean test(IContext context) {
		return EnabledCondition.test(this.item);
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
