package com.aizistral.enigmaticlegacy.enchantments;

import static com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack.getRegisteredAttackStregth;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/**
 * Upstream enchantment. Port: enchantments are data-driven in 1.21 (definition in
 * data/enigmaticlegacy/enchantment/torrent.json), so this class only keeps the bonus damage.
 */
public class TorrentEnchantment {

	public static float bonusDamageByCreature(LivingEntity attacker, LivingEntity living, int level) {
		float calculated = (living.fireImmune() || living.isSensitiveToWater() || living instanceof EnderDragon) ? level * 2.5F : 0F;
		calculated*= getRegisteredAttackStregth(attacker);

		return calculated;
	}

	private TorrentEnchantment() {
		// NO-OP
	}
}
