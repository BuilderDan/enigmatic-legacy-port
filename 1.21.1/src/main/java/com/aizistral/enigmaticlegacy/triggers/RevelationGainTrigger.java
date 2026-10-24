package com.aizistral.enigmaticlegacy.triggers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.items.RevelationTome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class RevelationGainTrigger extends SimpleCriterionTrigger<RevelationGainTrigger.Instance> {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "embrace_revelation");
	public static final RevelationGainTrigger INSTANCE = new RevelationGainTrigger();

	private RevelationGainTrigger() {}

	// Port: 1.21 triggers are codec-based and registered in BuiltInRegistries.TRIGGER_TYPES under ID.
	@NotNull
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, RevelationTome.TomeType type, int amount) {
		this.trigger(player, instance -> instance.test(type, amount));
	}

	public record Instance(Optional<ContextAwarePredicate> player, String revelationType, int requiredAmount) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
				Codec.STRING.fieldOf("point_type").forGetter(Instance::revelationType),
				Codec.INT.fieldOf("point_amount").forGetter(Instance::requiredAmount)
				).apply(builder, Instance::new));

		boolean test(RevelationTome.TomeType type, int count) {
			return RevelationTome.TomeType.resolveType(this.revelationType).equals(type) && count >= this.requiredAmount;
		}
	}

}
