package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.objects.SpecialLootModifier;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Port-only. Stands in for Forge's global loot modifier hook, which ran
 * {@link SpecialLootModifier} on every rolled table. Chest and suspicious-block loot both
 * pass through this private method.
 */
@Mixin(LootTable.class)
public abstract class MixinLootTable {

	@Inject(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
			at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$onGetRandomItems(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> info) {
		LootTable self = (LootTable) (Object) this;
		info.setReturnValue(SpecialLootModifier.doApply(SpecialLootModifier.getTableId(self), info.getReturnValue(), context));
	}

}
