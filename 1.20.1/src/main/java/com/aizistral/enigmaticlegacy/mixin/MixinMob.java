package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Stands in for upstream's LivingChangeTargetEvent handler: arthropods won't
 * target a player holding The Acknowledgment enchanted with Bane of Arthropods.
 */
@Mixin(Mob.class)
public abstract class MixinMob {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$ignoreAcknowledgmentHolder(LivingEntity target, CallbackInfo info) {
		Mob self = (Mob) (Object) this;

		if (target instanceof Player player && self.getMobType() == MobType.ARTHROPOD
				&& SuperpositionHandler.hasAntiInsectAcknowledgement(player)) {
			info.cancel();
		}
	}
}
