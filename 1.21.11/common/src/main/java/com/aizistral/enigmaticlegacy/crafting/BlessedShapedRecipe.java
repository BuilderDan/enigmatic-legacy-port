package com.aizistral.enigmaticlegacy.crafting;

import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.CosmicHeart;
import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Port: 1.21.11 recipes are read with codecs. The codecs mirror vanilla ShapedRecipe.Serializer's,
 * keeping own copies of the pattern and result (vanilla's fields are package-private).
 * Upstream also marked the Cosmic Heart ingredient stacks as blessed for recipe viewers; 1.21.11
 * ingredients hold items rather than stacks, so that marking is dropped. Matching still requires blessed hearts.
 */
public class BlessedShapedRecipe extends ShapedRecipe {
	private final ShapedRecipePattern pattern;
	private final ItemStack result;

	private static final MapCodec<BlessedShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group),
			CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
			ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
			Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
			).apply(builder, BlessedShapedRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, BlessedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, ShapedRecipe::group,
			CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
			ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
			ItemStack.STREAM_CODEC, recipe -> recipe.result,
			ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
			BlessedShapedRecipe::new);

	public static final RecipeSerializer<BlessedShapedRecipe> SERIALIZER = new RecipeSerializer<>() {
		@Override
		public MapCodec<BlessedShapedRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlessedShapedRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	};

	public BlessedShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
		super(group, category, pattern, result, showNotification);
		this.pattern = pattern;
		this.result = result;
	}

	@Override
	public RecipeSerializer<BlessedShapedRecipe> getSerializer() {
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
