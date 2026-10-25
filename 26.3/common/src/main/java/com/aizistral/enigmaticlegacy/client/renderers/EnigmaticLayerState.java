package com.aizistral.enigmaticlegacy.client.renderers;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.etherium.items.EtheriumArmor;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. 26.3 render layers only see the render state, not the entity, so the data that
 * {@link EnigmaticElytraLayer} and {@link com.aizistral.etherium.client.ShieldAuraLayer} used to
 * read from the entity is copied onto the state during extraction (mixed into
 * {@link EntityRenderState}).
 */
public interface EnigmaticLayerState {

	ItemStack enigmaticlegacy$getElytra();

	void enigmaticlegacy$setElytra(ItemStack stack);

	boolean enigmaticlegacy$hasShield();

	void enigmaticlegacy$setShield(boolean shield);

	static EnigmaticLayerState of(EntityRenderState state) {
		return (EnigmaticLayerState) state;
	}

	static void extract(LivingEntity entity, EntityRenderState state) {
		EnigmaticLayerState extra = of(state);
		extra.enigmaticlegacy$setElytra(SuperpositionHandler.hasEnigmaticElytra(entity) ? SuperpositionHandler.getEnigmaticElytra(entity).copy() : ItemStack.EMPTY);
		extra.enigmaticlegacy$setShield(entity instanceof Player player && EtheriumArmor.hasShield(player));
	}

}
