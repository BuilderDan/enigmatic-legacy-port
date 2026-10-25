package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.objects.DropCapture;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Port-only. 26.3 drops worn armor and the offhand item from EntityEquipment.dropAll, so they
 * join the drop capture here like the rest of the inventory (see MixinInventory).
 */
@Mixin(EntityEquipment.class)
public class MixinEntityEquipment {
	@WrapOperation(method = "dropAll", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean enigmaticlegacy$captureDrop(Level level, Entity entity, Operation<Boolean> original, LivingEntity dropper) {
		List<ItemEntity> drops = ((DropCapture) dropper).enigmaticlegacy$getCapturedDrops();

		if (drops != null && entity instanceof ItemEntity drop) {
			drops.add(drop);
			return true;
		}

		return original.call(level, entity);
	}
}
