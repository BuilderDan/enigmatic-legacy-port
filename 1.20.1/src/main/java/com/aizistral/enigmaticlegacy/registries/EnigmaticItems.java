package com.aizistral.enigmaticlegacy.registries;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.items.IAdvancedPotionItem.PotionType;
import com.aizistral.enigmaticlegacy.api.generic.ModRegistry;
import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.items.AbyssalHeart;
import com.aizistral.enigmaticlegacy.items.AstralDust;
import com.aizistral.enigmaticlegacy.items.AstralPotato;
import com.aizistral.enigmaticlegacy.items.BlazingCore;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet;
import com.aizistral.enigmaticlegacy.items.EscapeScroll;
import com.aizistral.enigmaticlegacy.items.CosmicHeart;
import com.aizistral.enigmaticlegacy.items.EnchantmentTransposer;
import com.aizistral.enigmaticlegacy.items.EnigmaticItem;
import com.aizistral.enigmaticlegacy.items.EnderRing;
import com.aizistral.enigmaticlegacy.items.CursedStone;
import com.aizistral.enigmaticlegacy.items.DarkestScroll;
import com.aizistral.enigmaticlegacy.items.EarthHeart;
import com.aizistral.enigmaticlegacy.items.EvilEssence;
import com.aizistral.enigmaticlegacy.items.EvilIngot;
import com.aizistral.enigmaticlegacy.items.ExtradimensionalEye;
import com.aizistral.enigmaticlegacy.items.GoldenRing;
import com.aizistral.enigmaticlegacy.items.Infinimeal;
import com.aizistral.enigmaticlegacy.items.IronRing;
import com.aizistral.enigmaticlegacy.items.LoreFragment;
import com.aizistral.enigmaticlegacy.items.LoreInscriber;
import com.aizistral.enigmaticlegacy.items.MagnetRing;
import com.aizistral.enigmaticlegacy.items.MendingMixture;
import com.aizistral.enigmaticlegacy.items.MiningCharm;
import com.aizistral.enigmaticlegacy.items.MonsterCharm;
import com.aizistral.enigmaticlegacy.items.ForbiddenAxe;
import com.aizistral.enigmaticlegacy.items.RecallPotion;
import com.aizistral.enigmaticlegacy.items.TwistedPotion;
import com.aizistral.enigmaticlegacy.items.UnholyGrail;
import com.aizistral.enigmaticlegacy.items.ForbiddenFruit;
import com.aizistral.enigmaticlegacy.items.RedemptionPotion;
import com.aizistral.enigmaticlegacy.items.AstralFruit;
import com.aizistral.enigmaticlegacy.items.IchorBottle;
import com.aizistral.enigmaticlegacy.items.UltimatePotionBase;
import com.aizistral.enigmaticlegacy.items.UltimatePotionLingering;
import com.aizistral.enigmaticlegacy.items.UltimatePotionSplash;
import com.aizistral.enigmaticlegacy.items.SuperMagnetRing;
import com.aizistral.enigmaticlegacy.items.SoulDust;
import com.aizistral.enigmaticlegacy.items.ThiccScroll;
import com.aizistral.enigmaticlegacy.items.TwistedHeart;
import com.aizistral.enigmaticlegacy.items.XPScroll;
import com.aizistral.enigmaticlegacy.items.HeavenScroll;
import com.aizistral.enigmaticlegacy.items.GolemHeart;
import com.aizistral.enigmaticlegacy.items.EyeOfNebula;
import com.aizistral.enigmaticlegacy.items.VoidPearl;
import com.aizistral.enigmaticlegacy.items.OceanStone;
import com.aizistral.enigmaticlegacy.items.AngelBlessing;
import com.aizistral.enigmaticlegacy.items.FabulousScroll;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.aizistral.enigmaticlegacy.items.AvariceScroll;
import com.aizistral.enigmaticlegacy.items.TheCube;
import com.aizistral.enigmaticlegacy.items.CosmicScroll;
import com.aizistral.enigmaticlegacy.items.MegaSponge;
import com.aizistral.enigmaticlegacy.items.LootGenerator;
import com.aizistral.enigmaticlegacy.items.AstralBreaker;
import com.aizistral.enigmaticlegacy.items.OblivionStone;
import com.aizistral.enigmaticlegacy.items.CurseTransposer;
import com.aizistral.etherium.items.EnderRod;
import com.aizistral.etherium.items.EtheriumArmor;
import com.aizistral.etherium.items.EtheriumAxe;
import com.aizistral.etherium.items.EtheriumIngot;
import com.aizistral.etherium.items.EtheriumNugget;
import com.aizistral.etherium.items.EtheriumOre;
import com.aizistral.etherium.items.EtheriumPickaxe;
import com.aizistral.etherium.items.EtheriumScraps;
import com.aizistral.etherium.items.EtheriumScythe;
import com.aizistral.etherium.items.EtheriumShovel;
import com.aizistral.etherium.items.EtheriumSword;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/**
 * Port: holds only the items ported so far, in upstream's order. Holder fields are
 * non-final with no initializer, so {@link AbstractRegistry} can fill them.
 */
