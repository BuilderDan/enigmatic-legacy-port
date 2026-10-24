package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.blocks.TileEndAnchor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Upstream registry. Port: BuiltInRegistries instead of ForgeRegistries; the fields are not
 * final, since the port's ObjectHolder handling assigns them after registration. The type is
 * built without a datafixer choice type (upstream used Util.fetchChoiceType, which only logs
 * an error for modded ids), as Fabric mods usually do.
 */
public class EnigmaticTiles extends AbstractRegistry<BlockEntityType<?>> {
	private static final EnigmaticTiles INSTANCE = new EnigmaticTiles();

	@ObjectHolder(value = MODID + ":tile_end_anchor", registryName = "block_entity_type")
	public static BlockEntityType<TileEndAnchor> END_ANCHOR;

	private EnigmaticTiles() {
		super(BuiltInRegistries.BLOCK_ENTITY_TYPE);
		this.register("tile_end_anchor", () -> BlockEntityType.Builder.<TileEndAnchor>of(
				TileEndAnchor::new, EnigmaticBlocks.END_ANCHOR).build(null));
	}

	/** Port: forces class loading, which registers the block entity types. Call after EnigmaticBlocks.init(). */
	public static void init() {
		// NO-OP
	}

}
