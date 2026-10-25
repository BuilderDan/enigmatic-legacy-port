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
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;

/**
 * Port-only. Adds worn accessories' Fortune where vanilla reads the tool's Fortune level for
 * ore drops, in place of Curios' fortune loot modifier. See also {@link MixinBonusLevelTableCondition}.
 */
@Mixin(ApplyBonusCount.class)
public abstract class MixinApplyBonusCount {
	@Shadow @Final
	Holder<Enchantment> enchantment;

	@ModifyExpressionValue(method = "run", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getItemEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/item/ItemInstance;)I"))
	private int enigmaticlegacy$addAccessoryFortune(int level, @Local(argsOnly = true) LootContext context) {
		return EnigmaticEventHandler.addAccessoryFortune(level, this.enchantment, context);
	}
}
