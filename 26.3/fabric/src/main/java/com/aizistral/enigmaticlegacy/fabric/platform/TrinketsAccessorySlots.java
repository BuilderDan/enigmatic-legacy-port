package com.aizistral.enigmaticlegacy.fabric.platform;

import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryDrops;
import com.google.common.collect.Multimap;
import com.aizistral.enigmaticlegacy.platform.accessory.AccessorySlots;
import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

import eu.pb4.trinkets.api.SlotAttributes;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketSlotUtils;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import eu.pb4.trinkets.api.event.TrinketDropCallback;
import eu.pb4.trinkets.impl.TrinketUtilities;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. {@link AccessorySlots} on Trinkets Updated. Trinkets slot names equal upstream's
 * Curios slot ids; see {@code data/trinkets/} for the groups they live in.
 */
public class TrinketsAccessorySlots implements AccessorySlots {
	/** The Trinkets group that gets the extra slot when a slot type is unlocked. */
	private static final Map<String, String> UNLOCK_GROUPS = Map.of(
			"scroll", "head",
			"spellstone", "legs",
			"ring", "hand",
			"charm", "chest");

	/** Applies {@link AccessoryDrops} overrides. Called from the main entry point. */
	public static void init() {
		TrinketDropCallback.EVENT.register((rule, stack, access, entity) -> {
			DropRule override = AccessoryDrops.getOverride(entity, stack);
			return override != null ? TrinketAdapter.toTrinkets(override) : rule;
		});

		// Port: Curios equips an ICurioItem on right-click when canEquipFromUse allows it.
		// Trinkets hooks the head of Item.use, which items overriding use() skip, so the
		// same check runs here.
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!player.isSpectator() && stack.getItem() instanceof IAccessoryItem accessory
					&& accessory.canEquipFromUse(new SlotContext("", player, -1, false, true), stack)) {
				InteractionResult result = TrinketUtilities.swapWithEquipmentSlot(stack, player);

				if (result.consumesAction())
					return result;
			}

			return InteractionResult.PASS;
		});
	}

	private static void forEachInventory(LivingEntity entity, Consumer<TrinketInventory> consumer) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);

		if (attachment != null) {
			attachment.getInventory().values().forEach(slots -> slots.values().forEach(consumer));
		}
	}

	@Override
	public void forEachEquipped(LivingEntity entity, BiConsumer<SlotContext, ItemStack> consumer) {
		forEachInventory(entity, inventory -> {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				ItemStack stack = inventory.getItem(i);

				if (!stack.isEmpty()) {
					consumer.accept(TrinketAdapter.context(inventory.getOrCreateSlotAccess(i), entity), stack);
				}
			}
		});
	}

	@Override
	@Nullable
	public ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> filter) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);

		if (attachment == null)
			return null;

		return attachment.findFirst(stack -> !stack.isEmpty() && filter.test(stack))
				.map(TrinketSlotAccess::get).orElse(null);
	}

	@Override
	public List<ItemStack> getSlotStacks(LivingEntity entity, String slotId) {
		List<ItemStack> stacks = new ArrayList<>();
		forEachInventory(entity, inventory -> {
			if (inventory.slotType().name().equals(slotId)) {
				for (int i = 0; i < inventory.getContainerSize(); i++) {
					stacks.add(inventory.getItem(i));
				}
			}
		});
		return stacks;
	}

	@Override
	public boolean isSlotLocked(LivingEntity entity, String slotId) {
		boolean[] open = new boolean[1];
		forEachInventory(entity, inventory -> {
			if (inventory.slotType().name().equals(slotId) && inventory.getContainerSize() > 0) {
				open[0] = true;
			}
		});
		return !open[0];
	}

	@Override
	public boolean unlockSlot(LivingEntity entity, String slotId, Identifier modifierId) {
		String group = UNLOCK_GROUPS.get(slotId);

		if (group == null)
			throw new IllegalArgumentException("Slot type '" + slotId + "' cannot be unlocked");

		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		TrinketInventory inventory = attachment == null ? null : attachment.getInventory().getOrDefault(group, Map.of()).get(slotId);

		if (inventory == null || inventory.getSlotCountModifier(modifierId) != null)
			return false;

		inventory.addSlotCountModifier(new AttributeModifier(modifierId, 1, AttributeModifier.Operation.ADD_VALUE));
		inventory.updateSlotCount();
		return true;
	}

	@Override
	public void addSlotModifier(Multimap<Holder<Attribute>, AttributeModifier> map, String slotId, Identifier id, double amount,
			AttributeModifier.Operation operation) {
		String group = UNLOCK_GROUPS.get(slotId);

		if (group == null)
			throw new IllegalArgumentException("Unknown slot type '" + slotId + "'");

		map.put(SlotAttributes.createAttributeForSlot(group + "/" + slotId), new AttributeModifier(id, amount, operation));
	}

	@Override
	public int destroy(LivingEntity entity, Item item) {
		int[] removed = new int[1];
		forEachInventory(entity, inventory -> {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (inventory.getItem(i).is(item)) {
					inventory.setItem(i, ItemStack.EMPTY);
					removed[0]++;
				}
			}
		});
		return removed[0];
	}

	@Override
	public boolean tryForceEquip(LivingEntity entity, ItemStack stack) {
		if (entity.level().isClientSide() || stack.isEmpty())
			return false;

		List<TrinketInventory> inventories = new ArrayList<>();
		forEachInventory(entity, inventories::add);

		for (TrinketInventory inventory : inventories) {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (!inventory.getItem(i).isEmpty()) {
					continue;
				}

				TrinketSlotAccess access = inventory.getOrCreateSlotAccess(i);

				// mayPlace checks the slot validators (item tags) and canEquip; isEquipable alone skips the tags
				if (TrinketSlotUtils.mayPlace(access, stack)) {
					inventory.setItem(i, stack.copy());
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void registerAccessory(Item item) {
		if (!(item instanceof IAccessoryItem accessory))
			throw new IllegalArgumentException(item + " does not implement IAccessoryItem");

		TrinketCallback.setCallback(item, new TrinketAdapter(accessory));
	}
}
