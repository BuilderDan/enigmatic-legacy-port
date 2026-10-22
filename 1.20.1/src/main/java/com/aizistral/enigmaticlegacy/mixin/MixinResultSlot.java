package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Forge fires {@code PlayerEvent.ItemCraftedEvent} from this method; this hook
 * calls the port's equivalent of upstream's handler at the same point.
 */
@Mixin(ResultSlot.class)
public abstract class MixinResultSlot {
	@Shadow @Final
	private CraftingContainer craftSlots;

	@Shadow @Final
	private Player player;

	@Inject(method = "checkTakeAchievements", at = @At("HEAD"))
	private void enigmaticlegacy$checkTakeAchievements(ItemStack stack, CallbackInfo info) {
		EnigmaticEventHandler.onItemCrafted(this.player, stack, this.craftSlots);
	}

}
