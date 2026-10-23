package com.aizistral.enigmaticlegacy.objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * Port-only. Stands in for Forge's Entity.getPersistentData() on any entity (MixinEntity saves it
 * with the entity under "enigmaticlegacy:data"). Player data that upstream keeps under
 * PERSISTED_NBT_TAG uses {@link PersistentData} instead.
 */
public interface EntityPersistentData {
	CompoundTag enigmaticlegacy$getEntityData();

	static CompoundTag get(Entity entity) {
		return ((EntityPersistentData) entity).enigmaticlegacy$getEntityData();
	}
}
