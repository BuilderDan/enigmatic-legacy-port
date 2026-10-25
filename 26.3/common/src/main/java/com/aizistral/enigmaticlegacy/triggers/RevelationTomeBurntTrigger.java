package com.aizistral.enigmaticlegacy.triggers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class RevelationTomeBurntTrigger extends SimpleCriterionTrigger<RevelationTomeBurntTrigger.Instance> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "revelation_tome_burnt");
	public static final RevelationTomeBurntTrigger INSTANCE = new RevelationTomeBurntTrigger();

	private RevelationTomeBurntTrigger() {}

	// Port: 1.21 triggers are codec-based and registered in BuiltInRegistries.TRIGGER_TYPES under ID.
	@NotNull
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, instance -> instance.test());
	}

	public record Instance(Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player)
				).apply(builder, Instance::new));

		boolean test() {
			return true;
		}
	}

}
