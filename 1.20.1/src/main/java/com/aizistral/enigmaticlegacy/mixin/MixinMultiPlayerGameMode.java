package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

/**
 * Port-only. Adds the port's block reach attribute to the client's block pick range, as Forge
 * does with BLOCK_REACH.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {
	@Shadow @Final
	private Minecraft minecraft;

	@Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$addBlockReach(CallbackInfoReturnable<Float> info) {
		if (this.minecraft.player != null) {
			double bonus = EnigmaticAttributes.getBlockReach(this.minecraft.player);

			if (bonus > 0) {
				info.setReturnValue((float) (info.getReturnValueF() + bonus));
			}
		}
	}
}
