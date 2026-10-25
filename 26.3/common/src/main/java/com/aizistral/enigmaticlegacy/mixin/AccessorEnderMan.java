package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;

/**
 * Port: Forge's access transformer makes {@code teleportTowards} public; Fabric reaches it through
 * this invoker instead.
 */
@Mixin(Enderman.class)
public interface AccessorEnderMan {
	@Invoker("teleportTowards")
	boolean enigmaticlegacy$teleportTowards(Entity target);
}
