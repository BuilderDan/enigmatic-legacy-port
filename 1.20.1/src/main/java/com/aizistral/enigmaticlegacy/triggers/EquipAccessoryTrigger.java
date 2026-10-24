package com.aizistral.enigmaticlegacy.triggers;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only: stands in for Curios' {@code curios:equip_curio} trigger, which Trinkets has no equal of.
 * Fired from TrinketAdapter whenever a player equips an accessory; supports the {@code item} condition.
 */
public class EquipAccessoryTrigger extends SimpleCriterionTrigger<EquipAccessoryTrigger.Instance> {
	public static final ResourceLocation ID = new ResourceLocation(EnigmaticLegacy.MODID, "equip_accessory");
	public static final EquipAccessoryTrigger INSTANCE = new EquipAccessoryTrigger();

	private EquipAccessoryTrigger() {}

	@NotNull
	@Override
	public ResourceLocation getId() {
		return EquipAccessoryTrigger.ID;
	}

	@NotNull
	@Override
	public EquipAccessoryTrigger.Instance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate playerPred, DeserializationContext conditions) {
		return new EquipAccessoryTrigger.Instance(playerPred, ItemPredicate.fromJson(json.get("item")));
	}

	public void trigger(ServerPlayer player, ItemStack stack) {
		this.trigger(player, instance -> instance.test(stack));
	}

	static class Instance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		Instance(ContextAwarePredicate playerPred, ItemPredicate item) {
			super(EquipAccessoryTrigger.ID, playerPred);
			this.item = item;
		}

		@NotNull
		@Override
		public ResourceLocation getCriterion() {
			return EquipAccessoryTrigger.ID;
		}

		boolean test(ItemStack stack) {
			return this.item.matches(stack);
		}
	}

}
