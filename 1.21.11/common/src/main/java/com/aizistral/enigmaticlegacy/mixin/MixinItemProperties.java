package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.registries.PendingRegistryId;

import net.minecraft.world.item.Item;

/** Port: see {@link PendingRegistryId}. */
@Mixin(Item.Properties.class)
public class MixinItemProperties {

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(CallbackInfo info) {
		if (PendingRegistryId.item != null) {
			Item.Properties props = (Item.Properties) (Object) this;
			props.setId(PendingRegistryId.item);

			if (PendingRegistryId.blockItem) {
				props.useBlockDescriptionPrefix();
			}
		}
	}

}
