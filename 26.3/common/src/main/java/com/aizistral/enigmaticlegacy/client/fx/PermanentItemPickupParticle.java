package com.aizistral.enigmaticlegacy.client.fx;

import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.world.entity.Entity;

/**
 * Port: @OnlyIn removed (client-only class). 26.3 particles render through particle groups, so this
 * now extends vanilla's ItemPickupParticle (which upstream's class copied) and only keeps the safe
 * copy of the permanent item entity.
 */
public class PermanentItemPickupParticle extends ItemPickupParticle {

	public PermanentItemPickupParticle(ClientLevel world, Entity item, Entity target) {
		super(world, Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(getSafeCopy(item), 1.0F),
				target, item.getDeltaMovement());
	}

	private static Entity getSafeCopy(Entity entity) {
		return !(entity instanceof PermanentItemEntity) ? entity : ((PermanentItemEntity)entity).copy();
	}

}
