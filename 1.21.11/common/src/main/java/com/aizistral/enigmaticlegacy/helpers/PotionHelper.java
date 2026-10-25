package com.aizistral.enigmaticlegacy.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aizistral.enigmaticlegacy.brewing.ComplexBrewingRecipe;
import com.aizistral.enigmaticlegacy.objects.AdvancedPotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import com.aizistral.enigmaticlegacy.brewing.BrewingIngredient;
import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;

/**
 * Methods for interacting with advanced potion system.
 * Registering methods for them are also stored here.
 * @author Integral
 */

public class PotionHelper {

	public static int getColor(ItemStack stack) {

		if (PotionHelper.isAdvancedPotion(stack))
			if (PotionHelper.getEffects(stack) != null && PotionHelper.getEffects(stack).size() > 0)
				return PotionContents.getColorOptional(PotionHelper.getEffects(stack)).orElse(PotionContents.BASE_POTION_COLOR);

		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();

	};

	public static boolean isAdvancedPotion(ItemStack stack) {
		if (ItemNBTHelper.verifyExistance(stack, "EnigmaticPotion"))
			return true;


		return false;
	}

	public static List<MobEffectInstance> getEffects(ItemStack stack) {
		if (PotionHelper.isAdvancedPotion(stack)) {
			AdvancedPotion potion = PotionHelper.getAdvancedPotion(ItemNBTHelper.getString(stack, "EnigmaticPotion", "nothing"));
			if (potion != null)
				return potion.getEffects();
		}

		return new ArrayList<MobEffectInstance>();
	};

	public static AdvancedPotion getAdvancedPotion(ItemStack stack) {
		if (PotionHelper.isAdvancedPotion(stack))
			return PotionHelper.getAdvancedPotion(ItemNBTHelper.getString(stack, "EnigmaticPotion", "nothing"));

		return EnigmaticPotions.EMPTY_POTION;
	}

	public static ItemStack setAdvancedPotion(ItemStack stack, AdvancedPotion potion) {
		ItemNBTHelper.setString(stack, "EnigmaticPotion", potion.getId());
		return stack;
	}

	/**
	 * Port: the custom data tag {@link #setAdvancedPotion} writes, built without an ItemStack.
	 * Loot tables load before item components are bound, so loot functions can't create stacks.
	 */
	public static CompoundTag advancedPotionTag(AdvancedPotion potion) {
		CompoundTag tag = new CompoundTag();
		tag.putString("EnigmaticPotion", potion.getId());
		return tag;
	}

	public static ItemStack setAdvancedPotion(ItemStack stack, String id) {
		ItemNBTHelper.setString(stack, "EnigmaticPotion", id);
		return stack;
	}

	public static AdvancedPotion getAdvancedPotion(String identifier) {

		for (AdvancedPotion potion : EnigmaticPotions.ULTIMATE_POTIONS) {
			if (potion.getId().equals(identifier))
				return potion;
		}

		for (AdvancedPotion potion : EnigmaticPotions.COMMON_POTIONS) {
			if (potion.getId().equals(identifier))
				return potion;
		}


		return EnigmaticPotions.EMPTY_POTION;
	}

	public static HashMap<BrewingIngredient, BrewingIngredient> constructIngredientMap(BrewingIngredient... ingredients) {
		HashMap<BrewingIngredient, BrewingIngredient> returnMap = new HashMap<BrewingIngredient, BrewingIngredient>();

		if (ingredients.length % 2 != 0)
			throw new IllegalArgumentException("Uneven number of ingredients passed. This must not be!");

		for (int counter = 0; counter < ingredients.length; counter += 2) {
			returnMap.put(ingredients[counter], ingredients[counter+1]);
		}

		return returnMap;
	}

	public static ItemStack createAdvancedPotion(Item item, AdvancedPotion potion) {
		return PotionHelper.setAdvancedPotion(new ItemStack(item), potion);
	}

	public static ItemStack createVanillaPotion(Item item, Holder<Potion> potion) {
		return PotionContents.createItemStack(item, potion);
	}

