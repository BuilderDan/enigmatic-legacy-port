package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.enchantments.EternalBindingCurse;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Upstream registry. Port: only Curse of Eternal Binding so far; the other enchantments follow
 * with their items.
 */
public class EnigmaticEnchantments extends AbstractRegistry<Enchantment> {
	private static final EnigmaticEnchantments INSTANCE = new EnigmaticEnchantments();

	@ConfigurableItem("Curse of Eternal Binding")
	@ObjectHolder(value = MODID + ":eternal_binding", registryName = "enchantment")
	public static EternalBindingCurse ETERNAL_BINDING;

	private EnigmaticEnchantments() {
		super(BuiltInRegistries.ENCHANTMENT);
		this.register("eternal_binding", () -> new EternalBindingCurse(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));
	}

	/** Port: forces class loading, which registers the enchantments. Call after the config loads. */
	public static void init() {
		// NO-OP
	}
}
