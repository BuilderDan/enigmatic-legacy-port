package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.registries.PendingRegistryId;

import net.minecraft.world.level.block.state.BlockBehaviour;

/** Port: see {@link PendingRegistryId}. */
@Mixin(BlockBehaviour.Properties.class)
public class MixinBlockProperties {

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo info) {
		if (PendingRegistryId.block != null) {
			((BlockBehaviour.Properties) (Object) this).setId(PendingRegistryId.block);
		}
	}

}
