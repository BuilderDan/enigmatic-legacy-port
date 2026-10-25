package com.aizistral.enigmaticlegacy.triggers;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only: stands in for Curios' {@code curios:equip_curio} trigger, which Trinkets has no equal of.
 * Fired from TrinketAdapter whenever a player equips an accessory; supports the {@code item} condition.
 */
public class EquipAccessoryTrigger extends SimpleCriterionTrigger<EquipAccessoryTrigger.Instance> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "equip_accessory");
	public static final EquipAccessoryTrigger INSTANCE = new EquipAccessoryTrigger();

	private EquipAccessoryTrigger() {}

	// Port: 1.21 triggers are codec-based and registered in BuiltInRegistries.TRIGGER_TYPES under ID.
	@NotNull
	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack stack) {
		this.trigger(player, instance -> instance.test(stack));
	}

	public record Instance(Optional<ContextAwarePredicate> player, ItemPredicate item) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
				ItemPredicate.CODEC.fieldOf("item").forGetter(Instance::item)
				).apply(builder, Instance::new));

		boolean test(ItemStack stack) {
			return this.item.test(stack);
		}
	}

}
