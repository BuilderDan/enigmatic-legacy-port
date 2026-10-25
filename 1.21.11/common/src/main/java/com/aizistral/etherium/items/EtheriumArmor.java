package com.aizistral.etherium.items;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.world.item.equipment.ArmorType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.materials.EnigmaticArmorMaterials;
import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.etherium.core.EtheriumUtil;
import com.aizistral.etherium.core.IEtheriumConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Port: 26.3 has no ArmorItem. Properties#humanoidArmor adds the equippable component,
 * armor attributes, durability (factor 132) and the repair tag; the armor type is kept here.
 */
public class EtheriumArmor extends ItemBase {
	private final ArmorType type;

	public EtheriumArmor(ArmorType type) {
		super(EtheriumUtil.defaultProperties(EtheriumArmor.class).fireResistant()
				.humanoidArmor(EnigmaticArmorMaterials.ETHERIUM, type));
		this.type = type;
	}

	public ArmorType getType() {
		return this.type;
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}


	public static boolean hasFullSet(Player player) {
		if (player == null)
			return false;

		for (ItemStack stack : SuperpositionHandler.getArmorStacks(player)) {
			if (!(stack.getItem().getClass() == EtheriumArmor.class))
				return false;
		}

		return true;
	}

	public static boolean hasShield(Player player) {
		if (player != null)
			if (hasFullSet(player) && player.getHealth() / player.getMaxHealth()
					<= EtheriumConfigHandler.instance().getShieldThreshold(player).asMultiplier(false))
				return true;

		return false;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			EtheriumArmor armor = (EtheriumArmor) stack.getItem();
			if (armor.getType() == ArmorType.HELMET) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumHelmet1");
			} else if (armor.getType() == ArmorType.CHESTPLATE) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumChestplate1");
			} else if (armor.getType() == ArmorType.LEGGINGS) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumLeggings1");
			} else if (armor.getType() == ArmorType.BOOTS) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumBoots1");
			}
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		if (hasFullSet(Minecraft.getInstance().player) || (ItemNBTHelper.verifyExistance(stack, "forceDisplaySetBonus") && ItemNBTHelper.getBoolean(stack, "forceDisplaySetBonus", false))) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumArmorSetBonus1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumArmorSetBonus2", ChatFormatting.GOLD, EtheriumConfigHandler.instance().getShieldThreshold(Minecraft.getInstance().player).asPercentage() + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumArmorSetBonus3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumArmorSetBonus4", ChatFormatting.GOLD, EtheriumConfigHandler.instance().getShieldReduction().asPercentage() + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumArmorSetBonus5");

			//ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumArmorSetBonus6");
		}

		if (stack.isEnchanted()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		}

	}

}
