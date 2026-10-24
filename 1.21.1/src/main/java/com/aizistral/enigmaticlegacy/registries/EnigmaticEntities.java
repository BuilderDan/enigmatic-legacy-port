package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.entities.EnigmaticPotionEntity;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.entities.UltimateWitherSkullEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Port: holds only the entities ported so far. The other upstream entities come
 * with the items that use them.
 */
public class EnigmaticEntities extends AbstractRegistry<EntityType<?>> {
	private static final EnigmaticEntities INSTANCE = new EnigmaticEntities();

	@ObjectHolder(value = MODID + ":enigmatic_potion_entity", registryName = "entity_type")
	public static EntityType<EnigmaticPotionEntity> ENIGMATIC_POTION;

	@ObjectHolder(value = MODID + ":ultimate_wither_skull_entity", registryName = "entity_type")
	public static EntityType<UltimateWitherSkullEntity> ULTIMATE_WITHER_SKULL;

	@ObjectHolder(value = MODID + ":permanent_item_entity", registryName = "entity_type")
	public static EntityType<PermanentItemEntity> PERMANENT_ITEM_ENTITY;

	private EnigmaticEntities() {
		super(BuiltInRegistries.ENTITY_TYPE);

		// Port: vanilla builder; the update interval matches upstream's setUpdateInterval(10)
		this.register("enigmatic_potion_entity", () -> EntityType.Builder.<EnigmaticPotionEntity>of(EnigmaticPotionEntity::new, MobCategory.MISC)
				.sized(0.25F, 0.25F).clientTrackingRange(64).updateInterval(10).build(MODID + ":enigmatic_potion_entity"));

		this.register("ultimate_wither_skull_entity", () -> EntityType.Builder.<UltimateWitherSkullEntity>of(UltimateWitherSkullEntity::new, MobCategory.MISC)
				.sized(0.25F, 0.25F).clientTrackingRange(64).build(MODID + ":ultimate_wither_skull_entity"));
		this.register("permanent_item_entity", () -> EntityType.Builder.<PermanentItemEntity>of(PermanentItemEntity::new, MobCategory.MISC)
				.sized(0.25F, 0.25F).clientTrackingRange(64).build(MODID + ":permanent_item_entity"));
	}

	/** Port: forces class loading, which registers the entities. */
	public static void init() {
		// NO-OP
	}

}
