package com.aizistral.enigmaticlegacy.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.component.BlocksAttacks;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;

// Port: the EQUIPPABLE component (offhand slot) stands in for Forge's Mob.getEquipmentSlotForItem,
// which sends items that canPerformAction(SHIELD_BLOCK) to the offhand.
public class InfernalShield extends ItemBase implements ICursed {

	public InfernalShield() {
		super(ItemBase.getDefaultProperties().fireResistant().rarity(Rarity.EPIC).stacksTo(1).durability(10000)
				.enchantable(16)
				.repairable(EnigmaticMaterials.REPAIRS_INFERNAL_SHIELD)
				.equippableUnswappable(EquipmentSlot.OFFHAND)
				.delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> shieldBlocking(context, 0.0F))
				.component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK));
		DispenserBlock.registerBehavior(this, EquipmentDispenseItemBehavior.INSTANCE);
	}

	/**
	 * Port: 26.3 drives blocking, shield damage and axe disabling through the BLOCKS_ATTACKS
	 * component (vanilla shield values). Upstream's shield hooks in MixinPlayer are gone: a
	 * disableCooldownScale of 0 keeps axes from disabling the Infernal Shield.
	 */
	public static BlocksAttacks shieldBlocking(HolderLookup.Provider context, float disableCooldownScale) {
		return new BlocksAttacks(0.25F, disableCooldownScale,
				List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
				new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
				Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
				Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
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
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (SuperpositionHandler.isTheCursedOne(player)) {
			player.startUsingItem(hand);
			return InteractionResult.CONSUME;
		} else
			return InteractionResult.PASS;
	}

	// Port: Forge's canPerformAction(DEFAULT_SHIELD_ACTIONS) is replaced by the BLOCKS_ATTACKS
	// component (shieldBlocking), and the obsidian isValidRepairItem by the repairs/infernal_shield tag.

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
	// isEnchantable/getEnchantmentValue: enchantable(16) in the properties.

}
