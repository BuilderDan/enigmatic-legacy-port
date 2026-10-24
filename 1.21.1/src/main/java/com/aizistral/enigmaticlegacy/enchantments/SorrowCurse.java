package com.aizistral.enigmaticlegacy.enchantments;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Upstream enchantment. Port: enchantments are data-driven in 1.21 (definition in
 * data/enigmaticlegacy/enchantment/sorrow.json), so this class only keeps the debuff logic.
 */
public class SorrowCurse {
	private static final List<Holder<MobEffect>> DEBUFF_LIST = List.of(MobEffects.BLINDNESS,
			MobEffects.CONFUSION, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN,
			MobEffects.DIG_SLOWDOWN, MobEffects.HUNGER);

	public static void maybeApplyDebuff(Player player, float damage) {
		if (player.getRandom().nextFloat() < 0.1F) {
			float severity = damage > 4 ? damage / 4 : 1;
			severity *= 0.5F + player.getRandom().nextFloat();

			Holder<MobEffect> debuff = DEBUFF_LIST.get(player.getRandom().nextInt(DEBUFF_LIST.size()));
			int amplifier = Math.min(3, (int) (severity / 2));

			player.addEffect(new MobEffectInstance(debuff, (int) (300 * severity), amplifier, false, true));
		}
	}

	private SorrowCurse() {
		// NO-OP
	}
}
