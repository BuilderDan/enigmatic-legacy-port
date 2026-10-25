package com.aizistral.enigmaticlegacy.mixin;

import net.minecraft.advancements.AdvancementHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

/**
 * Port: Forge posts AdvancementEarnEvent where the rewards are granted, i.e. once, when the
 * advancement becomes done. The port runs upstream's onAdvancement handler at the same point.
 */
@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements {
	@Shadow
	private ServerPlayer player;

	@Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"))
	private void enigmaticlegacy$onEarned(AdvancementHolder advancement, String criterion, CallbackInfoReturnable<Boolean> info) {
		EnigmaticEventHandler.onAdvancement(this.player, advancement.id());
	}
}
