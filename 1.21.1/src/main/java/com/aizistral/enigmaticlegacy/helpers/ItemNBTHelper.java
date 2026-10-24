package com.aizistral.enigmaticlegacy.helpers;

import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * A couple of methods for mork convenient work with NBT of ItemStacks.
 * Originated from Botania's code.
 * <p>
 * Port: 1.21 items have no NBT tag; this keeps the same API on top of the minecraft:custom_data
 * component. getNBT returns a copy, so changes to it only stick through injectNBT or the setters.
 * @author Integral
 */

public final class ItemNBTHelper {
	public static boolean detectNBT(final ItemStack stack) {
		return stack.has(DataComponents.CUSTOM_DATA);
	}

	public static void initNBT(final ItemStack stack) {
		if (!ItemNBTHelper.detectNBT(stack)) {
			ItemNBTHelper.injectNBT(stack, new CompoundTag());
		}
	}

	public static void injectNBT(final ItemStack stack, final CompoundTag nbt) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
	}

	public static CompoundTag getNBT(final ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	/** Port: edits the custom data in place (the 1.21 stand-in for getNBT(stack).putX). */
	public static void updateNBT(final ItemStack stack, final Consumer<CompoundTag> updater) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
	}

	public static void setBoolean(final ItemStack stack, final String tag, final boolean b) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putBoolean(tag, b));
	}

	public static void setByte(final ItemStack stack, final String tag, final byte b) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putByte(tag, b));
	}

	public static void setShort(final ItemStack stack, final String tag, final short s) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putShort(tag, s));
	}

	public static void setInt(final ItemStack stack, final String tag, final int i) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putInt(tag, i));
	}

	public static void setLong(final ItemStack stack, final String tag, final long l) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putLong(tag, l));
	}

	public static void setFloat(final ItemStack stack, final String tag, final float f) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putFloat(tag, f));
	}

	public static void setDouble(final ItemStack stack, final String tag, final double d) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putDouble(tag, d));
	}

	public static void setString(final ItemStack stack, final String tag, final String s) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putString(tag, s));
	}

	public static void setUUID(final ItemStack stack, final String tag, final UUID id) {
		ItemNBTHelper.updateNBT(stack, nbt -> nbt.putUUID(tag, id));
	}

	public static boolean verifyExistance(final ItemStack stack, final String tag) {
		return stack != null && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(tag);
	}

	public static boolean getBoolean(final ItemStack stack, final String tag, final boolean defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getBoolean(tag) : defaultExpected;
	}

	public static boolean containsUUID(final ItemStack stack, final String tag) {
		return stack != null && ItemNBTHelper.getNBT(stack).hasUUID(tag);
	}

	public static UUID getUUID(final ItemStack stack, final String tag, UUID defaultExpected) {
		return ItemNBTHelper.containsUUID(stack, tag) ? ItemNBTHelper.getNBT(stack).getUUID(tag) : defaultExpected;
	}

	public static byte getByte(final ItemStack stack, final String tag, final byte defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getByte(tag) : defaultExpected;
	}

	public static short getShort(final ItemStack stack, final String tag, final short defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getShort(tag) : defaultExpected;
	}

	public static int getInt(final ItemStack stack, final String tag, final int defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getInt(tag) : defaultExpected;
	}

	public static long getLong(final ItemStack stack, final String tag, final long defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getLong(tag) : defaultExpected;
	}

	public static float getFloat(final ItemStack stack, final String tag, final float defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getFloat(tag) : defaultExpected;
	}

	public static double getDouble(final ItemStack stack, final String tag, final double defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getDouble(tag) : defaultExpected;
	}

	public static CompoundTag getCompound(final ItemStack stack, final String tag, final boolean nullifyOnFail) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getCompound(tag) : (nullifyOnFail ? null : new CompoundTag());
	}

	public static String getString(final ItemStack stack, final String tag, final String defaultExpected) {
		return ItemNBTHelper.verifyExistance(stack, tag) ? ItemNBTHelper.getNBT(stack).getString(tag) : defaultExpected;
	}

}
