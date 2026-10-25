package com.aizistral.enigmaticlegacy.triggers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ForbiddenFruitTrigger extends SimpleCriterionTrigger<ForbiddenFruitTrigger.Instance> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "consume_forbidden_fruit");
	public static final ForbiddenFruitTrigger INSTANCE = new ForbiddenFruitTrigger();

	private ForbiddenFruitTrigger() {}

	// Port: 1.21 triggers are codec-based and registered in BuiltInRegistries.TRIGGER_TYPES under ID.
	@NotNull
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, instance -> instance.test());
	}

	public record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
				).apply(builder, Instance::new));

		boolean test() {
			return true;
		}
	}

}
