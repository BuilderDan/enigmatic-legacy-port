package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.crafting.BindToPlayerRecipe;
import com.aizistral.enigmaticlegacy.crafting.BlessedShapedRecipe;
import com.aizistral.enigmaticlegacy.crafting.CursedShapedRecipe;
import com.aizistral.enigmaticlegacy.crafting.EnchantmentTransposingRecipe;
import com.aizistral.enigmaticlegacy.crafting.MendingMixtureRepairRecipe;
import com.aizistral.enigmaticlegacy.crafting.ShapelessNoReturnRecipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class EnigmaticRecipes extends AbstractRegistry<RecipeSerializer<?>> {
	private static final EnigmaticRecipes INSTANCE = new EnigmaticRecipes();

	@ObjectHolder(value = MODID + ":mending_mixture_repair", registryName = "recipe_serializer")
	public static RecipeSerializer<MendingMixtureRepairRecipe> MENDING_MIXTURE_REPAIR;

	@ObjectHolder(value = MODID + ":enchantment_transposing", registryName = "recipe_serializer")
	public static RecipeSerializer<EnchantmentTransposingRecipe> ENCHANTMENT_TRANSPOSING;

	// Port: void_stone_combine (OblivionStoneCombineRecipe) is not registered yet; it needs the
	// Keystone of the Oblivion (VOID_STONE). Its recipe JSON is skipped by its is_enabled condition.

	@ObjectHolder(value = MODID + ":bind_to_player", registryName = "recipe_serializer")
	public static RecipeSerializer<BindToPlayerRecipe> BIND_TO_PLAYER;

	// Port: crafting_shaped_hidden (HiddenRecipe) is not registered yet; it needs the Enigmatic
	// Amulet, Ascension Amulet and Cosmic Scroll. Its recipe JSONs are skipped by is_enabled.

	@ObjectHolder(value = MODID + ":shapeless_no_return_craft", registryName = "recipe_serializer")
	public static RecipeSerializer<ShapelessRecipe> SHAPELESS_NO_RETURN;

	@ObjectHolder(value = MODID + ":crafting_shaped_cursed", registryName = "recipe_serializer")
	public static RecipeSerializer<CursedShapedRecipe> CURSED_SHAPED;

	@ObjectHolder(value = MODID + ":crafting_shaped_blessed", registryName = "recipe_serializer")
	public static RecipeSerializer<BlessedShapedRecipe> BLESSED_SHAPED;

	private EnigmaticRecipes() {
		super(BuiltInRegistries.RECIPE_SERIALIZER);
		this.register("mending_mixture_repair", () -> MendingMixtureRepairRecipe.SERIALIZER);
		this.register("enchantment_transposing", () -> EnchantmentTransposingRecipe.SERIALIZER);
		this.register("bind_to_player", () -> BindToPlayerRecipe.SERIALIZER);
		// Port: kept as upstream wrote it. ShapelessNoReturnRecipe.Serializer resolves to the
		// inherited vanilla ShapelessRecipe.Serializer (upstream's nested class is named
		// "Serialize"), so these recipes still return container items like vanilla ones.
		this.register("shapeless_no_return_craft", ShapelessNoReturnRecipe.Serializer::new);
		this.register("crafting_shaped_cursed", CursedShapedRecipe.Serializer::new);
		this.register("crafting_shaped_blessed", BlessedShapedRecipe.Serializer::new);
	}

	public static void init() {
		// NO-OP; loading the class registers the serializers.
		// Port: upstream's onRegister CraftingHelper call is EnabledCondition.register() on Fabric.
	}

}
