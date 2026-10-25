package com.aizistral.enigmaticlegacy.objects;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.item.ItemEntity;

/**
 * Port-only. Implemented on every Entity by MixinEntity: while the list is set, item
 * entities spawned through spawnAtLocation are recorded in it. Stands in for Forge's
 * captureDrops, which LivingDropsEvent reads.
 */
public interface DropCapture {
	@Nullable
	List<ItemEntity> enigmaticlegacy$getCapturedDrops();

	void enigmaticlegacy$setCapturedDrops(@Nullable List<ItemEntity> drops);
}
