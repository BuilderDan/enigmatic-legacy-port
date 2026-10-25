package com.aizistral.enigmaticlegacy.crafting;

import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.CosmicHeart;
import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Port: 26.3 recipes are read with codecs. The codecs mirror vanilla ShapedRecipe's, keeping
 * own copies of the pattern and result (vanilla's fields are private). ShapedRecipe's
 * getSerializer is typed RecipeSerializer<ShapedRecipe>, so the serializer is typed that way too.
 * Upstream also marked the Cosmic Heart ingredient stacks as blessed for recipe viewers; 26.3 ingredients
 * hold items rather than stacks, so that marking is dropped. Matching still requires blessed hearts.
 */
public class BlessedShapedRecipe extends ShapedRecipe {
	private final ShapedRecipePattern pattern;
	private final ItemStackTemplate result;

	private static final MapCodec<BlessedShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
			CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
			ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
			).apply(builder, BlessedShapedRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, BlessedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
			CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
			ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
			ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
			BlessedShapedRecipe::new);

	public static final RecipeSerializer<ShapedRecipe> SERIALIZER = new RecipeSerializer<>(
			CODEC.xmap(recipe -> recipe, recipe -> (BlessedShapedRecipe) recipe),
			STREAM_CODEC.map(recipe -> recipe, recipe -> (BlessedShapedRecipe) recipe));

	public BlessedShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
		super(commonInfo, bookInfo, pattern, result);
		this.pattern = pattern;
		this.result = result;
	}

	@Override
	public RecipeSerializer<ShapedRecipe> getSerializer() {
		return EnigmaticRecipes.BLESSED_SHAPED;
	}

	@Override
	public boolean matches(CraftingInput inv, Level worldIn) {
		boolean isAllBlessed = true;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (checkedItemStack.getItem() instanceof CosmicHeart && !ItemNBTHelper.getBoolean(checkedItemStack, "isBelieverBlessed", false)) {
				isAllBlessed = false;
			}
		}

		return isAllBlessed && super.matches(inv, worldIn);
	}

}
