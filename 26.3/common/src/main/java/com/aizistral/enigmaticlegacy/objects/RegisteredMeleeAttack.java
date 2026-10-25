package com.aizistral.enigmaticlegacy.objects;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Port: upstream also stores ticksSinceLastSwing (Player.attackStrengthTicker), which nothing reads;
 * it is dropped here (see CHANGES.md). Attacks are registered from Fabric's AttackEntityCallback in
 * EnigmaticLegacyFabric, which stands in for upstream's AttackEntityEvent subscriber.
 */
public class RegisteredMeleeAttack {
	private static final Map<Player, RegisteredMeleeAttack> attackRegistry = new WeakHashMap<>();

	public static boolean hasRegisteredMeleeAttack(Player player) {
		return attackRegistry.containsKey(player);
	}

	public static RegisteredMeleeAttack getRegisteredMeleeAttack(Player player) {
		return attackRegistry.get(player);
	}

	public static void registerAttack(Player player) {
		attackRegistry.put(player, new RegisteredMeleeAttack(player));
	}

	public static float getRegisteredAttackStregth(LivingEntity attacker) {
		float attackStregth = 1F;
		if (attacker instanceof Player && hasRegisteredMeleeAttack((Player) attacker)) {
			RegisteredMeleeAttack attack = getRegisteredMeleeAttack((Player)attacker);
			if (attack.ticksExisted == attacker.tickCount) {
				attackStregth = attack.cooledAttackStrength;
			}
		}

		return attackStregth;
	}

	public static void clearRegistry() {
		attackRegistry.clear();
	}

	public final Player player;
	public final int ticksExisted;
	public final float cooledAttackStrength;

	private RegisteredMeleeAttack(Player player) {
		this.player = player;

		this.ticksExisted = player.tickCount;
		this.cooledAttackStrength = player.getAttackStrengthScale(0.5F);
	}



}
