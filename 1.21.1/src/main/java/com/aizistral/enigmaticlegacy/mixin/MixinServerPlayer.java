package com.aizistral.enigmaticlegacy.mixin;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import com.aizistral.enigmaticlegacy.items.EndAnchor;
import com.aizistral.enigmaticlegacy.objects.AnchorSearchResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
	/**
	 * Upstream's MixinPlayer: respawn at an End Anchor. Port: 1.21 moved the static respawn search
	 * from Player to ServerPlayer and returns a position with a yaw that faces the block.
	 */
	@Inject(method = "findRespawnAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
	private static void enigmaticlegacy$onFindRespawnPosition(ServerLevel level, BlockPos pos, float angle, boolean forced,
			boolean keep, CallbackInfoReturnable<Optional<ServerPlayer.RespawnPosAngle>> info) {
		AnchorSearchResult result = EndAnchor.findAndUseEndAnchor(level, pos, angle, forced, keep);

		if (result.found()) {
			info.setReturnValue(result.location().map(location -> ServerPlayer.RespawnPosAngle.of(location, pos)));
		}
	}

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
