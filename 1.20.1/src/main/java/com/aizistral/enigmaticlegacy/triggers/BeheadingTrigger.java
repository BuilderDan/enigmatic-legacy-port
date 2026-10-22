package com.aizistral.enigmaticlegacy.triggers;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Special trigger that activates if player successfully beheads a mob with Axe of Executioner.
 * @author Integral
 */

public class BeheadingTrigger extends SimpleCriterionTrigger<BeheadingTrigger.Instance> {
	public static final ResourceLocation ID = new ResourceLocation(EnigmaticLegacy.MODID, "forbidden_axe_beheading");
	public static final BeheadingTrigger INSTANCE = new BeheadingTrigger();

	private BeheadingTrigger() {}

	@NotNull
	@Override
	public ResourceLocation getId() {
		return BeheadingTrigger.ID;
	}

	@NotNull
	@Override
	public BeheadingTrigger.Instance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate playerPred, DeserializationContext conditions) {
		return new BeheadingTrigger.Instance(playerPred);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, instance -> instance.test());
	}

	static class Instance extends AbstractCriterionTriggerInstance {
		Instance(ContextAwarePredicate playerPred) {
			super(BeheadingTrigger.ID, playerPred);
		}

		@NotNull
		@Override
		public ResourceLocation getCriterion() {
			return BeheadingTrigger.ID;
		}

		boolean test() {
			return true;
		}
	}

}
