package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.objects.DropCapture;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Records drops for the LivingDropsEvent stand-in (see MixinLivingEntity).
 * Unlike Forge's capture, the drops still spawn normally; handlers can only add drops.
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements DropCapture {
	@Unique @Nullable
	private List<ItemEntity> enigmaticlegacy$capturedDrops;

	@Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At("RETURN"))
	private void enigmaticlegacy$onSpawnAtLocation(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> info) {
		if (this.enigmaticlegacy$capturedDrops != null && info.getReturnValue() != null) {
			this.enigmaticlegacy$capturedDrops.add(info.getReturnValue());
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
