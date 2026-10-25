package com.aizistral.enigmaticlegacy.enchantments;

import static com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack.getRegisteredAttackStregth;

import net.minecraft.world.entity.LivingEntity;

/**
 * Upstream enchantment. Port: enchantments are data-driven in 1.21 (definition in
 * data/enigmaticlegacy/enchantment/wrath.json). Upstream's bonus came from vanilla's
 * getDamageBonus (level * 1.25), which no longer exists, so it is applied here instead.
 */
public class WrathEnchantment {

	public static float bonusDamageByCreature(LivingEntity attacker, LivingEntity living, int level) {
		return level * 1.25F * getRegisteredAttackStregth(attacker);
	}

	private WrathEnchantment() {
		// NO-OP
	}
}
