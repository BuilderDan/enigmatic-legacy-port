package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.items.GoldenRing;
import com.aizistral.enigmaticlegacy.platform.Services;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Stands in for Forge's makesPiglinsNeutral / isPiglinCurrency item hooks,
 * which upstream's GoldenRing overrides and Forge patches into PiglinAi.
 */
@Mixin(PiglinAi.class)
public class MixinPiglinAi {

	@Inject(method = "isWearingGold", at = @At("HEAD"), cancellable = true)
	private static void onIsWearingGold(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if (Services.ACCESSORIES.makesPiglinsNeutral(entity)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "isBarterCurrency", at = @At("HEAD"), cancellable = true)
	private static void onIsBarterCurrency(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (stack.getItem() instanceof GoldenRing ring && ring.isPiglinCurrency(stack)) {
			info.setReturnValue(true);
		}
	}

}
