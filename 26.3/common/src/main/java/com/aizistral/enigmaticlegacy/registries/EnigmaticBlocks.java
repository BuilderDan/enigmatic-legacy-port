package com.aizistral.enigmaticlegacy.registries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.blocks.BlockAstralDust;
import com.aizistral.enigmaticlegacy.blocks.BlockBigLamp;
import com.aizistral.enigmaticlegacy.blocks.BlockCosmicCake;
import com.aizistral.enigmaticlegacy.blocks.BlockEndAnchor;
import com.aizistral.enigmaticlegacy.blocks.BlockMassiveLamp;
import com.aizistral.enigmaticlegacy.blocks.BlockStarFabric;
import com.aizistral.enigmaticlegacy.items.CosmicCake;
import com.aizistral.enigmaticlegacy.items.EndAnchor;
import com.aizistral.enigmaticlegacy.items.generic.GenericBlockItem;
import com.aizistral.etherium.blocks.BlockEtherium;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

/**
 * Upstream registry. Port: BuiltInRegistries instead of ForgeRegistries; the fields are not
 * final, since the port's ObjectHolder handling assigns them after registration. Upstream
 * registers the block items from EnigmaticItems' RegisterEvent hook; the port does it from the
 * end of the EnigmaticItems constructor, through {@link #registerBlockItems()}.
 */
public class EnigmaticBlocks extends AbstractRegistry<Block> {
	// Port: LinkedHashMap, so the block items keep a fixed order in the item registry.
	private static final Map<Identifier, BlockItemSupplier> BLOCK_ITEM_MAP = new LinkedHashMap<>();
	private static final EnigmaticBlocks INSTANCE = new EnigmaticBlocks();

	@ObjectHolder(value = MODID + ":massive_lamp", registryName = "block")
	public static BlockMassiveLamp MASSIVE_LAMP;

	@ObjectHolder(value = MODID + ":massive_shroomlamp", registryName = "block")
	public static BlockMassiveLamp MASSIVE_SHROOMLAMP;

	@ObjectHolder(value = MODID + ":massive_redstonelamp", registryName = "block")
	public static BlockMassiveLamp MASSIVE_REDSTONELAMP;

	@ObjectHolder(value = MODID + ":big_lamp", registryName = "block")
	public static BlockBigLamp BIG_LAMP;

	@ObjectHolder(value = MODID + ":big_shroomlamp", registryName = "block")
	public static BlockBigLamp BIG_SHROOMLAMP;

	@ObjectHolder(value = MODID + ":big_redstonelamp", registryName = "block")
	public static BlockBigLamp BIG_REDSTONELAMP;

	@ObjectHolder(value = MODID + ":etherium_block", registryName = "block")
	public static BlockEtherium ETHERIUM_BLOCK;

	@ObjectHolder(value = MODID + ":cosmic_cake", registryName = "block")
	public static BlockCosmicCake COSMIC_CAKE;

	@ObjectHolder(value = MODID + ":astral_block", registryName = "block")
	public static BlockAstralDust ASTRAL_BLOCK;

	@ObjectHolder(value = MODID + ":end_anchor", registryName = "block")
	public static BlockEndAnchor END_ANCHOR;

	@ObjectHolder(value = MODID + ":star_fabric", registryName = "block")
	public static BlockStarFabric STAR_FABRIC;

	private EnigmaticBlocks() {
		super(BuiltInRegistries.BLOCK);
		this.register("massive_lamp", BlockMassiveLamp::new, GenericBlockItem::new);
		this.register("massive_shroomlamp", BlockMassiveLamp::new, GenericBlockItem::new);
		this.register("massive_redstonelamp", BlockMassiveLamp::new, GenericBlockItem::new);
		this.register("big_lamp", BlockBigLamp::new, GenericBlockItem::new);
		this.register("big_shroomlamp", BlockBigLamp::new, GenericBlockItem::new);
		this.register("big_redstonelamp", BlockBigLamp::new, GenericBlockItem::new);
		this.register("cosmic_cake", BlockCosmicCake::new, block -> new CosmicCake());
		this.register("end_anchor", BlockEndAnchor::new, block -> new EndAnchor());

		this.register("etherium_block", BlockEtherium::new, block ->
			new GenericBlockItem(block, GenericBlockItem.getDefaultProperties().rarity(Rarity.RARE)));

		this.register("astral_block", BlockAstralDust::new, block ->
			new GenericBlockItem(block, GenericBlockItem.getDefaultProperties().rarity(Rarity.EPIC)));

		this.register("star_fabric", BlockStarFabric::new, block ->
				new GenericBlockItem(block, GenericBlockItem.getDefaultProperties().rarity(Rarity.EPIC)));
	}

	protected void register(String name, Supplier<Block> block, BlockItemSupplier item) {
		super.register(name, block);
		BLOCK_ITEM_MAP.put(Identifier.fromNamespaceAndPath(MODID, name), item);
	}

	/** Port: upstream's EnigmaticItems.onRegister. Called at the end of the EnigmaticItems constructor. */
	static void registerBlockItems() {
		BLOCK_ITEM_MAP.forEach((id, item) -> Registry.register(BuiltInRegistries.ITEM, id,
				PendingRegistryId.construct(BuiltInRegistries.ITEM, id, true, () -> item.apply(BuiltInRegistries.BLOCK.getValue(id)))));
	}

	/** Port: forces class loading, which registers the blocks. Call before EnigmaticItems.init(). */
	public static void init() {
		// NO-OP
	}

	@FunctionalInterface
	protected static interface BlockItemSupplier extends Function<Block, BlockItem> {
		@Override
		public BlockItem apply(Block block);
	}

}
