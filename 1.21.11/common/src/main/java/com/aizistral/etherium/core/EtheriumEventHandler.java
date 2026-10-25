package com.aizistral.etherium.core;

import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.etherium.items.EtheriumArmor;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;

/**
 * Port: upstream subscribes an instance of this class to Forge's LivingHurtEvent and
 * LivingAttackEvent. Here the handlers are static methods called from
 * EnigmaticEventHandler.onLivingHurt and onLivingAttack. The standalone End City loot
 * pool is left out, since isStandalone() is always false inside Enigmatic Legacy.
 */
public class EtheriumEventHandler {

	private static IEtheriumConfig config() {
		return EtheriumConfigHandler.instance();
	}

	/**
	 * @return the damage amount after the Etherium Armor shield reduction.
	 */
	public static float onEntityHurt(LivingEntity entity, DamageSource source, float amount) {
		if (entity instanceof Player player && amount > 0) {
			/*
			 * Handler for knockback feedback and damage reduction of Etherium Armor Shield.
			 */

			if (EtheriumArmor.hasShield(player)) {
				if (source.getDirectEntity() instanceof LivingEntity) {
					LivingEntity attacker = ((LivingEntity) source.getEntity());
					Vector3 vec = Vector3.fromEntityCenter(player).subtract(Vector3.fromEntityCenter(source.getEntity())).normalize();
					attacker.knockback(0.75F, vec.x, vec.z);
					player.level().playSound(null, player.blockPosition(), config().getShieldTriggerSound(), SoundSource.PLAYERS, 1.0F, 0.9F + (float) (Math.random() * 0.1D));
					player.level().playSound(null, player.blockPosition(), config().getShieldTriggerSound(), SoundSource.PLAYERS, 1.0F, 0.9F + (float) (Math.random() * 0.1D));
				}

				return amount * config().getShieldReduction().asModifierInverted();
			}
		}

		return amount;
	}

	/**
	 * @return false if the attack should be canceled.
	 */
	public static boolean onEntityAttacked(LivingEntity entity, DamageSource source) {
		if (entity.level().isClientSide())
			return true;

		// TODO Figure out a way to account for shield blocking

		/*
		 * Handler for immunities and projectile deflection.
		 */

		if (entity instanceof Player player) {
			if (source.getDirectEntity() instanceof AbstractHurtingProjectile || source.getDirectEntity() instanceof AbstractArrow) {
				if (EtheriumArmor.hasShield(player)) {
					player.level().playSound(null, player.blockPosition(), config().getShieldTriggerSound(), SoundSource.PLAYERS, 1.0F, 0.9F + (float) (Math.random() * 0.1D));
					return false;
				}
			}
		}

		return true;
	}

}
