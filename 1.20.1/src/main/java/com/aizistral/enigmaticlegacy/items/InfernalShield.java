package com.aizistral.enigmaticlegacy.items;

import java.util.List;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;

// Port: implements Equipable (offhand slot) because Forge's Mob.getEquipmentSlotForItem
// sends items that canPerformAction(SHIELD_BLOCK) to the offhand, and vanilla does not.
public class InfernalShield extends ItemBase implements ICursed, Vanishable, Equipable {

	public InfernalShield() {
		super(ItemBase.getDefaultProperties().fireResistant().rarity(Rarity.EPIC).stacksTo(1).durability(10000));
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield1");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield2");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield3");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield4");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield5");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield6");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield7");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield8");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield9");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield10");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.infernalShield11");
		} else {
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(tooltip);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (SuperpositionHandler.isTheCursedOne(player)) {
			player.startUsingItem(hand);
			return InteractionResultHolder.consume(stack);
		} else
			return InteractionResultHolder.pass(stack);
	}

	// Port: Forge's canPerformAction(DEFAULT_SHIELD_ACTIONS) has no Fabric equivalent. Vanilla
	// blocking only needs UseAnim.BLOCK; MixinPlayer makes shield durability and axe disabling
	// treat this item like a shield.

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.OFFHAND;
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack repairStack) {
		if (repairStack.getItem() instanceof BlockItem blockItem)
			return blockItem.getBlock() == Blocks.OBSIDIAN;
		else
			return super.isValidRepairItem(stack, repairStack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity holder, int slot, boolean hand) {
		if (holder instanceof ServerPlayer player && player.isOnFire() && SuperpositionHandler.isTheCursedOne(player)) {
			if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
				if (!player.isInLava()) {
					player.clearFire();
				}
			}
		}
	}

	// Port: Forge's canApplyAtEnchantingTable override (Unbreaking) is not needed: vanilla's
	// BREAKABLE category already lets Unbreaking onto any damageable item.

	@Override
	public boolean isEnchantable(ItemStack pStack) {
		return true;
	}

	// Port: vanilla getEnchantmentValue() has no stack parameter (Forge's overload does).
	@Override
	public int getEnchantmentValue() {
		return 16;
	}

}
