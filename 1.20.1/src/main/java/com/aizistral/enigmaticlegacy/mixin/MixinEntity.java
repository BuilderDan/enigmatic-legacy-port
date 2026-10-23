package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.aizistral.enigmaticlegacy.objects.DropCapture;
import com.aizistral.enigmaticlegacy.objects.EntityPersistentData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Port-only. Forge's captureDrops: while a capture list is set (during dropAllDeathLoot, see
 * MixinLivingEntity), spawnAtLocation puts the drop in the list instead of the world.
 * MixinServerPlayer does the same for player inventory drops. Also carries Forge's
 * getPersistentData() for any entity (see EntityPersistentData).
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements DropCapture, EntityPersistentData {
	@Unique
	private static final String ENTITY_DATA_KEY = "enigmaticlegacy:data";

	@Unique @Nullable
	private List<ItemEntity> enigmaticlegacy$capturedDrops;

	@Unique @Nullable
	private CompoundTag enigmaticlegacy$entityData;

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
	public CompoundTag enigmaticlegacy$getEntityData() {
		if (this.enigmaticlegacy$entityData == null) {
			this.enigmaticlegacy$entityData = new CompoundTag();
		}

		return this.enigmaticlegacy$entityData;
	}

	/** Forge writes getPersistentData() as "ForgeData" in saveWithoutId. */
	@Inject(method = "saveWithoutId", at = @At("HEAD"))
	private void enigmaticlegacy$saveEntityData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
		if (this.enigmaticlegacy$entityData != null && !this.enigmaticlegacy$entityData.isEmpty()) {
			tag.put(ENTITY_DATA_KEY, this.enigmaticlegacy$entityData.copy());
		}
	}

	@Inject(method = "load", at = @At("HEAD"))
	private void enigmaticlegacy$loadEntityData(CompoundTag tag, CallbackInfo info) {
		if (tag.contains(ENTITY_DATA_KEY, Tag.TAG_COMPOUND)) {
			this.enigmaticlegacy$entityData = tag.getCompound(ENTITY_DATA_KEY).copy();
		}
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
