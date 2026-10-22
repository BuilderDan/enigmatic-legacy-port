package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;

/**
 * Port-only. Vanilla's server rejects block breaking more than 6 blocks from the eyes; Forge
 * checks the player's BLOCK_REACH instead. This widens the check by the port's block reach.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {
	@Shadow @Final
	protected ServerPlayer player;

	@ModifyExpressionValue(method = "handleBlockBreakAction", at = @At(value = "FIELD",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D"))
	private double enigmaticlegacy$widenBreakDistance(double maxDistanceSqr) {
		return EnigmaticAttributes.widenDistanceSqr(maxDistanceSqr, this.player);
	}
}
