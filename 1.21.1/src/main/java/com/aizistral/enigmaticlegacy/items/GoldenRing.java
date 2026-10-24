package com.aizistral.enigmaticlegacy.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class GoldenRing extends ItemBaseCurio {

	public GoldenRing() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.UNCOMMON));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			if (Minecraft.getInstance().player != null && SuperpositionHandler.isTheCursedOne(Minecraft.getInstance().player)) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.goldenRing1Cursed");
			} else {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.goldenRing1");
			}
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.LUCK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "golden_ring_luck"), 1.0, AttributeModifier.Operation.ADD_VALUE));

		return attributes;
	}

	// Port: upstream also overrides Forge's IForgeItem.makesPiglinsNeutral(ItemStack, LivingEntity),
	// which only applies to worn armor. The ring is never an armor item, so it is dropped;
	// MixinPiglinAi covers the accessory case through the method below.
	@Override
	public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
		return true;
	}

	/**
	 * Port: was an override of Forge's IForgeItem.isPiglinCurrency. Vanilla has no such hook,
	 * so it is a plain method that MixinPiglinAi checks.
	 */
	public boolean isPiglinCurrency(ItemStack stack) {
		return true;
	}

}
