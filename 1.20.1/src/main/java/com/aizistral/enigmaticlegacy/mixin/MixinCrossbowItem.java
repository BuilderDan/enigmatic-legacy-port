package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Upstream mixin. Port: Forge's {@code ItemStack.getEnchantmentLevel} becomes
 * {@code EnchantmentHelper.getItemEnchantmentLevel}.
 */
@Mixin(CrossbowItem.class)
public class MixinCrossbowItem {

	@Inject(method = "getArrow", at = @At("RETURN"), cancellable = true)
	private static void onGetArrow(Level level, LivingEntity shooter, ItemStack crossbowStack, ItemStack ammoStack, CallbackInfoReturnable<AbstractArrow> info) {
		AbstractArrow arrow = info.getReturnValue();
		int sharpshooter = EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.SHARPSHOOTER, crossbowStack);

		arrow.setBaseDamage(arrow.getBaseDamage() + (0.8 * sharpshooter));

		if (EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.CEASELESS, crossbowStack) > 0) {
			arrow.pickup = Pickup.CREATIVE_ONLY;
		}
	}

}
