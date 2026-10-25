package com.aizistral.enigmaticlegacy.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.CosmicHeart;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

public class HiddenRecipe extends CustomRecipe {
	// Port: 26.3 custom recipes have no data, so the serializer encodes one shared instance.
	public static final HiddenRecipe INSTANCE = new HiddenRecipe();
	public static final RecipeSerializer<HiddenRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));
	static final Map<ItemStack[][], ItemStack> RECIPES = new HashMap<>();
	static final Entry<ItemStack[][], ItemStack> EMPTY = new Entry<ItemStack[][], ItemStack>() {
		private final ItemStack[][] recipe = new ItemStack[][] {
			{ ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY },
			{ ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY },
			{ ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY }
		};

		@Override
		public ItemStack[][] getKey() {
			return this.recipe;
		}

		@Override
		public ItemStack getValue() {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack setValue(ItemStack value) {
			throw new UnsupportedOperationException();
		}
	};

	public HiddenRecipe() {
		super();
	}

	@Override
	public ItemStack assemble(CraftingInput inv) {
		ItemStack output = ItemStack.EMPTY;

		Optional<DataComponentPatch> amuletNBT = Optional.empty();

		recipes: for (Map.Entry<ItemStack[][], ItemStack> entry : recipes().entrySet()) {
			ItemStack[][] grid = align(inv, entry.getKey());

			if (grid == null) {
				continue;
			}

			for (int r = 0; r < 3; r++) {
				for (int i = 0; i < 3; i++) {
					ItemStack slotStack = grid[r][i];

					if (slotStack.getItem() != entry.getKey()[r][i].getItem()) {
						continue recipes;
					} else {
						if (slotStack.is(EnigmaticItems.ENIGMATIC_AMULET) || slotStack.is(EnigmaticItems.ASCENSION_AMULET)) {
							// Port: null check added; an amulet without NBT (e.g. from the creative tab) would crash here.
							if (!slotStack.getComponentsPatch().isEmpty()) {
								amuletNBT = Optional.of(slotStack.getComponentsPatch());
							}
						}
					}
				}
			}

			output = entry.getValue().copy();
			amuletNBT.ifPresent(output::applyComponents);
			break;
		}

		if (!OmniconfigHandler.isItemEnabled(output.getItem())) {
			output = ItemStack.EMPTY;
		}

		return output;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		recipes: for (ItemStack[][] array : recipes().keySet()) {
			ItemStack[][] grid = align(inv, array);

			if (grid == null) {
				continue;
			}

			for (int r = 0; r < 3; r++) {
				for (int i = 0; i < 3; i++) {
					ItemStack slotStack = grid[r][i];

					if (slotStack.getItem() != array[r][i].getItem()) {
						continue recipes;
					} else if (recipes().get(array).is(EnigmaticItems.COSMIC_SCROLL) &&
							slotStack.getItem() instanceof CosmicHeart &&
							!ItemNBTHelper.getBoolean(slotStack, "isBelieverBlessed", false)) {
						continue recipes;
					}
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Port: 1.21 hands recipes a CraftingInput trimmed to the occupied area, so the absolute slot
	 * positions upstream compared are gone. This places the input back into a 3x3 grid at the
	 * recipe's own offset, or returns null when the occupied area has a different size.
	 */
	private static ItemStack[][] align(CraftingInput inv, ItemStack[][] recipe) {
		int minR = 3, minC = 3, maxR = -1, maxC = -1;

		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				if (!recipe[r][c].isEmpty()) {
					minR = Math.min(minR, r);
					minC = Math.min(minC, c);
					maxR = Math.max(maxR, r);
					maxC = Math.max(maxC, c);
				}
			}
		}

		if (maxR < 0 || inv.height() != maxR - minR + 1 || inv.width() != maxC - minC + 1)
			return null;

		ItemStack[][] grid = new ItemStack[3][3];

		for (ItemStack[] row : grid) {
			java.util.Arrays.fill(row, ItemStack.EMPTY);
		}

		for (int r = 0; r < inv.height(); r++) {
			for (int c = 0; c < inv.width(); c++) {
				grid[minR + r][minC + c] = inv.getItem(c, r);
			}
		}

		return grid;
	}

	@Override
	public RecipeSerializer<HiddenRecipe> getSerializer() {
		return SERIALIZER;
	}

	public static void addRecipe(ItemStack output, ItemStack... inputs) {
		ItemStack[][] array = new ItemStack[][] {
			{ inputs[0], inputs[1], inputs[2] },
			{ inputs[3], inputs[4], inputs[5] },
			{ inputs[6], inputs[7], inputs[8] }
		};
		RECIPES.put(array, output);
	}

	/** Port: the hidden recipes are added on first use, once 26.3 has bound item components. */
	private static Map<ItemStack[][], ItemStack> recipes() {
		EnigmaticRecipes.ensureLateRecipes();
		return RECIPES;
	}

	public static Entry<ItemStack[][], ItemStack> getRecipe(Identifier output) {
		// Port: ForgeRegistries.ITEMS -> BuiltInRegistries.ITEM.
		return recipes().entrySet().stream().filter(entry -> BuiltInRegistries.ITEM.getKey(entry.getValue().getItem())
				.equals(output)).findFirst().orElse(EMPTY);
	}

}
