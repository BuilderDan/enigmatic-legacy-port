package com.aizistral.enigmaticlegacy.platform.accessory;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Port-only stand-in for Curios' ICurioItem. Items implementing it are hooked into the
 * loader's accessory mod by {@link AccessorySlots#registerAccessory}. Method names and
 * defaults follow Curios, so ported item code keeps upstream's overrides.
 */
public interface IAccessoryItem {
	default void curioTick(SlotContext context, ItemStack stack) {
		// NO-OP
	}

	/** The accessory mod does not report the previous stack; prevStack is always empty. */
	default void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
		// NO-OP
	}

	/** The accessory mod does not report the new stack; newStack is always empty. */
	default void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
		// NO-OP
	}

	default boolean canEquip(SlotContext context, ItemStack stack) {
		return true;
	}

	default boolean canUnequip(SlotContext context, ItemStack stack) {
		return true;
	}

	default boolean canEquipFromUse(SlotContext context, ItemStack stack) {
		return false;
	}

	default Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext context, UUID uuid, ItemStack stack) {
		return LinkedHashMultimap.create();
	}

	default DropRule getDropRule(SlotContext context, @Nullable DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
		return DropRule.DEFAULT;
	}

	default int getFortuneLevel(SlotContext context, @Nullable LootContext lootContext, ItemStack stack) {
		return 0;
	}

	default int getLootingLevel(SlotContext context, @Nullable DamageSource source, @Nullable LivingEntity target, int baseLooting, ItemStack stack) {
		return 0;
	}

	default boolean makesPiglinsNeutral(SlotContext context, ItemStack stack) {
		return false;
	}

	default SoundInfo getEquipSound(SlotContext context, ItemStack stack) {
		return new SoundInfo(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
	}
}