@ModRegistry
public class EnigmaticItems extends AbstractRegistry<Item> {
	public static final List<Item> SPELLSTONES = new ArrayList<>();
	private static final EnigmaticItems INSTANCE = new EnigmaticItems();

	@ObjectHolder(value = MODID + ":enigmatic_item", registryName = "item")
	public static EnigmaticItem ENIGMATIC_ITEM;

	@ConfigurableItem("Blazing Core")
	@ObjectHolder(value = MODID + ":blazing_core", registryName = "item")
	public static BlazingCore BLAZING_CORE;

	@ConfigurableItem("Scroll of Postmortal Recall")
	@ObjectHolder(value = MODID + ":escape_scroll", registryName = "item")
	public static EscapeScroll ESCAPE_SCROLL;

	@ObjectHolder(value = MODID + ":enigmatic_amulet", registryName = "item")
	public static EnigmaticAmulet ENIGMATIC_AMULET;

	@ConfigurableItem("Megasponge")
	@ObjectHolder(value = MODID + ":mega_sponge", registryName = "item")
	public static MegaSponge MEGA_SPONGE;

	@ObjectHolder(value = MODID + ":loot_generator", registryName = "item")
	public static LootGenerator LOOT_GENERATOR;

	@ConfigurableItem("Astral Breaker")
	@ObjectHolder(value = MODID + ":astral_breaker", registryName = "item")
	public static AstralBreaker ASTRAL_BREAKER;

	@ConfigurableItem("Keystone of The Oblivion")
	@ObjectHolder(value = MODID + ":void_stone", registryName = "item")
	public static OblivionStone VOID_STONE;

	@ConfigurableItem("Tome of Devoured Malignancy")
	@ObjectHolder(value = MODID + ":curse_transposer", registryName = "item")
	public static CurseTransposer CURSE_TRANSPOSER;

	@ConfigurableItem("The Architect's Inkwell")
	@ObjectHolder(value = MODID + ":lore_inscriber", registryName = "item")
	public static LoreInscriber LORE_INSCRIBER;

	@ConfigurableItem("Mending Mixture")
	@ObjectHolder(value = MODID + ":mending_mixture", registryName = "item")
	public static MendingMixture MENDING_MIXTURE;

	@ConfigurableItem("Blank Scroll")
	@ObjectHolder(value = MODID + ":thicc_scroll", registryName = "item")
	public static ThiccScroll THICC_SCROLL;

	@ConfigurableItem("Iron Ring")
	@ObjectHolder(value = MODID + ":iron_ring", registryName = "item")
	public static IronRing IRON_RING;

	@ConfigurableItem("Etherium Ore")
	@ObjectHolder(value = MODID + ":etherium_ore", registryName = "item")
	public static EtheriumOre ETHERIUM_ORE;

	@ConfigurableItem("Etherium Ingot")
	@ObjectHolder(value = MODID + ":etherium_ingot", registryName = "item")
	public static EtheriumIngot ETHERIUM_INGOT;

	@ConfigurableItem("Etherium Nugget")
	@ObjectHolder(value = MODID + ":etherium_nugget", registryName = "item")
	public static EtheriumNugget ETHERIUM_NUGGET;

	@ConfigurableItem("Etherium Scraps")
	@ObjectHolder(value = MODID + ":etherium_scraps", registryName = "item")
	public static EtheriumScraps ETHERIUM_SCRAPS;

