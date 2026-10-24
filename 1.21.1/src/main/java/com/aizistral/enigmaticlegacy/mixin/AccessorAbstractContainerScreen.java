package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Port: Forge adds public {@code getGuiLeft()} and {@code getGuiTop()}; Fabric reaches the
 * protected fields through this accessor instead.
 */
@Mixin(AbstractContainerScreen.class)
public interface AccessorAbstractContainerScreen {
	@Accessor("leftPos")
	int enigmaticlegacy$getLeftPos();

	@Accessor("topPos")
	int enigmaticlegacy$getTopPos();
}
