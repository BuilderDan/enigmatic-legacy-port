package com.aizistral.enigmaticlegacy.items;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AscensionAmulet extends EnigmaticAmulet {

	public AscensionAmulet() {
		this(getDefaultProperties().rarity(Rarity.EPIC).fireResistant(), "ascension_amulet");
	}

	protected AscensionAmulet(Properties properties, String name) {
		super(properties, name);
	}

	@Override
	public List<ItemStack> getCreativeTabStacks() {
		return ImmutableList.of(new ItemStack(this));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
		String name = ItemNBTHelper.getString(stack, "Inscription", null);

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown() && this.isVesselEnabled()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletShift1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletShift2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletShift3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletShift4");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletShift5");
		} else {
			if (this.isVesselEnabled()) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
			}

			if (name != null) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletInscription", ChatFormatting.DARK_RED, name);
			}
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		this.addAttributes(list, stack);
	}

	@Environment(EnvType.CLIENT)
	@Override
	protected void addAttributes(List<Component> list, ItemStack stack) {
		ItemLoreHelper.addLocalizedFormattedString(list, "curios.modifiers.charm", ChatFormatting.GOLD);

		for (AmuletColor color : AmuletColor.values())
			if (color != AmuletColor.RED) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletModifier" + color);
			} else {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticAmuletModifierRED", ChatFormatting.GOLD, minimizeNumber(damageBonus.getValue()));
			}
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
		return super.getAllModifiers(slotContext.entity() instanceof Player player ? player : null);
	}

	// Port: upstream's getAttributesTooltip override (clears Curios' attribute lines) is dropped;
	// the port's accessory tooltips do not list these modifiers.

	@Override
	public boolean canEquip(SlotContext context, ItemStack stack) {
		return !SuperpositionHandler.hasCurio(context.entity(), EnigmaticItems.ENIGMATIC_AMULET)
				&& !SuperpositionHandler.hasCurio(context.entity(), EnigmaticItems.ASCENSION_AMULET);
	}

}
