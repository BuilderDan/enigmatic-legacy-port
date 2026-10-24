package com.aizistral.enigmaticlegacy.platform.accessory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Multimap;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Port-only. Loader-neutral access to accessory slots (Curios on Forge, Trinkets on Fabric).
 * Slot ids are upstream's Curios ids: "charm", "ring", "back", "spellstone", "scroll".
 */
public interface AccessorySlots {
	/** Calls the consumer for every non-empty stack the entity has in its accessory slots. */
	void forEachEquipped(LivingEntity entity, BiConsumer<SlotContext, ItemStack> consumer);

	/** The first non-empty equipped stack matching the filter, or null. */
	@Nullable
	ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> filter);

	/** Every stack in slots of the given type, empty ones included. */
	List<ItemStack> getSlotStacks(LivingEntity entity, String slotId);

	/** True if the entity has no slots of the given type at all. */
	boolean isSlotLocked(LivingEntity entity, String slotId);

	/**
	 * Adds a permanent +1 slot modifier to the given slot type ("scroll", "spellstone", "ring"
	 * or "charm"). Returns false if the entity already had that modifier.
	 */
	boolean unlockSlot(LivingEntity entity, String slotId, ResourceLocation modifierId);

	/**
	 * Port: was {@code CuriosApi.getCuriosHelper().addSlotModifier}. Adds a modifier to an
	 * item's attribute map that changes how many slots of the given type ("scroll",
	 * "spellstone", "ring" or "charm") the wearer has, while the item is equipped.
	 */
	void addSlotModifier(Multimap<Holder<Attribute>, AttributeModifier> map, String slotId, ResourceLocation id, double amount,
			AttributeModifier.Operation operation);

	/** Empties every accessory slot holding the item. Returns how many stacks were removed. */
	int destroy(LivingEntity entity, Item item);

	/**
	 * Puts the stack into the first empty slot that accepts it, server side only.
	 * The item must implement {@link IAccessoryItem}.
	 */
	boolean tryForceEquip(LivingEntity entity, ItemStack stack);

	/** Hooks an {@link IAccessoryItem} into the accessory mod. Call once per item. */
	void registerAccessory(Item item);

	default boolean isEquipped(LivingEntity entity, Item item) {
		return this.findFirst(entity, stack -> stack.is(item)) != null;
	}

	default List<ItemStack> findAll(LivingEntity entity, Predicate<ItemStack> filter) {
		List<ItemStack> list = new ArrayList<>();
		this.forEachEquipped(entity, (context, stack) -> {
			if (filter.test(stack)) {
				list.add(stack);
			}
		});
		return list;
	}

	/** Sum of {@link IAccessoryItem#getFortuneLevel} over equipped accessories. */
	default int getFortuneBonus(LivingEntity entity, @Nullable LootContext lootContext) {
		int[] bonus = new int[1];
		this.forEachEquipped(entity, (context, stack) -> {
			if (stack.getItem() instanceof IAccessoryItem item) {
				bonus[0] += item.getFortuneLevel(context, lootContext, stack);
			}
		});
		return bonus[0];
	}

	/** Sum of {@link IAccessoryItem#getLootingLevel} over equipped accessories. */
	default int getLootingBonus(LivingEntity entity, @Nullable DamageSource source, @Nullable LivingEntity target, int baseLooting) {
		int[] bonus = new int[1];
		this.forEachEquipped(entity, (context, stack) -> {
			if (stack.getItem() instanceof IAccessoryItem item) {
				bonus[0] += item.getLootingLevel(context, source, target, baseLooting, stack);
			}
		});
		return bonus[0];
	}

	default boolean makesPiglinsNeutral(LivingEntity entity) {
		boolean[] neutral = new boolean[1];
		this.forEachEquipped(entity, (context, stack) -> {
			if (stack.getItem() instanceof IAccessoryItem item && item.makesPiglinsNeutral(context, stack)) {
				neutral[0] = true;
			}
		});
		return neutral[0];
	}
}
