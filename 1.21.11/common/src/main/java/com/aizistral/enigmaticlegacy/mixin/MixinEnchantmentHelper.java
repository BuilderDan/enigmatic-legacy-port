package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.platform.Services;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Port-only. Stands in for Curios' looting hook (Forge's LootingLevelEvent), which adds each
 * worn curio's getLootingLevel to the killer's looting level. In 1.21 looting is read through
 * getEnchantmentLevel(Holder, LivingEntity); it has no victim or damage source, so both are
 * passed as null.
 * <p>
 * Also adds the Ring of the Seven Curses enchanting bonus that MixinEnchantmentMenu works out
 * (Forge's EnchantmentLevelSetEvent, which fires right after getEnchantmentCost).
 * <p>
 * The EldritchPan and TheAcknowledgment table rules and Eternal Binding's binding behavior moved
 * to Fabric's ALLOW_ENCHANTING event and the enchantment's prevent_armor_change effect.
 */
@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {
	@Inject(method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$addAccessoryLooting(Holder<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> info) {
		if (!enchantment.is(Enchantments.LOOTING))
			return;

		int base = info.getReturnValueI();
		int bonus = Services.ACCESSORIES.getLootingBonus(entity, null, null, base);
		if (bonus != 0) {
			info.setReturnValue(base + bonus);
		}
	}

	@Inject(method = "getEnchantmentCost", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$addEnchantingBonus(RandomSource random, int slot, int power, ItemStack stack, CallbackInfoReturnable<Integer> info) {
		Integer bonus = EnigmaticEventHandler.ENCHANTING_BONUS.get();
		if (bonus != null && bonus != 0) {
			info.setReturnValue(info.getReturnValueI() + bonus);
		}
	}
}
