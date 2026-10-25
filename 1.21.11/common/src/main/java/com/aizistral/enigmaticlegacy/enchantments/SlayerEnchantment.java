package com.aizistral.enigmaticlegacy.enchantments;

import static com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack.getRegisteredAttackStregth;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;

/**
 * Upstream enchantment. Port: enchantments are data-driven in 1.21 (definition in
 * data/enigmaticlegacy/enchantment/slayer.json), so this class only keeps the bonus damage.
 */
public class SlayerEnchantment {

	public static float bonusDamageByCreature(LivingEntity attacker, LivingEntity living, int level) {
		float calculated = living instanceof Monster ? level * 1.5F : 0F;
		calculated*= getRegisteredAttackStregth(attacker);

		return calculated;
	}

	private SlayerEnchantment() {
		// NO-OP
	}
}