	@ConfigurableItem("Etherium Armor")
	@ObjectHolder(value = MODID + ":etherium_helmet", registryName = "item")
	public static EtheriumArmor ETHERIUM_HELMET;

	@ConfigurableItem("Etherium Armor")
	@ObjectHolder(value = MODID + ":etherium_chestplate", registryName = "item")
	public static EtheriumArmor ETHERIUM_CHESTPLATE;

	@ConfigurableItem("Etherium Armor")
	@ObjectHolder(value = MODID + ":etherium_leggings", registryName = "item")
	public static EtheriumArmor ETHERIUM_LEGGINGS;

	@ConfigurableItem("Etherium Armor")
	@ObjectHolder(value = MODID + ":etherium_boots", registryName = "item")
	public static EtheriumArmor ETHERIUM_BOOTS;

	@ConfigurableItem("Etherium Pickaxe")
	@ObjectHolder(value = MODID + ":etherium_pickaxe", registryName = "item")
	public static EtheriumPickaxe ETHERIUM_PICKAXE;

	@ConfigurableItem("Etherium Waraxe")
	@ObjectHolder(value = MODID + ":etherium_axe", registryName = "item")
	public static EtheriumAxe ETHERIUM_AXE;

	@ConfigurableItem("Etherium Shovel")
	@ObjectHolder(value = MODID + ":etherium_shovel", registryName = "item")
	public static EtheriumShovel ETHERIUM_SHOVEL;

	@ConfigurableItem("Etherium Broadsword")
	@ObjectHolder(value = MODID + ":etherium_sword", registryName = "item")
	public static EtheriumSword ETHERIUM_SWORD;

	@ConfigurableItem("Etherium Scythe")
	@ObjectHolder(value = MODID + ":etherium_scythe", registryName = "item")
	public static EtheriumScythe ETHERIUM_SCYTHE;

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

	@ConfigurableItem("Scroll of Ageless Wisdom")
	@ObjectHolder(value = MODID + ":xp_scroll", registryName = "item")
	public static XPScroll XP_SCROLL;

	@ConfigurableItem("Magnet Ring")
	@ObjectHolder(value = MODID + ":magnet_ring", registryName = "item")
	public static MagnetRing MAGNET_RING;

	@ConfigurableItem("Extradimensional Eye")
	@ObjectHolder(value = MODID + ":extradimensional_eye", registryName = "item")
	public static ExtradimensionalEye EXTRADIMENSIONAL_EYE;

	@ConfigurableItem("Potion of Recall")
	@ObjectHolder(value = MODID + ":recall_potion", registryName = "item")
	public static RecallPotion RECALL_POTION;

	@ConfigurableItem("Potion of Twisted Mercy")
	@ObjectHolder(value = MODID + ":twisted_potion", registryName = "item")
	public static TwistedPotion TWISTED_POTION;

	@ConfigurableItem("Unholy Grail")
	@ObjectHolder(value = MODID + ":unholy_grail", registryName = "item")
	public static UnholyGrail UNHOLY_GRAIL;

	@ConfigurableItem("Forbidden Fruit")
	@ObjectHolder(value = MODID + ":forbidden_fruit", registryName = "item")
	public static ForbiddenFruit FORBIDDEN_FRUIT;

	@ConfigurableItem
	@ObjectHolder(value = MODID + ":redemption_potion", registryName = "item")
	public static RedemptionPotion REDEMPTION_POTION;

	@ConfigurableItem("Celestial Fruit")
	@ObjectHolder(value = MODID + ":astral_fruit", registryName = "item")
	public static AstralFruit ASTRAL_FRUIT;

	@ConfigurableItem("Bottle of Ichor")
	@ObjectHolder(value = MODID + ":ichor_bottle", registryName = "item")
	public static IchorBottle ICHOR_BOTTLE;

	@ConfigurableItem("Ultimate Potions")
	@ObjectHolder(value = MODID + ":ultimate_potion", registryName = "item")
	public static UltimatePotionBase ULTIMATE_POTION;

	@ConfigurableItem("Ultimate Potions")
	@ObjectHolder(value = MODID + ":ultimate_potion_splash", registryName = "item")
	public static UltimatePotionSplash ULTIMATE_POTION_SPLASH;

