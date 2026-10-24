package com.aizistral.enigmaticlegacy.triggers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Special trigger that activates if player successfully beheads a mob with Axe of Executioner.
 * @author Integral
 */
public class BeheadingTrigger extends SimpleCriterionTrigger<BeheadingTrigger.Instance> {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "forbidden_axe_beheading");
	public static final BeheadingTrigger INSTANCE = new BeheadingTrigger();

	private BeheadingTrigger() {}

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
