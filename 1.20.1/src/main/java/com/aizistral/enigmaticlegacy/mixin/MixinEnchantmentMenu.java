package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Port-only slotsChanged injects stand in for Forge's EnchantmentLevelSetEvent: they work out the
 * Ring of the Seven Curses bonus for this table once per slotsChanged, and MixinEnchantmentHelper
 * adds it to each getEnchantmentCost result made meanwhile. The client's access is NULL, so it gets
 * no bonus. The clickMenuButton and getGoldCount injects port upstream's MixinEnchantmentContainer
 * (Enchanter's Pearl).
 */
@Mixin(EnchantmentMenu.class)
public abstract class MixinEnchantmentMenu {
	@Shadow @Final private ContainerLevelAccess access;
	@Shadow @Final private Container enchantSlots;
	@Shadow @Final private DataSlot enchantmentSeed;
	@Shadow @Final public int[] costs;

	@Shadow
	private List<EnchantmentInstance> getEnchantmentList(ItemStack stack, int slot, int cost) {
		throw new IllegalStateException("Mixin shadow");
	}

	@Shadow
	public abstract void slotsChanged(Container container);

	@Inject(method = "slotsChanged", at = @At("HEAD"))
	private void enigmaticlegacy$startEnchantingBonus(Container container, CallbackInfo info) {
		EnigmaticEventHandler.ENCHANTING_BONUS.set(this.access.evaluate(EnigmaticEventHandler::getEnchantingBonus, 0));
	}

	@Inject(method = "slotsChanged", at = @At("RETURN"))
	private void enigmaticlegacy$endEnchantingBonus(Container container, CallbackInfo info) {
		EnigmaticEventHandler.ENCHANTING_BONUS.remove();
	}

	// Port: upstream injects at @At("INVOKE") with no target; HEAD is what that resolved to (see CHANGES.md).
	@Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onEnchantedItem(Player player, int clickedID, CallbackInfoReturnable<Boolean> info) {
		if (clickedID < 0 || clickedID >= this.costs.length || !EnigmaticItems.ENCHANTER_PEARL.isPresent(player))
			return;

		ItemStack inputItem = this.enchantSlots.getItem(0);
		int levelsRequired = clickedID + 1;

		if (this.costs[clickedID] <= 0 || inputItem.isEmpty() || (player.experienceLevel < levelsRequired || player.experienceLevel < this.costs[clickedID]) && !player.getAbilities().instabuild) {
			info.setReturnValue(false);
			return;
		}

		this.access.execute((world, blockPos) -> {
			ItemStack enchantedItem = inputItem;
			List<EnchantmentInstance> rolledEnchantments = this.getEnchantmentList(inputItem, clickedID, this.costs[clickedID]);
			if (!rolledEnchantments.isEmpty()) {
				ItemStack doubleRoll = EnchantmentHelper.enchantItem(player.getRandom(), enchantedItem.copy(), Math.min(this.costs[clickedID] + 7, 40), true);

				player.onEnchantmentPerformed(inputItem, levelsRequired);
				boolean isBookStack = inputItem.getItem() == Items.BOOK;
				if (isBookStack) {
					enchantedItem = new ItemStack(Items.ENCHANTED_BOOK);
					CompoundTag compoundnbt = inputItem.getTag();
					if (compoundnbt != null) {
						enchantedItem.setTag(compoundnbt.copy());
					}

					this.enchantSlots.setItem(0, enchantedItem);
				}

				for (EnchantmentInstance enchantmentdata : rolledEnchantments) {
					if (isBookStack) {
						EnchantedBookItem.addEnchantment(enchantedItem, enchantmentdata);
					} else {
						enchantedItem.enchant(enchantmentdata.enchantment, enchantmentdata.level);
					}
				}

				enchantedItem = SuperpositionHandler.mergeEnchantments(enchantedItem, doubleRoll, false, false);
				enchantedItem = SuperpositionHandler.maybeApplyEternalBinding(enchantedItem);

				this.enchantSlots.setItem(0, enchantedItem);

				player.awardStat(Stats.ENCHANT_ITEM);
				if (player instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, enchantedItem, levelsRequired);
				}

				this.enchantSlots.setChanged();
				this.enchantmentSeed.set(player.getEnchantmentSeed());
				this.slotsChanged(this.enchantSlots);
				world.playSound(null, blockPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
			}
		});

		info.setReturnValue(true);
	}

	// Port: upstream applies this on the client only; here it runs on both sides (see CHANGES.md).
	@Inject(method = "getGoldCount", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onGetLapisAmount(CallbackInfoReturnable<Integer> info) {
		Player containerUser = null;

		for (Slot slot : ((AbstractContainerMenu) (Object) this).slots) {
			if (slot.container instanceof Inventory playerInv) {
				containerUser = playerInv.player;
				break;
			}
		}

		if (containerUser != null && EnigmaticItems.ENCHANTER_PEARL.isPresent(containerUser)) {
			info.setReturnValue(64);
		}
	}
}
