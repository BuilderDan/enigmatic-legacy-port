package com.aizistral.enigmaticlegacy.mixin;

import net.minecraft.world.damagesource.DamageSource;
import com.aizistral.enigmaticlegacy.items.EvilEssence;
import com.aizistral.enigmaticlegacy.items.EvilIngot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryTooltips;
import com.aizistral.enigmaticlegacy.items.EldritchPan;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Port-only. Marks the tooltip pass so {@link TrinketAdapter} can hide the "When worn"
 * attribute lines of accessories that upstream cleared through Curios' getAttributesTooltip.
 * <p>
 * Also stands in for Forge's stack-aware Item.getAttributeModifiers(slot, stack), which the
 * Eldritch Pan uses to scale its damage and armor with kills.
 * <p>
 * Item.canBeHurtBy is gone in 1.21, so canBeHurtBy here asks Evil Essence and Evil Ingot instead.
 */
@Mixin(ItemStack.class)
public class MixinItemStack {
	@Inject(method = "getTooltipLines", at = @At("HEAD"))
	private void enigmaticlegacy$beginTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> info) {
		AccessoryTooltips.TOOLTIP_PASS.get()[0]++;
	}

	@Inject(method = "getTooltipLines", at = @At("RETURN"))
	private void enigmaticlegacy$endTooltip(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> info) {
		int[] depth = AccessoryTooltips.TOOLTIP_PASS.get();
		depth[0] = Math.max(0, depth[0] - 1);
	}

	// Port: 1.21.11 reads the attribute modifiers component in forEachModifier; the pan swaps in its kill-scaled modifiers.
	// Optional: NeoForge replaces this read with getAttributeModifiers(), which its own MixinItemStack covers.
	@WrapOperation(method = "forEachModifier", require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object enigmaticlegacy$stackModifiers(ItemStack stack, DataComponentType<?> type, Object fallback, Operation<Object> original) {
		if (type == DataComponents.ATTRIBUTE_MODIFIERS && stack.getItem() instanceof EldritchPan pan)
			return pan.getAttributeModifiers(stack);

		return original.call(stack, type, fallback);
	}

	@Inject(method = "canBeHurtBy", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$canBeHurtBy(DamageSource source, CallbackInfoReturnable<Boolean> info) {
		Item item = ((ItemStack) (Object) this).getItem();

		if (item instanceof EvilEssence essence) {
			info.setReturnValue(essence.canBeHurtBy(source));
		} else if (item instanceof EvilIngot ingot) {
			info.setReturnValue(ingot.canBeHurtBy(source));
		}
	}
}
