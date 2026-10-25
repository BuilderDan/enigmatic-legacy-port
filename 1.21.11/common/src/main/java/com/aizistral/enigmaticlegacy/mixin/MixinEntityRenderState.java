package com.aizistral.enigmaticlegacy.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.aizistral.enigmaticlegacy.client.InsigniaClient;
import com.aizistral.enigmaticlegacy.client.renderers.EnigmaticLayerState;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Port: 26.3 name tags are drawn from the render state, so the Insignia's small true-name
 * line is carried there from extraction to submission. The same goes for the elytra and
 * Etherium shield layers ({@link EnigmaticLayerState}).
 */
@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements InsigniaClient.TrueNameState, EnigmaticLayerState {
	@Unique
	private @Nullable Component enigmaticlegacy$trueName;
	@Unique
	private ItemStack enigmaticlegacy$elytra = ItemStack.EMPTY;
	@Unique
	private boolean enigmaticlegacy$shield;

	@Override
	public @Nullable Component enigmaticlegacy$getTrueName() {
		return this.enigmaticlegacy$trueName;
	}

	@Override
	public void enigmaticlegacy$setTrueName(@Nullable Component name) {
		this.enigmaticlegacy$trueName = name;
	}

	@Override
	public ItemStack enigmaticlegacy$getElytra() {
		return this.enigmaticlegacy$elytra;
	}

	@Override
	public void enigmaticlegacy$setElytra(ItemStack stack) {
		this.enigmaticlegacy$elytra = stack;
	}

	@Override
	public boolean enigmaticlegacy$hasShield() {
		return this.enigmaticlegacy$shield;
	}

	@Override
	public void enigmaticlegacy$setShield(boolean shield) {
		this.enigmaticlegacy$shield = shield;
	}
}
