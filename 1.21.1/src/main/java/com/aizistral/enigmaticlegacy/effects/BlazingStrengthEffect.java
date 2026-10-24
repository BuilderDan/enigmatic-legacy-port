package com.aizistral.enigmaticlegacy.effects;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Port: AttackDamageMobEffect is gone in 1.21. A plain modifier of 3.0 gives the same
 * +3 attack damage per level, since 1.21 multiplies it by (amplifier + 1).
 */
public class BlazingStrengthEffect extends MobEffect {

	public BlazingStrengthEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFF5000);
		this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "effect.blazing_strength"), 3.0D, AttributeModifier.Operation.ADD_VALUE);
	}

}
