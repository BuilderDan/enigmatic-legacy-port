package com.aizistral.enigmaticlegacy.items;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class IronRing extends ItemBaseCurio {

	public IronRing() {
		super(ItemBaseCurio.getDefaultProperties());
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext context, Identifier id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> atts = HashMultimap.create();
		atts.put(Attributes.ARMOR, new AttributeModifier(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "iron_ring_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		return atts;
	}

}
