package com.aizistral.enigmaticlegacy.crafting;

import com.aizistral.enigmaticlegacy.api.items.ITaintable;
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
 * Upstream also marked the ingredient stacks as tainted for recipe viewers; 1.21.11 ingredients
 * hold items rather than stacks, so that marking is dropped. Matching still requires tainted items.
 */
public class CursedShapedRecipe extends ShapedRecipe {
	// TODO We need one custom recipe handler for all our mod stuff
	private final ShapedRecipePattern pattern;
	private final ItemStack result;

	private static final MapCodec<CursedShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::group),
			CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
			ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
			ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
			Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
			).apply(builder, CursedShapedRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, CursedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, ShapedRecipe::group,
			CraftingBookCategory.STREAM_CODEC, ShapedRecipe::category,
			ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
			ItemStack.STREAM_CODEC, recipe -> recipe.result,
			ByteBufCodecs.BOOL, ShapedRecipe::showNotification,
			CursedShapedRecipe::new);

	public static final RecipeSerializer<CursedShapedRecipe> SERIALIZER = new RecipeSerializer<>() {
		@Override
		public MapCodec<CursedShapedRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CursedShapedRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	};

	public CursedShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
		super(group, category, pattern, result, showNotification);
		this.pattern = pattern;
		this.result = result;
	}

	@Override
	public RecipeSerializer<CursedShapedRecipe> getSerializer() {
		return EnigmaticRecipes.CURSED_SHAPED;
	}

	@Override
	public boolean matches(CraftingInput inv, Level worldIn) {
		boolean isAllTainted = true;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (checkedItemStack.getItem() instanceof ITaintable && !((ITaintable)checkedItemStack.getItem()).isTainted(checkedItemStack)) {
				isAllTainted = false;
			}
		}

		return super.matches(inv, worldIn) && isAllTainted;
	}

}
