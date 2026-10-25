package com.aizistral.enigmaticlegacy.registries;

import net.minecraft.core.Holder;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Port-only. Created and registered eagerly (on class load) so the attributes exist before vanilla
 * builds the player's attributes ({@code MixinPlayer}). Forge's BLOCK_REACH maps to vanilla's
 * BLOCK_INTERACTION_RANGE in 1.21, so it needs no attribute here.
 * <p>
 * GRAVITY and SWIM_SPEED stand in for Forge's ENTITY_GRAVITY and SWIM_SPEED (used by the Enigmatic
 * Amulet). Both are multipliers (base 1.0) applied in {@code MixinLivingEntity.travel}.
 */
public class EnigmaticAttributes {
	public static final Holder<Attribute> GRAVITY = register("gravity", new RangedAttribute("attribute.name.enigmaticlegacy.gravity",
			1.0D, 0.0D, 16.0D).setSyncable(true));
	public static final Holder<Attribute> SWIM_SPEED = register("swim_speed", new RangedAttribute("attribute.name.enigmaticlegacy.swim_speed",
			1.0D, 0.0D, 1024.0D).setSyncable(true));

	private static Holder<Attribute> register(String name, Attribute attribute) {
		return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, name), attribute);
	}

	/** Forces class loading, which registers the attributes. */
	public static void init() {
		// NO-OP
	}

	/** Value of a multiplier attribute (GRAVITY, SWIM_SPEED), 1 if the entity lacks it. */
	public static double getMultiplier(LivingEntity entity, Holder<Attribute> attribute) {
		AttributeInstance instance = entity.getAttribute(attribute);
		return instance != null ? instance.getValue() : 1.0D;
	}

}
