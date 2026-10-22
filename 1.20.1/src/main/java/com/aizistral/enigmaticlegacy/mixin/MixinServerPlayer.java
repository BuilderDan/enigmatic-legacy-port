package com.aizistral.enigmaticlegacy.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.objects.DropCapture;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Port-only. Stands in for the confirmed part of Forge's {@code LivingDeathEvent} for players
 * (die() runs only after totems and ALLOW_DEATH), and extends MixinEntity's drop capture to
 * player inventory drops, which ServerPlayer.drop adds to the world itself.
 */
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer {
	@Inject(method = "die", at = @At("HEAD"))
	private void enigmaticlegacy$onConfirmedDeath(DamageSource source, CallbackInfo info) {
		EnigmaticEventHandler.onConfirmedDeath((ServerPlayer) (Object) this, source);
	}

	@WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean enigmaticlegacy$captureDrop(Level level, Entity entity, Operation<Boolean> original) {
		List<ItemEntity> drops = ((DropCapture) this).enigmaticlegacy$getCapturedDrops();

		if (drops != null && entity instanceof ItemEntity drop) {
			drops.add(drop);
			return true;
		}

		return original.call(level, entity);
	}
}
