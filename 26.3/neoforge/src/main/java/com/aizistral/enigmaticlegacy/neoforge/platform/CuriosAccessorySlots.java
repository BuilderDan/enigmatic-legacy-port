package com.aizistral.enigmaticlegacy.neoforge.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryDrops;
import com.aizistral.enigmaticlegacy.platform.accessory.AccessorySlots;
import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Port-only. {@link AccessorySlots} on Curios, which upstream calls directly.
 */
public class CuriosAccessorySlots implements AccessorySlots {
	/** Slot types the port can unlock or add modifiers to. */
	private static final Set<String> UNLOCKABLE = Set.of("scroll", "spellstone", "ring", "charm");

	/** Applies {@link AccessoryDrops} overrides. Called from the main entry point. */
	public static void init() {
		NeoForge.EVENT_BUS.addListener(DropRulesEvent.class, event -> {
			LivingEntity entity = event.getEntity();

			event.getCurioHandler().getCurios().values().forEach(handler -> {
				IDynamicStackHandler stacks = handler.getStacks();

				for (int i = 0; i < stacks.getSlots(); i++) {
					ItemStack stack = stacks.getStackInSlot(i);

					if (!stack.isEmpty()) {
						DropRule override = AccessoryDrops.getOverride(entity, stack);

						if (override != null) {
							event.addOverride(s -> s == stack, CurioAdapter.toCurios(override));
						}
					}
				}
			});
		});
	}

	private static Optional<ICuriosItemHandler> inventory(LivingEntity entity) {
		return CuriosApi.getCuriosInventory(entity);
	}

	@Override
	public void forEachEquipped(LivingEntity entity, BiConsumer<SlotContext, ItemStack> consumer) {
		inventory(entity).ifPresent(inv -> inv.getCurios().forEach((id, handler) -> {
			IDynamicStackHandler stacks = handler.getStacks();

			for (int i = 0; i < stacks.getSlots(); i++) {
				ItemStack stack = stacks.getStackInSlot(i);

				if (!stack.isEmpty()) {
					consumer.accept(new SlotContext(id, entity, i, false, true), stack);
				}
			}
		}));
	}

	@Override
	@Nullable
	public ItemStack findFirst(LivingEntity entity, Predicate<ItemStack> filter) {
		return inventory(entity).flatMap(inv -> inv.findFirstCurio(stack -> !stack.isEmpty() && filter.test(stack)))
				.map(SlotResult::stack).orElse(null);
	}

	@Override
	public List<ItemStack> getSlotStacks(LivingEntity entity, String slotId) {
		List<ItemStack> list = new ArrayList<>();
		inventory(entity).flatMap(inv -> inv.getStacksHandler(slotId)).ifPresent(handler -> {
			IDynamicStackHandler stacks = handler.getStacks();

			for (int i = 0; i < stacks.getSlots(); i++) {
				list.add(stacks.getStackInSlot(i));
			}
		});
		return list;
	}

	@Override
	public boolean isSlotLocked(LivingEntity entity, String slotId) {
		return inventory(entity).flatMap(inv -> inv.getStacksHandler(slotId))
				.map(handler -> handler.getSlots() <= 0).orElse(true);
	}

	@Override
	public boolean unlockSlot(LivingEntity entity, String slotId, Identifier modifierId) {
		if (!UNLOCKABLE.contains(slotId))
			throw new IllegalArgumentException("Slot type '" + slotId + "' cannot be unlocked");

		Optional<ICuriosItemHandler> inv = inventory(entity);
		Optional<ICurioStacksHandler> handler = inv.flatMap(i -> i.getStacksHandler(slotId));

		if (handler.isEmpty() || handler.get().getModifiers().containsKey(modifierId))
			return false;

		inv.get().addPermanentSlotModifier(slotId, modifierId, 1, AttributeModifier.Operation.ADD_VALUE);
		return true;
	}

	@Override
	public void addSlotModifier(Multimap<Holder<Attribute>, AttributeModifier> map, String slotId, Identifier id, double amount,
			AttributeModifier.Operation operation) {
		if (!UNLOCKABLE.contains(slotId))
			throw new IllegalArgumentException("Unknown slot type '" + slotId + "'");

		map.put(SlotAttribute.getOrCreate(slotId), new AttributeModifier(id, amount, operation));
	}

	@Override
	public int destroy(LivingEntity entity, Item item) {
		int[] removed = new int[1];
		inventory(entity).ifPresent(inv -> inv.getCurios().values().forEach(handler -> {
			IDynamicStackHandler stacks = handler.getStacks();

			for (int i = 0; i < stacks.getSlots(); i++) {
				if (stacks.getStackInSlot(i).is(item)) {
					stacks.setStackInSlot(i, ItemStack.EMPTY);
					removed[0]++;
				}
			}
		}));
		return removed[0];
	}

	@Override
	public boolean tryForceEquip(LivingEntity entity, ItemStack stack) {
		if (entity.level().isClientSide() || stack.isEmpty())
			return false;

		Optional<ICuriosItemHandler> inv = inventory(entity);

		if (inv.isEmpty())
			return false;

		for (var entry : inv.get().getCurios().entrySet()) {
			IDynamicStackHandler stacks = entry.getValue().getStacks();

			for (int i = 0; i < stacks.getSlots(); i++) {
				if (!stacks.getStackInSlot(i).isEmpty())
					continue;

				var ctx = new top.theillusivec4.curios.api.SlotContext(entry.getKey(), entity, i, false, true);

				// isStackValid checks the slot tags and canEquip
				if (CuriosApi.isStackValid(ctx, stack) && stacks.isItemValid(i, stack)) {
					stacks.setStackInSlot(i, stack.copy());
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

		CuriosApi.registerCurio(item, new CurioAdapter(accessory));
	}
}
