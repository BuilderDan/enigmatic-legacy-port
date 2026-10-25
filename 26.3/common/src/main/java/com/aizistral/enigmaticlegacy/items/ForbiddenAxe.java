package com.aizistral.enigmaticlegacy.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.world.item.ItemStack;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseTool;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ForbiddenAxe extends ItemBase {
	public static Omniconfig.PerhapsParameter beheadingBase;
	public static Omniconfig.PerhapsParameter beheadingBonus;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("ForbiddenAxe");

		beheadingBase = builder
				.comment("Default chance to behead an enemy with Axe of Executioner. Defined as percentage.")
				.max(100)
				.getPerhaps("BeheadingBase", 10);

		beheadingBonus = builder
				.comment("Bonus percantage to beheading chance from each Looting level applied to Axe of Executioner.")
				.max(100)
				.getPerhaps("BeheadingBonus", 5);

		builder.popPrefix();
	}

	public ForbiddenAxe() {
		// Port: 1.21 weapons take their attack attributes from the item properties.
		// Port: 26.3 has no SwordItem; the sword properties add the tool component, attributes, durability and repair tag.
		super(ItemBaseTool.getDefaultProperties().rarity(Rarity.EPIC).fireResistant().sword(EnigmaticMaterials.FORBIDDEN_AXE, 6, -2.4F));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		Level worldIn = Minecraft.getInstance().level; // Port: TooltipContext has no level in 1.21
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.forbiddenAxe1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.forbiddenAxe2", ChatFormatting.GOLD, beheadingBonus.getValue().asPercentage() + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.forbiddenAxe3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		int looting = 0;

		if (worldIn != null) {
			looting = EnigmaticEnchantments.getLevel(Enchantments.LOOTING, stack);
		}

		// Port: Curios' looting bonus becomes the accessory layer's (Trinkets on Fabric).
		if (Minecraft.getInstance().player != null && worldIn != null) {
			looting += Services.ACCESSORIES.getLootingBonus(Minecraft.getInstance().player, worldIn.damageSources().generic(), null, looting);
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.forbiddenAxeBeheadingChance", ChatFormatting.GOLD, (beheadingBase.getValue().asPercentage() + (beheadingBonus.getValue().asPercentage() * looting)) + "%");
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState blockIn) {
		return false;
	}

	@Override
	public @Nullable CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

}
