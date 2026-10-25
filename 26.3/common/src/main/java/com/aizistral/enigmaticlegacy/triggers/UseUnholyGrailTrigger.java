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

/**
 * Special trigger that activates when player drinks from Unholy Grail.
 * @author Integral
 */
public class UseUnholyGrailTrigger extends SimpleCriterionTrigger<UseUnholyGrailTrigger.Instance> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "unholy_grail_drink");
	public static final UseUnholyGrailTrigger INSTANCE = new UseUnholyGrailTrigger();

	private UseUnholyGrailTrigger() {}

	// Port: 1.21 triggers are codec-based and registered in BuiltInRegistries.TRIGGER_TYPES under ID.
	@NotNull
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, boolean isTheWorthyOne) {
		this.trigger(player, instance -> instance.test(isTheWorthyOne));
	}

	public record Instance(Optional<Holder<LootItemCondition>> player, Optional<Boolean> isTheWorthyOne) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Instance::player),
				Codec.BOOL.optionalFieldOf("is_the_worthy_one").forGetter(Instance::isTheWorthyOne)
				).apply(builder, Instance::new));

		boolean test(boolean isTheWorthyOne) {
			return this.isTheWorthyOne.map(value -> value == isTheWorthyOne).orElse(true);
		}
	}

}
