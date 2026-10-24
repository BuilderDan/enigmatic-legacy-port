package com.aizistral.enigmaticlegacy.items;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
// Port: Curios SlotContext -> platform SlotContext (Trinkets on Fabric).
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

public class EnchanterPearl extends ItemBaseCurio implements ICursed {

	public EnchanterPearl() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl4");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl5");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enchantersPearl6");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();

		if (slotContext.entity() instanceof Player player && SuperpositionHandler.isTheCursedOne(player)) {
			// Port: CuriosApi.getCuriosHelper() -> Services.ACCESSORIES.
			Services.ACCESSORIES.addSlotModifier(attributes, "charm",
					ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "enchanter_pearl_charm_slot"), 1, Operation.ADD_VALUE);
		}

		return attributes;
	}

	// Port: upstream's getAttributesTooltip override (clears the list) has no Trinkets counterpart; dropped.

	public boolean isPresent(Player player) {
		return SuperpositionHandler.isTheCursedOne(player) && SuperpositionHandler.hasCurio(player, this);
	}

	@Override
	public boolean canEquip(SlotContext context, ItemStack stack) {
		return context.entity() instanceof Player player && SuperpositionHandler.isTheCursedOne(player)
				&& super.canEquip(context, stack);
	}

}
