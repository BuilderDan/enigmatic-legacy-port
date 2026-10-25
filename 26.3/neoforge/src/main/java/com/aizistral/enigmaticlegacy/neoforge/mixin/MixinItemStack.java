package com.aizistral.enigmaticlegacy.neoforge.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.aizistral.enigmaticlegacy.EnigmaticSetup;
import com.aizistral.enigmaticlegacy.items.EldritchPan;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * Port-only. Stands in for Fabric's enchanting-allowed event: overrides the
 * IItemStackExtension defaults so disabled enchantments and the mod's own enchanting
 * rules apply on NeoForge too. Falls back to the item when the mod has no opinion.
 * <p>
 * Also overrides getAttributeModifiers, which NeoForge reads in place of the vanilla
 * component lookup that the common mixin wraps on Fabric.
 */
@Mixin(ItemStack.class)
public abstract class MixinItemStack {
	public boolean isPrimaryItemFor(Holder<Enchantment> enchantment) {
		ItemStack self = (ItemStack) (Object) this;
		Boolean allowed = EnigmaticSetup.allowEnchanting(enchantment, self, true);
		return allowed != null ? allowed : self.getItem().isPrimaryItemFor(self, enchantment);
	}

	public boolean supportsEnchantment(Holder<Enchantment> enchantment) {
		ItemStack self = (ItemStack) (Object) this;
		Boolean allowed = EnigmaticSetup.allowEnchanting(enchantment, self, false);
		return allowed != null ? allowed : self.getItem().supportsEnchantment(self, enchantment);
	}

	// Same as the IItemStackExtension default, except the Eldritch Pan supplies its kill-scaled modifiers.
	public ItemAttributeModifiers getAttributeModifiers() {
		ItemStack self = (ItemStack) (Object) this;
		ItemAttributeModifiers modifiers;

		if (self.getItem() instanceof EldritchPan pan) {
			modifiers = pan.getAttributeModifiers(self);
		} else {
			modifiers = self.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

			if (modifiers.modifiers().isEmpty()) {
				modifiers = self.getItem().getDefaultAttributeModifiers(self);
			}
		}

		return CommonHooks.computeModifiedAttributes(self, modifiers);
	}
}
