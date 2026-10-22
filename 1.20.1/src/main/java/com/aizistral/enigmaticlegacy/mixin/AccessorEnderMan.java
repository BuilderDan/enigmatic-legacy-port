package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;

/**
 * Port: Forge's access transformer makes {@code teleportTowards} public; Fabric reaches it through
 * this invoker instead.
 */
@Mixin(EnderMan.class)
public interface AccessorEnderMan {
	@Invoker("teleportTowards")
	boolean enigmaticlegacy$teleportTowards(Entity target);
}
