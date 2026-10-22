package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.effects.BlazingStrengthEffect;
import com.aizistral.enigmaticlegacy.effects.GrowingBloodlustEffect;
import com.aizistral.enigmaticlegacy.effects.GrowingHungerEffect;
import com.aizistral.enigmaticlegacy.effects.MoltenHeartEffect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class EnigmaticEffects extends AbstractRegistry<MobEffect> {
	private static final EnigmaticEffects INSTANCE = new EnigmaticEffects();

	@ObjectHolder(value = MODID + ":blazing_strength", registryName = "mob_effect")
	public static BlazingStrengthEffect BLAZING_STRENGTH;

	@ObjectHolder(value = MODID + ":molten_heart", registryName = "mob_effect")
	public static MoltenHeartEffect MOLTEN_HEART;

	@ObjectHolder(value = MODID + ":growing_hunger", registryName = "mob_effect")
	public static GrowingHungerEffect GROWING_HUNGER;

	@ObjectHolder(value = MODID + ":growing_bloodlust", registryName = "mob_effect")
	public static GrowingBloodlustEffect GROWING_BLOODLUST;

	private EnigmaticEffects() {
		super(BuiltInRegistries.MOB_EFFECT);
		this.register("blazing_strength", BlazingStrengthEffect::new);
		this.register("molten_heart", MoltenHeartEffect::new);
		this.register("growing_hunger", GrowingHungerEffect::new);
		this.register("growing_bloodlust", GrowingBloodlustEffect::new);
	}

	/** Port: forces class loading, which registers the effects. Call after the config loads. */
	public static void init() {
		// NO-OP
	}
}
