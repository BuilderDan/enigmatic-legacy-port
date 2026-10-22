package com.aizistral.enigmaticlegacy.registries;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.ModRegistry;
import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.items.AbyssalHeart;
import com.aizistral.enigmaticlegacy.items.AstralDust;
import com.aizistral.enigmaticlegacy.items.CosmicHeart;
import com.aizistral.enigmaticlegacy.items.EnchantmentTransposer;
import com.aizistral.enigmaticlegacy.items.CursedStone;
import com.aizistral.enigmaticlegacy.items.DarkestScroll;
import com.aizistral.enigmaticlegacy.items.EarthHeart;
import com.aizistral.enigmaticlegacy.items.EvilEssence;
import com.aizistral.enigmaticlegacy.items.EvilIngot;
import com.aizistral.enigmaticlegacy.items.ExtradimensionalEye;
import com.aizistral.enigmaticlegacy.items.GoldenRing;
import com.aizistral.enigmaticlegacy.items.IronRing;
import com.aizistral.enigmaticlegacy.items.LoreFragment;
import com.aizistral.enigmaticlegacy.items.MagnetRing;
import com.aizistral.enigmaticlegacy.items.MendingMixture;
import com.aizistral.enigmaticlegacy.items.SuperMagnetRing;
import com.aizistral.enigmaticlegacy.items.SoulDust;
import com.aizistral.enigmaticlegacy.items.ThiccScroll;
import com.aizistral.enigmaticlegacy.items.TwistedHeart;
import com.aizistral.etherium.items.EnderRod;
import com.aizistral.etherium.items.EtheriumIngot;
import com.aizistral.etherium.items.EtheriumNugget;
import com.aizistral.etherium.items.EtheriumScraps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

/**
 * Port: holds only the items ported so far, in upstream's order. Holder fields are
 * non-final with no initializer, so {@link AbstractRegistry} can fill them.
 */
@ModRegistry
public class EnigmaticItems extends AbstractRegistry<Item> {
	public static final List<Item> SPELLSTONES = new ArrayList<>();
	private static final EnigmaticItems INSTANCE = new EnigmaticItems();

	@ConfigurableItem("Mending Mixture")
	@ObjectHolder(value = MODID + ":mending_mixture", registryName = "item")
	public static MendingMixture MENDING_MIXTURE;

	@ConfigurableItem("Blank Scroll")
	@ObjectHolder(value = MODID + ":thicc_scroll", registryName = "item")
	public static ThiccScroll THICC_SCROLL;

	@ConfigurableItem("Iron Ring")
	@ObjectHolder(value = MODID + ":iron_ring", registryName = "item")
	public static IronRing IRON_RING;

	@ConfigurableItem("Etherium Ingot")
	@ObjectHolder(value = MODID + ":etherium_ingot", registryName = "item")
	public static EtheriumIngot ETHERIUM_INGOT;

	@ConfigurableItem("Etherium Nugget")
	@ObjectHolder(value = MODID + ":etherium_nugget", registryName = "item")
	public static EtheriumNugget ETHERIUM_NUGGET;

	@ConfigurableItem("Etherium Scraps")
	@ObjectHolder(value = MODID + ":etherium_scraps", registryName = "item")
	public static EtheriumScraps ETHERIUM_SCRAPS;

	@ConfigurableItem("Ender Rod")
	@ObjectHolder(value = MODID + ":ender_rod", registryName = "item")
	public static EnderRod ENDER_ROD;

	@ConfigurableItem("Astral Dust")
	@ObjectHolder(value = MODID + ":astral_dust", registryName = "item")
	public static AstralDust ASTRAL_DUST;

	@ConfigurableItem("Lore Fragment")
	@ObjectHolder(value = MODID + ":lore_fragment", registryName = "item")
	public static LoreFragment LORE_FRAGMENT;

	@ConfigurableItem("Heart of the Earth")
	@ObjectHolder(value = MODID + ":earth_heart", registryName = "item")
	public static EarthHeart EARTH_HEART;