	public static void registerCommonPotions() {

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.AWKWARD)),
								BrewingIngredient.of(Items.QUARTZ)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.REDSTONE)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.GLOWSTONE_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.STRONG_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.AWKWARD)),
								BrewingIngredient.of(Items.QUARTZ),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.REDSTONE),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_HASTE)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.GLOWSTONE_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.STRONG_HASTE)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.STRONG_HASTE)
						));



		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.AWKWARD)),
								BrewingIngredient.of(Items.QUARTZ),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.REDSTONE),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_HASTE)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.LONG_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.HASTE)),
								BrewingIngredient.of(Items.GLOWSTONE_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.STRONG_HASTE)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.STRONG_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.MUNDANE)),
								BrewingIngredient.of(Items.BLAZE_ROD)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.MOLTEN_HEART)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.MOLTEN_HEART)),
								BrewingIngredient.of(Items.REDSTONE)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_MOLTEN_HEART)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.MUNDANE)),
								BrewingIngredient.of(Items.BLAZE_ROD),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.MOLTEN_HEART)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.MOLTEN_HEART)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.MOLTEN_HEART)),
								BrewingIngredient.of(Items.REDSTONE),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_MOLTEN_HEART)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_MOLTEN_HEART)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.MUNDANE)),
								BrewingIngredient.of(Items.BLAZE_ROD),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.MOLTEN_HEART)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.MOLTEN_HEART)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.MOLTEN_HEART)),
								BrewingIngredient.of(Items.REDSTONE),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_MOLTEN_HEART)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.LONG_MOLTEN_HEART)
						));
	}


	public static void registerBasicUltimatePotions() {
		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_NIGHT_VISION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_NIGHT_VISION)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_INVISIBILITY)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_INVISIBILITY)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_FIRE_RESISTANCE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_FIRE_RESISTANCE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_HEALING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HEALING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_HARMING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HARMING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_SLOW_FALLING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTMATE_SLOW_FALLING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_WEAKNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_WEAKNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_WATER_BREATHING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_WATER_BREATHING)
						));


		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_STRENGTH)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_STRENGTH)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_STRENGTH)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_LEAPING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_LEAPING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_LEAPING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_SWIFTNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_SWIFTNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_SWIFTNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_SLOWNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_SLOWNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_SLOWNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_TURTLE_MASTER)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_TURTLE_MASTER)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_TURTLE_MASTER)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_POISON)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_POISON)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_POISON)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.LONG_REGENERATION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.POTION, Potions.STRONG_REGENERATION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_REGENERATION)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_HASTE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.STRONG_HASTE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.LONG_MOLTEN_HEART)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_MOLTEN_HEART)
						));
	}



	public static void registerSplashUltimatePotions() {
		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_NIGHT_VISION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_NIGHT_VISION)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_NIGHT_VISION)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_INVISIBILITY)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_INVISIBILITY)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_INVISIBILITY)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_FIRE_RESISTANCE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_FIRE_RESISTANCE)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_FIRE_RESISTANCE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_HEALING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HEALING)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HEALING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_HARMING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HARMING)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HARMING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_SLOW_FALLING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTMATE_SLOW_FALLING)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTMATE_SLOW_FALLING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_WEAKNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_WEAKNESS)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_WEAKNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_WATER_BREATHING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_WATER_BREATHING)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_WATER_BREATHING)
						));




		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_STRENGTH)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_STRENGTH)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_STRENGTH)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_STRENGTH)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_LEAPING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_LEAPING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_LEAPING)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_LEAPING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_SWIFTNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_SWIFTNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_SWIFTNESS)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_SWIFTNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_SLOWNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_SLOWNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_SLOWNESS)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_SLOWNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_TURTLE_MASTER)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_TURTLE_MASTER)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_TURTLE_MASTER)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_TURTLE_MASTER)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_POISON)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_POISON)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_POISON)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_POISON)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.LONG_REGENERATION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.SPLASH_POTION, Potions.STRONG_REGENERATION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_REGENERATION)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_REGENERATION)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_HASTE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.STRONG_HASTE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_HASTE)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticPotions.LONG_MOLTEN_HEART)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION, EnigmaticPotions.ULTIMATE_MOLTEN_HEART)),
								BrewingIngredient.of(Items.GUNPOWDER)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_MOLTEN_HEART)
						));
	}



	public static void registerLingeringUltimatePotions() {
		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_NIGHT_VISION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_NIGHT_VISION)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_NIGHT_VISION)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_INVISIBILITY)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_INVISIBILITY)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_INVISIBILITY)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_FIRE_RESISTANCE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_FIRE_RESISTANCE)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_FIRE_RESISTANCE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_HEALING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HEALING)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_HEALING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_HARMING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HARMING)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_HARMING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_SLOW_FALLING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTMATE_SLOW_FALLING)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTMATE_SLOW_FALLING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_WEAKNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_WEAKNESS)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_WEAKNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_WATER_BREATHING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_WATER_BREATHING)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_WATER_BREATHING)
						));




		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_STRENGTH)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_STRENGTH)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_STRENGTH)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_STRENGTH)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_LEAPING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_LEAPING)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_LEAPING)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_LEAPING)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_SWIFTNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_SWIFTNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_SWIFTNESS)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_SWIFTNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_SLOWNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_SLOWNESS)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_SLOWNESS)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_SLOWNESS)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_TURTLE_MASTER)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_TURTLE_MASTER)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_TURTLE_MASTER)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_TURTLE_MASTER)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_POISON)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_POISON)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_POISON)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_POISON)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.LONG_REGENERATION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createVanillaPotion(Items.LINGERING_POTION, Potions.STRONG_REGENERATION)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_REGENERATION)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_REGENERATION)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.LONG_HASTE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.STRONG_HASTE)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_HASTE)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_HASTE)
						));

		BrewingRecipeRegistry.addRecipe(
				new ComplexBrewingRecipe(
						PotionHelper.constructIngredientMap(
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION_LINGERING, EnigmaticPotions.LONG_MOLTEN_HEART)),
								BrewingIngredient.of(EnigmaticItems.ASTRAL_DUST),
								BrewingIngredient.of(PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_SPLASH, EnigmaticPotions.ULTIMATE_MOLTEN_HEART)),
								BrewingIngredient.of(Items.DRAGON_BREATH)
								),
						PotionHelper.createAdvancedPotion(EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticPotions.ULTIMATE_MOLTEN_HEART)
						));
	}

}