	@ConfigurableItem("Ultimate Potions")
	@ObjectHolder(value = MODID + ":ultimate_potion_lingering", registryName = "item")
	public static UltimatePotionLingering ULTIMATE_POTION_LINGERING;

	@ConfigurableItem("Common Potions")
	@ObjectHolder(value = MODID + ":common_potion", registryName = "item")
	public static UltimatePotionBase COMMON_POTION;

	@ConfigurableItem("Common Potions")
	@ObjectHolder(value = MODID + ":common_potion_splash", registryName = "item")
	public static UltimatePotionSplash COMMON_POTION_SPLASH;

	@ConfigurableItem("Common Potions")
	@ObjectHolder(value = MODID + ":common_potion_lingering", registryName = "item")
	public static UltimatePotionLingering COMMON_POTION_LINGERING;

	@ConfigurableItem("Axe of Executioner")
	@ObjectHolder(value = MODID + ":forbidden_axe", registryName = "item")
	public static ForbiddenAxe FORBIDDEN_AXE;

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

	@ConfigurableItem("Emblem of Monster Slayer")
	@ObjectHolder(value = MODID + ":monster_charm", registryName = "item")
	public static MonsterCharm MONSTER_CHARM;

	@ConfigurableItem("Ring of Ender")
	@ObjectHolder(value = MODID + ":ender_ring", registryName = "item")
	public static EnderRing ENDER_RING;

	@ConfigurableItem("Essence of Raging Life")
	@ObjectHolder(value = MODID + ":infinimeal", registryName = "item")
	public static Infinimeal INFINIMEAL;

	@ConfigurableItem("Astral Potato")
	@ObjectHolder(value = MODID + ":astral_potato", registryName = "item")
	public static AstralPotato ASTRAL_POTATO;

	@ConfigurableItem("Charm of Treasure Hunter")
	@ObjectHolder(value = MODID + ":mining_charm", registryName = "item")
	public static MiningCharm MINING_CHARM;

	@ConfigurableItem("Gift of the Heaven")
	@ObjectHolder(value = MODID + ":heaven_scroll", registryName = "item")
	public static HeavenScroll HEAVEN_SCROLL;

	@ConfigurableItem("Heart of the Golem")
	@ObjectHolder(value = MODID + ":golem_heart", registryName = "item")
	public static GolemHeart GOLEM_HEART;

	@ConfigurableItem("Eye of Nebula")
	@ObjectHolder(value = MODID + ":eye_of_nebula", registryName = "item")
	public static EyeOfNebula EYE_OF_NEBULA;

	@ConfigurableItem("Pearl of the Void")
	@ObjectHolder(value = MODID + ":void_pearl", registryName = "item")
	public static VoidPearl VOID_PEARL;

	@ConfigurableItem("Will of the Ocean")
	@ObjectHolder(value = MODID + ":ocean_stone", registryName = "item")
	public static OceanStone OCEAN_STONE;

	@ConfigurableItem("Angel's Blessing")
	@ObjectHolder(value = MODID + ":angel_blessing", registryName = "item")
	public static AngelBlessing ANGEL_BLESSING;

	@ConfigurableItem("Grace of the Creator")
	@ObjectHolder(value = MODID + ":fabulous_scroll", registryName = "item")
	public static FabulousScroll FABULOUS_SCROLL;

	@ConfigurableItem("Scroll of a Thousand Curses")
	@ObjectHolder(value = MODID + ":cursed_scroll", registryName = "item")
	public static CursedScroll CURSED_SCROLL;

	@ConfigurableItem("Pact of Infinite Avarice")
	@ObjectHolder(value = MODID + ":avarice_scroll", registryName = "item")
	public static AvariceScroll AVARICE_SCROLL;

	@ConfigurableItem("Non-Euclidean Cube")
	@ObjectHolder(value = MODID + ":the_cube", registryName = "item")
	public static TheCube THE_CUBE;

	@ConfigurableItem("Cosmic Scroll")
	@ObjectHolder(value = MODID + ":cosmic_scroll", registryName = "item")
	public static CosmicScroll COSMIC_SCROLL;

	@ConfigurableItem("Soul Dust")
	@ObjectHolder(value = MODID + ":soul_dust", registryName = "item")
	public static Item SOUL_DUST;

