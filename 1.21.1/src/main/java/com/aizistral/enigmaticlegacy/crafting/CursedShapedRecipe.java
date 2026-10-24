package com.aizistral.enigmaticlegacy.crafting;

import com.aizistral.enigmaticlegacy.api.items.ITaintable;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Port: 1.21 recipes have no id and are read with codecs. The serializer mirrors vanilla's
 * ShapedRecipe.Serializer, keeping its own copy of the pattern (vanilla's field is not public).
 */
public class CursedShapedRecipe extends ShapedRecipe {
	// TODO We need one custom recipe handler for all our mod stuff
	private final ShapedRecipePattern pattern;

	public CursedShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
		super(group, category, pattern, result, showNotification);
		this.pattern = pattern;
		Serializer.handleTainted(pattern.ingredients());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
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

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
		return super.assemble(inv, access);
	}

	public static class Serializer implements RecipeSerializer<CursedShapedRecipe> {
		private static final MapCodec<CursedShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
				ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
				Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ShapedRecipe::showNotification)
				).apply(builder, CursedShapedRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, CursedShapedRecipe> STREAM_CODEC = StreamCodec.of(
				Serializer::toNetwork, Serializer::fromNetwork);

		private static NonNullList<Ingredient> handleTainted(NonNullList<Ingredient> ingredientList) {
			for (Ingredient ing : ingredientList) {
				for (ItemStack stack : ing.getItems()) {
					if (stack.getItem() instanceof ITaintable) {
						ItemNBTHelper.setBoolean(stack, "isTainted", true);
					}
				}
			}

			return ingredientList;
		}

		@Override
		public MapCodec<CursedShapedRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CursedShapedRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static CursedShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			String group = buffer.readUtf();
			CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
			ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
			ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
			return new CursedShapedRecipe(group, category, pattern, result, buffer.readBoolean());
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, CursedShapedRecipe recipe) {
			buffer.writeUtf(recipe.getGroup());
			buffer.writeEnum(recipe.category());
			ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
			buffer.writeBoolean(recipe.showNotification());
		}

	}

}
