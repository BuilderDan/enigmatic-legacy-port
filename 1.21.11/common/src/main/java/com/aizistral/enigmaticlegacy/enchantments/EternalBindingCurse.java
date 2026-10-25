package com.aizistral.enigmaticlegacy.enchantments;

import com.aizistral.enigmaticlegacy.api.items.IBindable;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.world.item.ItemStack;

/**
 * Upstream enchantment. Port: enchantments are data-driven in 1.21 (definition in
 * data/enigmaticlegacy/enchantment/eternal_binding.json). The extra items upstream allowed in
 * canEnchant are granted through Fabric's EnchantmentEvents.ALLOW_ENCHANTING.
 */
public class EternalBindingCurse {

	/** TRUE: force allow, FALSE: deny, null: leave it to the enchantment definition. */
	public static Boolean canApplyTo(ItemStack stack) {
		if (stack.is(EnigmaticItems.CURSED_RING) || stack.is(EnigmaticItems.ESCAPE_SCROLL) ||
				stack.is(EnigmaticItems.ENIGMATIC_AMULET) || stack.is(EnigmaticItems.DESOLATION_RING))
			return false;

		return stack.getItem() instanceof IBindable ? true : null;
	}

	private EternalBindingCurse() {
		// NO-OP
	}
}
