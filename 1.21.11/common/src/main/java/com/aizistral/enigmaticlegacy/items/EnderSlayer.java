package com.aizistral.enigmaticlegacy.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.client.Minecraft;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseTool;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class EnderSlayer extends ItemBase implements ICursed {
	public static final List<Identifier> endDwellers = new ArrayList<>();

	public static Omniconfig.IntParameter attackDamage;
	public static Omniconfig.DoubleParameter attackSpeed;
	public static Omniconfig.PerhapsParameter endDamageBonus;
	public static Omniconfig.PerhapsParameter endKnockbackBonus;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("EnderSlayer");

		attackDamage = builder
				.comment("Attack damage of The Ender Slayer, actual damage shown in tooltip will be is 4 + this_value.")
				.max(32768)
				.getInt("AttackDamage", 4);

		attackSpeed = builder
				.comment("Attack speed of The Ender Slayer.")
				.minMax(32768)
				.getDouble("AttackSpeed", -2.6);

		endDamageBonus = builder
				.comment("Attack damage bonus of The Ender Slayer against dwellers of The End.")
				.getPerhaps("EndDamageBonus", 150);

		endKnockbackBonus = builder
				.comment("Knockback bonus of The Ender Slayer against dwellers of The End.")
				.getPerhaps("EndKnockbackBonus", 600);

		builder.popPrefix();

		endDwellers.clear();
		String[] list = builder.config.getStringList("EnderSlayerEndDwellers", builder.getCurrentCategory(), new String[0],
				"List of entities that should be considered dwellers of The End by The Ender Slayer."
						+ " Examples: minecraft:iron_golem, minecraft:zombified_piglin");

		Arrays.stream(list).forEach(entry -> endDwellers.add(Identifier.parse(entry)));
	}

	public EnderSlayer() {
		// Port: 1.21 weapons take their attack attributes from the item properties.
		// Port: 26.3 has no SwordItem; the sword properties add the tool component, attributes, durability and repair tag.
		super(ItemBaseTool.getDefaultProperties().rarity(Rarity.EPIC).fireResistant()
				.sword(EnigmaticMaterials.ENDER_SLAYER, attackDamage.getValue(), (float) attackSpeed.getValue()));
	}

	public boolean isEndDweller(LivingEntity entity) {
		if (entity instanceof EnderMan || entity instanceof EnderDragon || entity instanceof Shulker || entity instanceof Endermite)
			return true;
		else
			return endDwellers.stream().anyMatch(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())::equals);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer3", ChatFormatting.GOLD, endDamageBonus + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer4", ChatFormatting.GOLD, endKnockbackBonus + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer5");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer6");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer7");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer8");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer9");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer10");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer11");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer12");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer13");
		} else {
			String stackName = stack.getDisplayName().getString();

			if (stackName.substring(1, stackName.length()-1).equals("§dThe Ender Slapper")) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer1_alt");
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer2_alt");
			} else {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer1");
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enderSlayer2");
			}
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

	@Override
	public @Nullable CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

}
