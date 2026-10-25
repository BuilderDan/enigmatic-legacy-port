package com.aizistral.enigmaticlegacy.items.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public abstract class ItemBase extends Item implements ICreativeTabMember {
	protected static final Random random = new Random();
	protected boolean isPlaceholder;

	public ItemBase() {
		this(ItemBase.getDefaultProperties());
	}

	public ItemBase(Properties props) {
		super(props);
		this.isPlaceholder = false;
	}

	@Override
	public void onCraftedBy(ItemStack stack, Player playerIn) {
		// Insert existential void here
	}

	// 26.3 port: vanilla tooltips take a Consumer; subclasses keep the old List overload.
	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
		List<Component> list = new ArrayList<>();
		this.appendHoverText(stack, context, list, flag);
		list.forEach(adder);
	}

	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		// NO-OP
	}

	// 26.3 port: vanilla inventoryTick is server-only and takes an EquipmentSlot;
	// subclasses keep the old (slot index, selected) overload.
	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		int index = -1;

		if (entity instanceof Player player) {
			NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems();

			for (int i = 0; i < items.size(); i++) {
				if (items.get(i) == stack) {
					index = i;
					break;
				}
			}
		}

		this.inventoryTick(stack, (Level) level, entity, index, slot == EquipmentSlot.MAINHAND);
	}

	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
		// NO-OP
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

	public static Properties getDefaultProperties() {
		Properties props = new Item.Properties();

		props.stacksTo(64);
		props.rarity(Rarity.COMMON);

		return props;
	}

	public static BlockHitResult rayTrace(Level worldIn, Player player, ClipContext.Fluid fluidMode) {
		return Item.getPlayerPOVHitResult(worldIn, player, fluidMode);
	}

	public Item setPlaceholder() {
		this.isPlaceholder = true;
		return this;
	}

	public boolean isPlaceholder() {
		return this.isPlaceholder;
	}

	@Override
	public Component getName(ItemStack stack) {
		Component superName = super.getName(stack);

		if (this.isPlaceholder) {
			if (superName instanceof MutableComponent)
				return ((MutableComponent)superName).withStyle(ChatFormatting.OBFUSCATED);
		}

		return superName;
	}

	protected static String minimizeNumber(double num) {
		return SuperpositionHandler.minimizeNumber(num);
	}

}
