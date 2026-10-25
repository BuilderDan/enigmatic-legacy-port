package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.NaturalSpawner;

/**
 * Port-only stand-in for upstream's MobSpawnEvent.FinalizeSpawn handler (Burden of Desolation):
 * natural spawns of piglins, zombified piglins, iron golems and endermen are refused inside a
 * desolation box.
 */
@Mixin(NaturalSpawner.class)
public class MixinNaturalSpawner {

	@Inject(method = "isValidPositionForMob", at = @At("HEAD"), cancellable = true)
	private static void enigmaticlegacy$desolation(ServerLevel level, Mob entity, double distance, CallbackInfoReturnable<Boolean> info) {
		if (entity instanceof Piglin || entity instanceof ZombifiedPiglin || entity instanceof IronGolem
				|| entity instanceof EnderMan) {
			if (EnigmaticEventHandler.DESOLATION_BOXES.values().stream().anyMatch(entity.getBoundingBox()::intersects)) {
				info.setReturnValue(false);
			}
		}
	}

}
