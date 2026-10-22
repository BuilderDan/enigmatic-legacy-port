package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.fabric.platform.TrinketAdapter;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Port-only. Marks the tooltip pass so {@link TrinketAdapter} can hide the "When worn"
 * attribute lines of accessories that upstream cleared through Curios' getAttributesTooltip.
 */
@Mixin(ItemStack.class)
public class MixinItemStack {
	@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private void enigmaticlegacy$beginTooltip(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> info) {
		TrinketAdapter.TOOLTIP_PASS.get()[0]++;
	}

	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void enigmaticlegacy$endTooltip(Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> info) {
		int[] depth = TrinketAdapter.TOOLTIP_PASS.get();
		depth[0] = Math.max(0, depth[0] - 1);
	}
}
