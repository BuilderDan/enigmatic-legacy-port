package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.advancements.triggers.SummonedEntityTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Port: upstream posts its SummonedEntityEvent here; the port runs the handler directly.
 */
@Mixin(SummonedEntityTrigger.class)
public class MixinSummonedEntityTrigger {
	@Inject(method = "trigger", at = @At("HEAD"))
	private void enigmaticlegacy$onTrigger(ServerPlayer player, Entity entity, CallbackInfo info) {
		EnigmaticEventHandler.onEntitySummon(player, entity);
	}
}
