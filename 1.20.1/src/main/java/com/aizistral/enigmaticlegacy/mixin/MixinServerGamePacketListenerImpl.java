package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Port-only. Same as {@link MixinServerPlayerGameMode}, for placing and using blocks: vanilla
 * rejects use-on-block packets more than 6 blocks from the eyes.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
	@Shadow
	public ServerPlayer player;

	@ModifyExpressionValue(method = "handleUseItemOn", at = @At(value = "FIELD",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D"))
	private double enigmaticlegacy$widenUseDistance(double maxDistanceSqr) {
		return EnigmaticAttributes.widenDistanceSqr(maxDistanceSqr, this.player);
	}
}
