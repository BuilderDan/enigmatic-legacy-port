package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;
import com.aizistral.enigmaticlegacy.brewing.SpecialBrewingRecipe;
import com.aizistral.enigmaticlegacy.brewing.ValidationBrewingRecipe;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.crafting.BindToPlayerRecipe;
import com.aizistral.enigmaticlegacy.crafting.BlessedShapedRecipe;
import com.aizistral.enigmaticlegacy.crafting.CursedShapedRecipe;
import com.aizistral.enigmaticlegacy.crafting.EnchantmentTransposingRecipe;
import com.aizistral.enigmaticlegacy.crafting.HiddenRecipe;
import com.aizistral.enigmaticlegacy.crafting.MendingMixtureRepairRecipe;
import com.aizistral.enigmaticlegacy.crafting.OblivionStoneCombineRecipe;
import com.aizistral.enigmaticlegacy.crafting.ShapelessNoReturnRecipe;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import com.aizistral.enigmaticlegacy.brewing.BrewingIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

public class EnigmaticRecipes extends AbstractRegistry<RecipeSerializer<?>> {
	private static final EnigmaticRecipes INSTANCE = new EnigmaticRecipes();

	@ObjectHolder(value = MODID + ":mending_mixture_repair", registryName = "recipe_serializer")
	public static RecipeSerializer<MendingMixtureRepairRecipe> MENDING_MIXTURE_REPAIR;

	@ObjectHolder(value = MODID + ":enchantment_transposing", registryName = "recipe_serializer")
	public static RecipeSerializer<EnchantmentTransposingRecipe> ENCHANTMENT_TRANSPOSING;

	@ObjectHolder(value = MODID + ":void_stone_combine", registryName = "recipe_serializer")
	public static RecipeSerializer<OblivionStoneCombineRecipe> OBLIVION_STONE_COMBINE;

	@ObjectHolder(value = MODID + ":bind_to_player", registryName = "recipe_serializer")
	public static RecipeSerializer<BindToPlayerRecipe> BIND_TO_PLAYER;

	@ObjectHolder(value = MODID + ":crafting_shaped_hidden", registryName = "recipe_serializer")
	public static RecipeSerializer<HiddenRecipe> HIDDEN_SHAPED;

	@ObjectHolder(value = MODID + ":shapeless_no_return_craft", registryName = "recipe_serializer")
	public static RecipeSerializer<ShapelessRecipe> SHAPELESS_NO_RETURN;

	@ObjectHolder(value = MODID + ":crafting_shaped_cursed", registryName = "recipe_serializer")
	public static RecipeSerializer<ShapedRecipe> CURSED_SHAPED;

	@ObjectHolder(value = MODID + ":crafting_shaped_blessed", registryName = "recipe_serializer")
	public static RecipeSerializer<ShapedRecipe> BLESSED_SHAPED;

	private EnigmaticRecipes() {
		super(BuiltInRegistries.RECIPE_SERIALIZER);
		this.register("mending_mixture_repair", () -> MendingMixtureRepairRecipe.SERIALIZER);
		this.register("enchantment_transposing", () -> EnchantmentTransposingRecipe.SERIALIZER);
		this.register("void_stone_combine", () -> OblivionStoneCombineRecipe.SERIALIZER);
		this.register("bind_to_player", () -> BindToPlayerRecipe.SERIALIZER);
		this.register("crafting_shaped_hidden", () -> HiddenRecipe.SERIALIZER);
		// Port: upstream's ShapelessNoReturnRecipe.Serializer resolved to the inherited vanilla
		// shapeless serializer, so these recipes still return container items like vanilla ones.
		this.register("shapeless_no_return_craft", () -> ShapelessNoReturnRecipe.SERIALIZER);
		this.register("crafting_shaped_cursed", () -> CursedShapedRecipe.SERIALIZER);
		this.register("crafting_shaped_blessed", () -> BlessedShapedRecipe.SERIALIZER);
	}

	public static void init() {
		// NO-OP; loading the class registers the serializers.
		// Port: upstream's onRegister CraftingHelper call is EnabledCondition.register() on Fabric.
	}

	private static boolean lateRecipesAdded = false;

	/**
	 * Port: 26.3 binds item components only when a world's registries load, so no ItemStack
	 * can be made during mod init. The brewing and hidden recipes are added here instead, the
	 * first time either registry is read (on the server or the client).
	 */
	public static synchronized void ensureLateRecipes() {
		if (lateRecipesAdded)
			return;

		lateRecipesAdded = true;
		registerBrewingRecipes();
		registerHiddenRecipes();
	}

