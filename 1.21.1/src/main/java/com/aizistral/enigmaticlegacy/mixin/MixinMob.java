package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Stands in for upstream's LivingChangeTargetEvent handler: arthropods won't
 * target a player holding The Acknowledgment enchanted with Bane of Arthropods, and Guardians
 * won't target a cursed bearer of the Heart of the Guardian unless that player hit them first.
 */
@Mixin(Mob.class)
public abstract class MixinMob {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$ignoreAcknowledgmentHolder(LivingEntity target, CallbackInfo info) {
		Mob self = (Mob) (Object) this;

		if (target instanceof Player player && self.getType().is(EntityTypeTags.ARTHROPOD)
				&& SuperpositionHandler.hasAntiInsectAcknowledgement(player)) {
			info.cancel();
			return;
		}

		if (target instanceof Player player && self instanceof Guardian guardian && self.getClass() != ElderGuardian.class) {
			if (SuperpositionHandler.hasItem(player, EnigmaticItems.GUARDIAN_HEART) && SuperpositionHandler.isTheCursedOne(player)) {
				boolean isBlacklisted = EnigmaticEventHandler.AGERED_GUARDIANS.containsEntry(player, guardian);

				if (self.getLastHurtByMob() != player && !isBlacklisted) {
					info.cancel();
				} else if (!isBlacklisted) {
					EnigmaticEventHandler.AGERED_GUARDIANS.put(player, guardian);
				}
			}
		}
	}
}
