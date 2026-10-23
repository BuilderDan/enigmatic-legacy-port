package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;

/**
 * Port: Forge's access transformer makes {@code tridentItem} public; Fabric reaches it through
 * this accessor instead.
 */
@Mixin(ThrownTrident.class)
public interface AccessorThrownTrident {
	@Accessor("tridentItem")
	ItemStack enigmaticlegacy$getTridentItem();
}
