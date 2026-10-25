package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.objects.EntityPersistentData;

import net.minecraft.world.entity.monster.Shulker;

/**
 * Upstream mixin. Port: reads the teleport block from EntityPersistentData instead of Forge's
 * getPersistentData(), so the class no longer extends AbstractGolem.
 */
@Mixin(Shulker.class)
public abstract class MixinShulker {
	@Inject(method = "teleportSomewhere", at = @At("HEAD"), cancellable = true, require = 1)
	private void onTeleportSomewhere(CallbackInfoReturnable<Boolean> info) {
		if (EntityPersistentData.get((Shulker) (Object) this).contains("ELTeleportBlock")) {
			info.setReturnValue(false);
		}
	}
}