	/**
	 * Port: upstream registers these in EnigmaticLegacy's common setup through Forge's
	 * BrewingRecipeRegistry; here they go to the port's own registry (see the brewing package).
	 */
	private static void registerBrewingRecipes() {
		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.RECALL_POTION)) {
			// Port: upstream's PotionHelper.createVanillaPotion is not ported; PotionContents.createItemStack does the same.
			BrewingRecipeRegistry.addRecipe(new SpecialBrewingRecipe(BrewingIngredient.of(PotionContents.createItemStack(Items.POTION, Potions.AWKWARD)), BrewingIngredient.of(Items.ENDER_EYE), new ItemStack(EnigmaticItems.RECALL_POTION), Identifier.fromNamespaceAndPath(MODID, "recall_potion")));
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.TWISTED_POTION)) {
			BrewingRecipeRegistry.addRecipe(new SpecialBrewingRecipe(BrewingIngredient.of(EnigmaticItems.RECALL_POTION), BrewingIngredient.of(EnigmaticItems.TWISTED_HEART), new ItemStack(EnigmaticItems.TWISTED_POTION), Identifier.fromNamespaceAndPath(MODID, "twisted_potion")));
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.COMMON_POTION)) {
			PotionHelper.registerCommonPotions();
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.ULTIMATE_POTION)) {
			PotionHelper.registerBasicUltimatePotions();
			PotionHelper.registerSplashUltimatePotions();
			PotionHelper.registerLingeringUltimatePotions();
		}

		BrewingRecipeRegistry.addRecipe(new ValidationBrewingRecipe(BrewingIngredient.of(EnigmaticItems.RECALL_POTION, EnigmaticItems.TWISTED_POTION, EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticItems.COMMON_POTION_LINGERING), null));
	}

	/**
	 * Port: upstream adds these in EnigmaticLegacy's onLoadComplete, right after the brewing
	 * recipes; here they are added by ensureLateRecipes.
	 */
	private static void registerHiddenRecipes() {
		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.THE_CUBE)) {
			HiddenRecipe.addRecipe(new ItemStack(EnigmaticItems.THE_CUBE),
					new ItemStack(EnigmaticItems.GOLEM_HEART), new ItemStack(EnigmaticItems.COSMIC_HEART), new ItemStack(EnigmaticItems.BLAZING_CORE),
					new ItemStack(EnigmaticItems.ANGEL_BLESSING), new ItemStack(Blocks.OBSIDIAN), new ItemStack(EnigmaticItems.EYE_OF_NEBULA),
					new ItemStack(EnigmaticItems.OCEAN_STONE), new ItemStack(EnigmaticItems.COSMIC_HEART), new ItemStack(EnigmaticItems.VOID_PEARL)
					);
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.ASCENSION_AMULET)) {
			HiddenRecipe.addRecipe(new ItemStack(EnigmaticItems.ASCENSION_AMULET),
					new ItemStack(Items.AMETHYST_SHARD), new ItemStack(EnigmaticItems.ASTRAL_DUST), new ItemStack(Items.AMETHYST_SHARD),
					new ItemStack(EnigmaticItems.ETHERIUM_INGOT), new ItemStack(EnigmaticItems.ENIGMATIC_AMULET), new ItemStack(EnigmaticItems.ETHERIUM_INGOT),
					new ItemStack(Items.DRAGON_BREATH), new ItemStack(EnigmaticItems.COSMIC_HEART), new ItemStack(Items.DRAGON_BREATH)
					);
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.ELDRITCH_AMULET)) {
			HiddenRecipe.addRecipe(new ItemStack(EnigmaticItems.ELDRITCH_AMULET),
					new ItemStack(EnigmaticItems.EVIL_ESSENCE), new ItemStack(EnigmaticItems.ABYSSAL_HEART), new ItemStack(EnigmaticItems.EVIL_ESSENCE),
					new ItemStack(Items.NETHERITE_INGOT), new ItemStack(EnigmaticItems.ASCENSION_AMULET), new ItemStack(Items.NETHERITE_INGOT),
					new ItemStack(EnigmaticItems.TWISTED_HEART), new ItemStack(Items.NETHER_STAR), new ItemStack(EnigmaticItems.TWISTED_HEART)
					);
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.COSMIC_SCROLL)) {
			HiddenRecipe.addRecipe(new ItemStack(EnigmaticItems.COSMIC_SCROLL),
					new ItemStack(EnigmaticItems.ASTRAL_DUST), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), new ItemStack(EnigmaticItems.ASTRAL_DUST),
					new ItemStack(EnigmaticItems.ETHERIUM_INGOT), new ItemStack(EnigmaticItems.DARKEST_SCROLL), new ItemStack(EnigmaticItems.ETHERIUM_INGOT),
					new ItemStack(EnigmaticItems.ASTRAL_DUST), new ItemStack(EnigmaticItems.COSMIC_HEART), new ItemStack(EnigmaticItems.ASTRAL_DUST)
					);
		}
	}

}
