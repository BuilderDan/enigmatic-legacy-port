package com.aizistral.enigmaticlegacy.fabric.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryDrops;
import com.aizistral.enigmaticlegacy.platform.accessory.AccessorySlots;
import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. {@link AccessorySlots} on Trinkets. Trinkets slot names equal upstream's
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
		TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
			DropRule override = AccessoryDrops.getOverride(entity, stack);
			return override != null ? TrinketAdapter.toTrinkets(override) : rule;
		});

		// Port: Curios equips an ICurioItem on right-click when canEquipFromUse allows it.
		// Trinkets does that only for its own TrinketItem, so the same check runs here.
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!player.isSpectator() && stack.getItem() instanceof IAccessoryItem accessory
					&& accessory.canEquipFromUse(new SlotContext("", player, -1, false, true), stack)
					&& TrinketItem.equipItem(player, stack))
				return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());

			return InteractionResultHolder.pass(ItemStack.EMPTY);
		});
	}

	private static void forEachInventory(LivingEntity entity, Consumer<TrinketInventory> consumer) {
		TrinketsApi.getTrinketComponent(entity).map(TrinketComponent::getInventory).ifPresent(groups ->
		groups.values().forEach(slots -> slots.values().forEach(consumer)));
	}

	@Override
	public void forEachEquipped(LivingEntity entity, BiConsumer<SlotContext, ItemStack> consumer) {
		forEachInventory(entity, inventory -> {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				ItemStack stack = inventory.getItem(i);

				if (!stack.isEmpty()) {
					consumer.accept(TrinketAdapter.context(new SlotReference(inventory, i), entity), stack);
				}
			}
		});
	}

	@Override
	@Nullable
	public ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> filter) {
		return TrinketsApi.getTrinketComponent(entity)
				.flatMap(component -> component.getEquipped(stack -> !stack.isEmpty() && filter.test(stack))
						.stream().findFirst())
				.map(tuple -> tuple.getB()).orElse(null);
	}

	@Override
	public List<ItemStack> getSlotStacks(LivingEntity entity, String slotId) {
		List<ItemStack> stacks = new ArrayList<>();
		forEachInventory(entity, inventory -> {
			if (inventory.getSlotType().getName().equals(slotId)) {
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
			if (inventory.getSlotType().getName().equals(slotId) && inventory.getContainerSize() > 0) {
				open[0] = true;
			}
		});
		return !open[0];
	}

	@Override
	public boolean unlockSlot(LivingEntity entity, String slotId, UUID modifierId, String modifierName) {
		String group = UNLOCK_GROUPS.get(slotId);

		if (group == null)
			throw new IllegalArgumentException("Slot type '" + slotId + "' cannot be unlocked");

		TrinketInventory inventory = TrinketsApi.getTrinketComponent(entity)
				.map(component -> component.getInventory().getOrDefault(group, Map.of()).get(slotId)).orElse(null);

		if (inventory == null || inventory.getModifiers().containsKey(modifierId))
			return false;

		inventory.addPersistentModifier(new AttributeModifier(modifierId, modifierName, 1, AttributeModifier.Operation.ADDITION));
		return true;
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
		if (entity.level().isClientSide || stack.isEmpty())
			return false;

		List<TrinketInventory> inventories = new ArrayList<>();
		forEachInventory(entity, inventories::add);

		for (TrinketInventory inventory : inventories) {
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (!inventory.getItem(i).isEmpty()) {
					continue;
				}

				SlotReference ref = new SlotReference(inventory, i);

				if (TrinketsApi.evaluatePredicateSet(inventory.getSlotType().getValidatorPredicates(), stack, ref, entity)
						&& TrinketsApi.getTrinket(stack.getItem()).canEquip(stack, ref, entity)) {
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

		TrinketsApi.registerTrinket(item, new TrinketAdapter(accessory));
	}
}
