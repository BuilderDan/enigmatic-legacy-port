package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.platform.Services;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Port-only. Stands in for Curios' looting hook (Forge's LootingLevelEvent), which adds each
 * worn curio's getLootingLevel to the killer's looting level. Vanilla's getMobLooting has no
 * victim or damage source, so both are passed as null.
 */
@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {
	@Inject(method = "getMobLooting", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$addAccessoryLooting(LivingEntity entity, CallbackInfoReturnable<Integer> info) {
		int base = info.getReturnValueI();
		int bonus = Services.ACCESSORIES.getLootingBonus(entity, null, null, base);
		if (bonus != 0) {
			info.setReturnValue(base + bonus);
		}
	}
}