	@ConfigurableItem("Twisted Heart")
	@ObjectHolder(value = MODID + ":twisted_heart", registryName = "item")
	public static TwistedHeart TWISTED_HEART;

	@ConfigurableItem("Exquisite Ring")
	@ObjectHolder(value = MODID + ":golden_ring", registryName = "item")
	public static GoldenRing GOLDEN_RING;

	@ConfigurableItem("Nefarious Essence")
	@ObjectHolder(value = MODID + ":evil_essence", registryName = "item")
	public static EvilEssence EVIL_ESSENCE;

	@ConfigurableItem("Nefarious Ingot")
	@ObjectHolder(value = MODID + ":evil_ingot", registryName = "item")
	public static EvilIngot EVIL_INGOT;

	@ConfigurableItem("Unholy Stone")
	@ObjectHolder(value = MODID + ":cursed_stone", registryName = "item")
	public static CursedStone CURSED_STONE;

	@ConfigurableItem("Darkest Scroll")
	@ObjectHolder(value = MODID + ":darkest_scroll", registryName = "item")
	public static DarkestScroll DARKEST_SCROLL;

	@ConfigurableItem("Magnet Ring")
	@ObjectHolder(value = MODID + ":magnet_ring", registryName = "item")
	public static MagnetRing MAGNET_RING;

	@ConfigurableItem("Extradimensional Eye")
	@ObjectHolder(value = MODID + ":extradimensional_eye", registryName = "item")
	public static ExtradimensionalEye EXTRADIMENSIONAL_EYE;

	@ConfigurableItem("Ring of Dislocation")
	@ObjectHolder(value = MODID + ":super_magnet_ring", registryName = "item")
	public static SuperMagnetRing SUPER_MAGNET_RING;

	@ConfigurableItem("Tome of Hungering Knowledge")
	@ObjectHolder(value = MODID + ":enchantment_transposer", registryName = "item")
	public static EnchantmentTransposer ENCHANTMENT_TRANSPOSER;

	@ConfigurableItem("Heart of the Cosmos")
	@ObjectHolder(value = MODID + ":cosmic_heart", registryName = "item")
	public static CosmicHeart COSMIC_HEART;

	@ConfigurableItem("Heart of the Abyss")
	@ObjectHolder(value = MODID + ":abyssal_heart", registryName = "item")
	public static AbyssalHeart ABYSSAL_HEART;

	@ConfigurableItem("Soul Dust")
	@ObjectHolder(value = MODID + ":soul_dust", registryName = "item")
	public static Item SOUL_DUST;

	private EnigmaticItems() {
		super(BuiltInRegistries.ITEM);

		this.register("magnet_ring", MagnetRing::new);
		this.register("extradimensional_eye", ExtradimensionalEye::new);
		this.register("super_magnet_ring", SuperMagnetRing::new);
		this.register("mending_mixture", MendingMixture::new);
		this.register("thicc_scroll", ThiccScroll::new);
		this.register("iron_ring", IronRing::new);
		this.register("etherium_ingot", EtheriumIngot::new);
		this.register("etherium_nugget", EtheriumNugget::new);
		this.register("etherium_scraps", EtheriumScraps::new);
		this.register("ender_rod", EnderRod::new);
		this.register("astral_dust", AstralDust::new);
		this.register("lore_fragment", LoreFragment::new);
		this.register("evil_essence", EvilEssence::new);
		this.register("evil_ingot", EvilIngot::new);
		this.register("earth_heart", EarthHeart::new);
		this.register("twisted_heart", TwistedHeart::new);
		this.register("golden_ring", GoldenRing::new);
		this.register("cursed_stone", CursedStone::new);
		this.register("darkest_scroll", DarkestScroll::new);
		this.register("enchantment_transposer", EnchantmentTransposer::new);
		this.register("cosmic_heart", CosmicHeart::new);
		this.register("abyssal_heart", AbyssalHeart::new);
		this.register("soul_dust", SoulDust::new);
	}

	/** Port: forces class loading, which registers the items. */
	public static void init() {
		// NO-OP
	}
}
