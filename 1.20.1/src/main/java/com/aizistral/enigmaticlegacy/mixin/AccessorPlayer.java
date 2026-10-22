package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;

/**
 * Port: Forge's access transformer makes {@code sleepCounter} public; Fabric reaches it through
 * this accessor instead.
 */
@Mixin(Player.class)
public interface AccessorPlayer {
	@Accessor("sleepCounter")
	void enigmaticlegacy$setSleepCounter(int counter);
}
