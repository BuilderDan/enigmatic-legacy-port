package com.aizistral.enigmaticlegacy.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.RevelationTome;
import com.aizistral.enigmaticlegacy.triggers.RevelationTomeBurntTrigger;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Port-only. Stands in for upstream's FurnaceFuelBurnTimeEvent handler: when a Revelation
 * Tome is used as fuel, its last holder gets the burnt-tome advancement trigger.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity {
	@Inject(method = "getBurnDuration", at = @At("HEAD"))
	private void enigmaticlegacy$onTomeBurnt(ItemStack fuel, CallbackInfoReturnable<Integer> info) {
		if (fuel.getItem() instanceof RevelationTome && ItemNBTHelper.verifyExistance(fuel, RevelationTome.lastHolderTag)) {
			Level level = ((BlockEntity) (Object) this).getLevel();

			if (level instanceof ServerLevel serverLevel) {
				UUID holder = ItemNBTHelper.getUUID(fuel, RevelationTome.lastHolderTag, null);
				ServerPlayer player = holder != null ? serverLevel.getServer().getPlayerList().getPlayer(holder) : null;

				if (player != null) {
					RevelationTomeBurntTrigger.INSTANCE.trigger(player);
				}
			}
		}
	}
}
