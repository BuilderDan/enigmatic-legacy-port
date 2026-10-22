package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;

/**
 * Port-only. Stands in for Forge's EnchantmentLevelSetEvent: works out the Ring of the Seven
 * Curses bonus for this table once per slotsChanged, and MixinEnchantmentHelper adds it to each
 * getEnchantmentCost result made meanwhile. The client's access is NULL, so it gets no bonus.
 */
@Mixin(EnchantmentMenu.class)
public abstract class MixinEnchantmentMenu {
	@Shadow @Final private ContainerLevelAccess access;

	@Inject(method = "slotsChanged", at = @At("HEAD"))
	private void enigmaticlegacy$startEnchantingBonus(Container container, CallbackInfo info) {
		EnigmaticEventHandler.ENCHANTING_BONUS.set(this.access.evaluate(EnigmaticEventHandler::getEnchantingBonus, 0));
	}

	@Inject(method = "slotsChanged", at = @At("RETURN"))
	private void enigmaticlegacy$endEnchantingBonus(Container container, CallbackInfo info) {
		EnigmaticEventHandler.ENCHANTING_BONUS.remove();
	}
}