	private EnigmaticItems() {
		super(BuiltInRegistries.ITEM);

		this.register("enigmatic_item", EnigmaticItem::new);
		this.register("xp_scroll", XPScroll::new);
		this.register("magnet_ring", MagnetRing::new);
		this.register("extradimensional_eye", ExtradimensionalEye::new);
		this.register("recall_potion", RecallPotion::new);
		this.register("twisted_potion", TwistedPotion::new);
		this.register("unholy_grail", UnholyGrail::new);
		this.register("forbidden_fruit", ForbiddenFruit::new);
		this.register("redemption_potion", RedemptionPotion::new);
		this.register("astral_fruit", AstralFruit::new);
		this.register("ichor_bottle", IchorBottle::new);
		this.register("common_potion", () -> new UltimatePotionBase(Rarity.COMMON, PotionType.COMMON));
		this.register("common_potion_splash", () -> new UltimatePotionSplash(Rarity.COMMON, PotionType.COMMON));
		this.register("common_potion_lingering", () -> new UltimatePotionLingering(Rarity.COMMON, PotionType.COMMON));
		this.register("ultimate_potion", () -> new UltimatePotionBase(Rarity.RARE, PotionType.ULTIMATE));
		this.register("ultimate_potion_splash", () -> new UltimatePotionSplash(Rarity.RARE, PotionType.ULTIMATE));
		this.register("ultimate_potion_lingering", () -> new UltimatePotionLingering(Rarity.RARE, PotionType.ULTIMATE));
		this.register("forbidden_axe", ForbiddenAxe::new);
		this.register("super_magnet_ring", SuperMagnetRing::new);
		this.register("blazing_core", BlazingCore::new);
		this.register("escape_scroll", EscapeScroll::new);
		this.register("enigmatic_amulet", EnigmaticAmulet::new);
		this.register("ender_ring", EnderRing::new);
		this.register("mending_mixture", MendingMixture::new);
		this.register("thicc_scroll", ThiccScroll::new);
		this.register("iron_ring", IronRing::new);
		this.register("etherium_ore", EtheriumOre::new);
		this.register("etherium_ingot", EtheriumIngot::new);
		this.register("etherium_nugget", EtheriumNugget::new);
		this.register("etherium_scraps", EtheriumScraps::new);
		this.register("etherium_pickaxe", EtheriumPickaxe::new);
		this.register("etherium_axe", EtheriumAxe::new);
		this.register("etherium_shovel", EtheriumShovel::new);
		this.register("etherium_sword", EtheriumSword::new);
		this.register("etherium_scythe", EtheriumScythe::new);
		this.register("etherium_helmet", () -> new EtheriumArmor(ArmorItem.Type.HELMET));
		this.register("etherium_chestplate", () -> new EtheriumArmor(ArmorItem.Type.CHESTPLATE));
		this.register("etherium_leggings", () -> new EtheriumArmor(ArmorItem.Type.LEGGINGS));
		this.register("etherium_boots", () -> new EtheriumArmor(ArmorItem.Type.BOOTS));
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
		this.register("monster_charm", MonsterCharm::new);
		this.register("infinimeal", Infinimeal::new);
		this.register("astral_potato", AstralPotato::new);
		this.register("mining_charm", MiningCharm::new);
		this.register("heaven_scroll", HeavenScroll::new);
		this.register("golem_heart", GolemHeart::new);
		this.register("eye_of_nebula", EyeOfNebula::new);
		this.register("void_pearl", VoidPearl::new);
		this.register("ocean_stone", OceanStone::new);
		this.register("angel_blessing", AngelBlessing::new);
		this.register("fabulous_scroll", FabulousScroll::new);
		this.register("cursed_scroll", CursedScroll::new);
		this.register("avarice_scroll", AvariceScroll::new);
		this.register("the_cube", TheCube::new);
		this.register("cosmic_scroll", CosmicScroll::new);
		this.register("mega_sponge", MegaSponge::new);
		this.register("loot_generator", LootGenerator::new);
		this.register("astral_breaker", AstralBreaker::new);
		this.register("void_stone", OblivionStone::new);
		this.register("curse_transposer", CurseTransposer::new);
		this.register("lore_inscriber", LoreInscriber::new);
	}

	/** Port: forces class loading, which registers the items. */
	public static void init() {
		// NO-OP
	}
}
