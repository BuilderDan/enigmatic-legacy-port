package com.aizistral.enigmaticlegacy.triggers;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CursedRingEquippedTrigger extends SimpleCriterionTrigger<CursedRingEquippedTrigger.Instance> {
	public static final ResourceLocation ID = new ResourceLocation(EnigmaticLegacy.MODID, "equip_cursed_ring");
	public static final CursedRingEquippedTrigger INSTANCE = new CursedRingEquippedTrigger();

	private CursedRingEquippedTrigger() {}

	@NotNull
	@Override
	public ResourceLocation getId() {
		return CursedRingEquippedTrigger.ID;
	}

	@NotNull
	@Override
	public CursedRingEquippedTrigger.Instance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate playerPred, DeserializationContext conditions) {
		return new CursedRingEquippedTrigger.Instance(playerPred);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, instance -> instance.test());
	}

	static class Instance extends AbstractCriterionTriggerInstance {
		Instance(ContextAwarePredicate playerPred) {
			super(CursedRingEquippedTrigger.ID, playerPred);
		}

		@NotNull
		@Override
		public ResourceLocation getCriterion() {
			return CursedRingEquippedTrigger.ID;
		}

		boolean test() {
			return true;
		}
	}

}
