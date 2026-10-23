package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.EldritchPan;
import com.aizistral.enigmaticlegacy.items.TheAcknowledgment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Port-only. Stands in for TheAcknowledgment's Forge isBookEnchantable override: lets Bane of
 * Arthropods be applied to The Acknowledgment in an anvil.
 * <p>
 * Also stands in for CursedRing's isBookEnchantable override, which refuses books carrying Curse
 * of Vanishing. Port: the anvil drops just that enchantment instead of refusing the whole book.
 * <p>
 * Also stands in for EldritchPan's isBookEnchantable override: the pan takes whatever a diamond
 * sword takes.
 */
@Mixin(Enchantment.class)
public abstract class MixinEnchantment {
	@Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$acknowledgmentBane(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && stack.getItem() instanceof TheAcknowledgment
				&& (Object) this == Enchantments.BANE_OF_ARTHROPODS) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$panAsSword(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (stack.getItem() instanceof EldritchPan) {
			info.setReturnValue(((Enchantment) (Object) this).canEnchant(new ItemStack(Items.DIAMOND_SWORD)));
		}
	}

	@Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$cursedRingVanishing(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ() && stack.getItem() instanceof CursedRing
				&& (Object) this == Enchantments.VANISHING_CURSE) {
			info.setReturnValue(false);
		}
	}
}
