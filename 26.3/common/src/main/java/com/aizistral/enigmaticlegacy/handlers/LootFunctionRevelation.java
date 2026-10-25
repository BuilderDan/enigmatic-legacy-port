package com.aizistral.enigmaticlegacy.handlers;

import java.util.Optional;

import com.aizistral.enigmaticlegacy.items.RevelationTome;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * Port: loot functions are codec-based. CODEC replaces upstream's JSON Serializer and keeps
 * its "randomPoints"/"randomXP" field names. 26.3 registers the MapCodec itself as the
 * function type, and number providers are {@link ContextIntProvider} holders.
 */
public class LootFunctionRevelation extends LootItemConditionalFunction {
	public static final MapCodec<LootFunctionRevelation> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
			.and(ContextIntProviders.CODEC.fieldOf("randomPoints").forGetter(function -> function.randomPoints))
			.and(ContextIntProviders.CODEC.fieldOf("randomXP").forGetter(function -> function.randomXP))
			.apply(instance, LootFunctionRevelation::new));

	private final Holder<ContextIntProvider> randomPoints;
	private final Holder<ContextIntProvider> randomXP;

	private LootFunctionRevelation(Optional<Holder<LootItemCondition>> condition, Holder<ContextIntProvider> minMaxPoints, Holder<ContextIntProvider> minMaxXP) {
		super(condition);
		this.randomPoints = minMaxPoints;
		this.randomXP = minMaxXP;
	}

	@Override
	public MapCodec<LootFunctionRevelation> codec() {
		return CODEC;
	}

	@Override
	public ItemStack run(ItemStack stack, LootContext context) {
		if (stack.getItem() instanceof RevelationTome tome) {
			stack.applyComponents(tome.createTome(this.randomPoints.value().getInt(context), this.randomXP.value().getInt(context)).getComponentsPatch());
		}

		return stack;
	}

	public static LootFunctionRevelation.Builder of(Holder<ContextIntProvider> minMaxPoints, Holder<ContextIntProvider> minMaxXP) {
		return new LootFunctionRevelation.Builder(minMaxPoints, minMaxXP);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<LootFunctionRevelation.Builder> {
		private final Holder<ContextIntProvider> randomPoints;
		private final Holder<ContextIntProvider> randomXP;

		public Builder(Holder<ContextIntProvider> minMaxPoints, Holder<ContextIntProvider> minMaxXP) {
			this.randomPoints = minMaxPoints;
			this.randomXP = minMaxXP;
		}

		@Override
		protected LootFunctionRevelation.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new LootFunctionRevelation(this.getCondition(), this.randomPoints, this.randomXP);
		}
	}
}
