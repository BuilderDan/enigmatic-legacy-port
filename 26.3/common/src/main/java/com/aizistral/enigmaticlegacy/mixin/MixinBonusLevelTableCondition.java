package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;

/**
 * Port-only. Same as {@link MixinApplyBonusCount}, for Fortune-scaled drop chances (flint from
 * gravel, saplings and apples from leaves).
 */
@Mixin(BonusLevelTableCondition.class)
public abstract class MixinBonusLevelTableCondition {
	@Shadow @Final
	Holder<Enchantment> enchantment;

	@ModifyExpressionValue(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemInstance;)I"))
	private int enigmaticlegacy$addAccessoryFortune(int level, @Local(argsOnly = true) LootContext context) {
		return EnigmaticEventHandler.addAccessoryFortune(level, this.enchantment, context);
	}
}
