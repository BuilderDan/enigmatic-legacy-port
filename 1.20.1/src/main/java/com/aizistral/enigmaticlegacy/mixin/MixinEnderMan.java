package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.objects.EntityPersistentData;

import net.minecraft.world.entity.monster.EnderMan;

/**
 * Port-only. Forge fires EntityTeleportEvent.EnderEntity in EnderMan.teleport(double, double, double);
 * upstream cancels it while The Ender Slayer's "ELTeleportBlock" is set (see MixinShulker).
 */
@Mixin(EnderMan.class)
public abstract class MixinEnderMan {
	@Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true, require = 1)
	private void enigmaticlegacy$onTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
		if (EntityPersistentData.get((EnderMan) (Object) this).contains("ELTeleportBlock")) {
			info.setReturnValue(false);
		}
	}
}
