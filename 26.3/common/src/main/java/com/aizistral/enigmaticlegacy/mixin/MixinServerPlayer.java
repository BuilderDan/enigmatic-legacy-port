package com.aizistral.enigmaticlegacy.mixin;

import java.util.Optional;
import net.minecraft.world.level.portal.TeleportTransition;
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
	 * from Player to ServerPlayer and returns a position with a yaw that faces the block; 26.3 passes
	 * the whole RespawnConfig instead of position, angle and forced flag.
	 */
	@Inject(method = "findRespawnAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
	private static void enigmaticlegacy$onFindRespawnPosition(ServerLevel level, ServerPlayer.RespawnConfig config,
			boolean consume, CallbackInfoReturnable<Optional<ServerPlayer.RespawnPosAngle>> info) {
		BlockPos pos = config.respawnData().pos();
		AnchorSearchResult result = EndAnchor.findAndUseEndAnchor(level, pos, config.respawnData().yaw(), config.forced(), !consume);

		if (result.found()) {
			info.setReturnValue(result.location().map(location -> ServerPlayer.RespawnPosAngle.of(location, pos, 0.0F)));
		}
	}

	/**
	 * Upstream's MixinPlayerList: a player who keeps their data (leaving the End through the exit
	 * portal) with an End Anchor spawn in the End goes to the world spawn but keeps the anchor spawn.
	 */
	@Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onFindRespawnTransition(boolean consume, TeleportTransition.PostTeleportTransition post,
			CallbackInfoReturnable<TeleportTransition> info) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		ServerPlayer.RespawnConfig config = self.getRespawnConfig();

		if (!consume && config != null && config.respawnData().dimension() == Level.END) {
			ServerLevel level = self.level().getServer().getLevel(Level.END);

			if (level != null && EndAnchor.findEndAnchor(level, config.respawnData().pos(), config.respawnData().yaw(),
					config.forced(), true).location().isPresent()) {
				info.setReturnValue(TeleportTransition.createDefault(self, post));
			}
		}
	}

	@Inject(method = "die", at = @At("HEAD"))
	private void enigmaticlegacy$onConfirmedDeath(DamageSource source, CallbackInfo info) {
		EnigmaticEventHandler.onConfirmedDeath((ServerPlayer) (Object) this, source);
	}
}
