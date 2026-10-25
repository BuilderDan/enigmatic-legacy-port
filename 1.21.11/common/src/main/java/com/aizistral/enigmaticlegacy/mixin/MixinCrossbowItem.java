package com.aizistral.enigmaticlegacy.mixin;

import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
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

	// Port: 1.21 builds crossbow arrows in createProjectile, which may also return a firework rocket.
	@Inject(method = "createProjectile", at = @At("RETURN"))
	private void onGetArrow(Level level, LivingEntity shooter, ItemStack crossbowStack, ItemStack ammoStack, boolean isCrit, CallbackInfoReturnable<Projectile> info) {
		if (!(info.getReturnValue() instanceof AbstractArrow arrow))
			return;

		int sharpshooter = EnigmaticEnchantments.getLevel(EnigmaticEnchantments.SHARPSHOOTER, crossbowStack);

		arrow.setBaseDamage(((AccessorAbstractArrow) arrow).enigmaticlegacy$getBaseDamage() + (0.8 * sharpshooter));

		if (EnigmaticEnchantments.getLevel(EnigmaticEnchantments.CEASELESS, crossbowStack) > 0) {
			arrow.pickup = Pickup.CREATIVE_ONLY;
		}
	}

}
