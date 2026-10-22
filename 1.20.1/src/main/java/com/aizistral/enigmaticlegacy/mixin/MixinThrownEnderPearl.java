package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;

/**
 * Port-only. Stands in for Forge's {@code EntityTeleportEvent.EnderPearl}, where upstream sets
 * the pearl's damage to 0 for wearers of the Eye of the Nebula or the Cube.
 */
@Mixin(ThrownEnderpearl.class)
public class MixinThrownEnderPearl {
	@ModifyArg(method = "onHit", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
	private float enigmaticlegacy$pearlDamage(float damage) {
		Entity owner = ((ThrownEnderpearl) (Object) this).getOwner();

		if (owner instanceof Player player && (SuperpositionHandler.hasCurio(player, EnigmaticItems.EYE_OF_NEBULA)
				|| SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)))
			return 0;

		return damage;
	}
}
