package com.aizistral.enigmaticlegacy.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.items.TheAcknowledgment;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Port-only. Stands in for Curios' looting hook (Forge's LootingLevelEvent), which adds each
 * worn curio's getLootingLevel to the killer's looting level. Vanilla's getMobLooting has no
 * victim or damage source, so both are passed as null.
 * <p>
 * Also stands in for TheAcknowledgment's Forge canApplyAtEnchantingTable override: the
 * enchanting table may offer Bane of Arthropods for it.
 * <p>
 * Also adds the Ring of the Seven Curses enchanting bonus that MixinEnchantmentMenu works out
 * (Forge's EnchantmentLevelSetEvent, which fires right after getEnchantmentCost).
 * <p>
 * Also the upstream hasBindingCurse inject: Curse of Eternal Binding counts as Curse of Binding.
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

	@Inject(method = "getEnchantmentCost", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$addEnchantingBonus(RandomSource random, int slot, int power, ItemStack stack, CallbackInfoReturnable<Integer> info) {
		Integer bonus = EnigmaticEventHandler.ENCHANTING_BONUS.get();
		if (bonus != null && bonus != 0) {
			info.setReturnValue(info.getReturnValueI() + bonus);
		}
	}

	@Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$addAcknowledgmentBane(int level, ItemStack stack, boolean treasure, CallbackInfoReturnable<List<EnchantmentInstance>> info) {
		if (!(stack.getItem() instanceof TheAcknowledgment))
			return;

		List<EnchantmentInstance> results = info.getReturnValue();
		Enchantment bane = Enchantments.BANE_OF_ARTHROPODS;

		for (EnchantmentInstance instance : results) {
			if (instance.enchantment == bane)
				return;
		}

		for (int i = bane.getMaxLevel(); i > bane.getMinLevel() - 1; i--) {
			if (level >= bane.getMinCost(i) && level <= bane.getMaxCost(i)) {
				List<EnchantmentInstance> copy = new ArrayList<>(results);
				copy.add(new EnchantmentInstance(bane, i));
				info.setReturnValue(copy);
				return;
			}
		}
	}

	@Inject(method = "hasBindingCurse", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$hasEternalBinding(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && EnigmaticEnchantments.ETERNAL_BINDING != null
				&& EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.ETERNAL_BINDING, stack) > 0) {
			info.setReturnValue(true);
		}
	}
}
