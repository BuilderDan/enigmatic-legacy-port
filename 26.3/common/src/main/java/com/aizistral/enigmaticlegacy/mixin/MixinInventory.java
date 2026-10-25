package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.objects.DropCapture;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Port-only. 26.3 spawns the death drops of the player inventory straight from Inventory.dropAll
 * instead of Player.drop, so they join the drop capture here (see MixinLivingEntity).
 */
@Mixin(Inventory.class)
public class MixinInventory {
	@Shadow @Final public Player player;

	@WrapOperation(method = "dropAll", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean enigmaticlegacy$captureDrop(Level level, Entity entity, Operation<Boolean> original) {
		List<ItemEntity> drops = ((DropCapture) this.player).enigmaticlegacy$getCapturedDrops();

		if (drops != null && entity instanceof ItemEntity drop) {
			drops.add(drop);
			return true;
		}

		return original.call(level, entity);
	}
}
