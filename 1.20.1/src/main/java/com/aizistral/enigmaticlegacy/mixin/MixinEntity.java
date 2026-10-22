package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.aizistral.enigmaticlegacy.objects.DropCapture;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Port-only. Forge's captureDrops: while a capture list is set (during dropAllDeathLoot, see
 * MixinLivingEntity), spawnAtLocation puts the drop in the list instead of the world.
 * MixinServerPlayer does the same for player inventory drops.
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements DropCapture {
	@Unique @Nullable
	private List<ItemEntity> enigmaticlegacy$capturedDrops;

	@WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean enigmaticlegacy$captureDrop(Level level, Entity entity, Operation<Boolean> original) {
		if (this.enigmaticlegacy$capturedDrops != null && entity instanceof ItemEntity drop) {
			this.enigmaticlegacy$capturedDrops.add(drop);
			return true;
		}

		return original.call(level, entity);
	}

	@Override
	public @Nullable List<ItemEntity> enigmaticlegacy$getCapturedDrops() {
		return this.enigmaticlegacy$capturedDrops;
	}

	@Override
	public void enigmaticlegacy$setCapturedDrops(@Nullable List<ItemEntity> drops) {
		this.enigmaticlegacy$capturedDrops = drops;
	}
}
