package com.aizistral.enigmaticlegacy.triggers;

import java.util.List;
import java.util.Optional;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CursedInventoryChangedTrigger extends SimpleCriterionTrigger<CursedInventoryChangedTrigger.TriggerInstance> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "cursed_inventory_changed");
	public static final CursedInventoryChangedTrigger INSTANCE = new CursedInventoryChangedTrigger();

	// Port: 1.21 triggers are codec-based; the slots object reuses vanilla's InventoryChangeTrigger codec.
	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack) {
		int i = 0;
		int j = 0;
		int k = 0;

		for(int l = 0; l < pInventory.getContainerSize(); ++l) {
			ItemStack itemstack = pInventory.getItem(l);
			if (itemstack.isEmpty()) {
				++j;
			} else {
				++k;
				if (itemstack.getCount() >= itemstack.getMaxStackSize()) {
					++i;
				}
			}
		}

		this.trigger(pPlayer, pInventory, pStack, i, j, k);
	}

	private void trigger(ServerPlayer pPlayer, Inventory pInventory, ItemStack pStack, int pFull, int pEmpty, int pOccupied) {
		this.trigger(pPlayer, (p_43166_) -> {
			return p_43166_.matches(pInventory, pStack, pFull, pEmpty, pOccupied);
		});
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, InventoryChangeTrigger.TriggerInstance.Slots slots,
			List<ItemPredicate> items) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				InventoryChangeTrigger.TriggerInstance.Slots.CODEC.optionalFieldOf("slots", InventoryChangeTrigger.TriggerInstance.Slots.ANY).forGetter(TriggerInstance::slots),
				ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(TriggerInstance::items)
				).apply(builder, TriggerInstance::new));

		public boolean matches(Inventory pInventory, ItemStack pStack, int pFull, int pEmpty, int pOccupied) {
			if (!this.slots.matches(pFull, pEmpty, pOccupied))
				return false;
			else {
				int i = this.items.size();
				if (i == 0)
					return true;
				else if (i != 1) {
					List<ItemPredicate> list = new ObjectArrayList<>(this.items);
					int j = pInventory.getContainerSize();

					for(int k = 0; k < j; ++k) {
						if (list.isEmpty())
							return true;

						ItemStack itemstack = pInventory.getItem(k);
						if (!itemstack.isEmpty()) {
							list.removeIf((predicate) -> {
								if (itemstack.getItem() instanceof IEldritch)
									return predicate.test(itemstack) && SuperpositionHandler.isTheWorthyOne(pInventory.player);
								else
									return predicate.test(itemstack);
							});
						}
					}

					return list.isEmpty();
				} else {
					if (!pStack.isEmpty()) {
						if (pStack.getItem() instanceof IEldritch)
							return this.items.get(0).test(pStack) && SuperpositionHandler.isTheWorthyOne(pInventory.player);
						else
							return this.items.get(0).test(pStack);
					} else
						return false;
				}
			}
		}
	}
}