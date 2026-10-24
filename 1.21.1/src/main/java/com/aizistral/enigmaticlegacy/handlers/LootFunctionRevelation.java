package com.aizistral.enigmaticlegacy.handlers;

import com.aizistral.enigmaticlegacy.items.RevelationTome;
import com.aizistral.enigmaticlegacy.registries.EnigmaticLootFunctions;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

/**
 * Port: 1.21 loot functions are codec-based. CODEC replaces upstream's JSON Serializer and
 * keeps its "randomPoints"/"randomXP" field names.
 */
public class LootFunctionRevelation extends LootItemConditionalFunction {
	public static final MapCodec<LootFunctionRevelation> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
			.and(NumberProviders.CODEC.fieldOf("randomPoints").forGetter(function -> function.randomPoints))
			.and(NumberProviders.CODEC.fieldOf("randomXP").forGetter(function -> function.randomXP))
			.apply(instance, LootFunctionRevelation::new));

	private final NumberProvider randomPoints;
	private final NumberProvider randomXP;

	private LootFunctionRevelation(List<LootItemCondition> conditions, NumberProvider minMaxPoints, NumberProvider minMaxXP) {
		super(conditions);
		this.randomPoints = minMaxPoints;
		this.randomXP = minMaxXP;
	}

	@Override
	public LootItemFunctionType getType() {
		return EnigmaticLootFunctions.REVELATION;
	}

	@Override
	public ItemStack run(ItemStack stack, LootContext context) {
		if (stack.getItem() instanceof RevelationTome tome) {
			stack.applyComponents(tome.createTome(this.randomPoints.getInt(context), this.randomXP.getInt(context)).getComponentsPatch());
		}

		return stack;
	}

	public static LootFunctionRevelation.Builder of(NumberProvider minMaxPoints, NumberProvider minMaxXP) {
		return new LootFunctionRevelation.Builder(minMaxPoints, minMaxXP);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<LootFunctionRevelation.Builder> {
		private final NumberProvider randomPoints;
		private final NumberProvider randomXP;

		public Builder(NumberProvider minMaxPoints, NumberProvider minMaxXP) {
			this.randomPoints = minMaxPoints;
			this.randomXP = minMaxXP;
		}

		@Override
		protected LootFunctionRevelation.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new LootFunctionRevelation(this.getConditions(), this.randomPoints, this.randomXP);
		}
	}

}
