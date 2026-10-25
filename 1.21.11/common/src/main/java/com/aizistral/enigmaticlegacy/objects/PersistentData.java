package com.aizistral.enigmaticlegacy.objects;

import net.minecraft.nbt.CompoundTag;

/**
 * Port-only. Implemented on Player by MixinPlayer. Stands in for the {@code PlayerPersisted}
 * compound inside Forge's {@code Entity.getPersistentData()}, which survives death and is
 * copied to the respawned player (see EnigmaticLegacyFabric's COPY_FROM hook).
 */
public interface PersistentData {
	CompoundTag enigmaticlegacy$getPersistedTag();

	void enigmaticlegacy$setPersistedTag(CompoundTag tag);
}
