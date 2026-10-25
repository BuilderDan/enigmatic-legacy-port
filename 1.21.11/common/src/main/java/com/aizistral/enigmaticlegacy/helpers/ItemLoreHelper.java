package com.aizistral.enigmaticlegacy.helpers;

import java.util.ArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// Port: @OnlyIn -> @Environment(EnvType.CLIENT) on the Minecraft-using methods.
public class ItemLoreHelper {

	@Environment(EnvType.CLIENT)
	public static void indicateCursedOnesOnly(List<Component> list) {
		ChatFormatting format;

		if (Minecraft.getInstance().player != null) {
			format = SuperpositionHandler.isTheCursedOne(Minecraft.getInstance().player) ? ChatFormatting.GOLD : ChatFormatting.DARK_RED;
		} else {
			format = ChatFormatting.DARK_RED;
		}
		// TODO God do I hate making stylistic choices
		//format = ChatFormatting.DARK_RED;

		list.add(Component.translatable("tooltip.enigmaticlegacy.cursedOnesOnly1").withStyle(format));
		list.add(Component.translatable("tooltip.enigmaticlegacy.cursedOnesOnly2").withStyle(format));

	}

	@Environment(EnvType.CLIENT)
	public static void indicateBlessedOnesOnly(List<Component> list) {
		ChatFormatting format;

		if (Minecraft.getInstance().player != null) {
			format = SuperpositionHandler.isTheBlessedOne(Minecraft.getInstance().player) ? ChatFormatting.GOLD : ChatFormatting.DARK_RED;
		} else {
			format = ChatFormatting.DARK_RED;
		}

		list.add(Component.translatable("tooltip.enigmaticlegacy.blessedOnesOnly1").withStyle(format));
		list.add(Component.translatable("tooltip.enigmaticlegacy.blessedOnesOnly2").withStyle(format));

	}

	@Environment(EnvType.CLIENT)
	public static void indicateWorthyOnesOnly(List<Component> list) {
		ChatFormatting format = ChatFormatting.DARK_RED;
		Player player = Minecraft.getInstance().player;

		if (player != null) {
			format = SuperpositionHandler.isTheWorthyOne(Minecraft.getInstance().player) ? ChatFormatting.GOLD : ChatFormatting.DARK_RED;
		}

		double requiredCurse = SuperpositionHandler.roundToPlaces(100 * CursedRing.superCursedTime.getValue(), 1);

		list.add(Component.translatable("tooltip.enigmaticlegacy.worthyOnesOnly1"));
		list.add(Component.translatable("tooltip.enigmaticlegacy.worthyOnesOnly2",
						Component.literal(requiredCurse + "%").withStyle(ChatFormatting.GOLD))
				.withStyle(format));
		list.add(Component.translatable("tooltip.enigmaticlegacy.worthyOnesOnly3"));
		list.add(Component.translatable("tooltip.enigmaticlegacy.void"));
		list.add(Component.translatable("tooltip.enigmaticlegacy.worthyOnesOnly4")
				.withStyle(format).append(Component.literal(" " + SuperpositionHandler.getSufferingTime(player))
						.withStyle(ChatFormatting.LIGHT_PURPLE)));
	}

	public static void addLocalizedFormattedString(List<Component> list, String str, ChatFormatting format) {
		list.add(Component.translatable(str).withStyle(format));
	}

	public static void addLocalizedString(List<Component> list, String str) {
		list.add(Component.translatable(str));
	}

	public static void addLocalizedString(List<Component> list, String str, @Nullable ChatFormatting format, Object... values) {
		Component[] stringValues = new Component[values.length];

		int counter = 0;
		for (Object value : values) {
			MutableComponent comp;

			if (value instanceof MutableComponent) {
				comp = (MutableComponent)value;
			} else {
				comp = Component.literal(value.toString());
			}

			if (format != null) {
				comp.withStyle(format);
			}

			stringValues[counter] = comp;
			counter++;
		}

		list.add(Component.translatable(str, (Object[])stringValues));
	}

	// Port: the display NBT tag is gone in 1.21; name and lore use the CUSTOM_NAME and LORE components.
	public static ItemStack mergeDisplayData(ItemStack from, ItemStack to) {
		ItemLore fromLore = from.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
		ItemLore lore = !fromLore.lines().isEmpty() ? fromLore : to.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
		Component displayName = from.has(DataComponents.CUSTOM_NAME) ? from.get(DataComponents.CUSTOM_NAME) : to.get(DataComponents.CUSTOM_NAME);

		to.set(DataComponents.LORE, lore);
		to.set(DataComponents.CUSTOM_NAME, displayName);

		return to;
	}

	private static List<Component> getLore(ItemStack stack) {
		return new ArrayList<>(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
	}

	private static ItemStack setLore(ItemStack stack, List<Component> loreList) {
		stack.set(DataComponents.LORE, new ItemLore(loreList));
		return stack;
	}

	public static ItemStack addLoreString(ItemStack stack, String string) {
		List<Component> loreList = getLore(stack);
		loreList.add(Component.literal(string));

		return setLore(stack, loreList);
	}

	public static ItemStack setLoreString(ItemStack stack, String string, int index) {
		List<Component> loreList = getLore(stack);
		if (loreList.size() - 1 >= index) {
			loreList.set(index, Component.literal(string));
		} else {
			loreList.add(Component.literal(string));
		}

		return setLore(stack, loreList);
	}

	public static ItemStack removeLoreString(ItemStack stack, int index) {
		List<Component> loreList = getLore(stack);

		if (loreList.size() > 0) {
			if (index == -1) {
				loreList.remove(loreList.size() - 1);
			} else if (loreList.size() - 1 >= index) {
				loreList.remove(index);
			}
		}

		return setLore(stack, loreList);
	}

	public static ItemStack setLastLoreString(ItemStack stack, String string) {
		List<Component> loreList = getLore(stack);

		if (loreList.size() > 0) {
			loreList.set(loreList.size() - 1, Component.literal(string));
		} else {
			loreList.add(Component.literal(string));
		}

		return setLore(stack, loreList);
	}

	public static ItemStack setDisplayName(ItemStack stack, String name) {
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));

		return stack;
	}

	public static class AnvilParser {
		private boolean isLore;
		private int loreIndex;
		private boolean removeString;
		private String handledString;

		private AnvilParser(String string) {
			this.loreIndex = -1;
			this.handledString = string.toString();
			this.isLore = this.handledString.startsWith("!");
			this.removeString = this.handledString.startsWith("-!");

			if (this.isLore) {
				this.handledString = this.handledString.replaceFirst("!", "");
				String index = AnvilParser.parseIndex(this.handledString);
				this.loreIndex = Integer.parseInt(index);

				if (this.loreIndex != -1) {
					this.handledString = this.handledString.replaceFirst(index, "");
				}
			} else if (this.removeString) {
				this.handledString = this.handledString.replaceFirst("-!", "");
				String index = AnvilParser.parseIndex(this.handledString);
				this.loreIndex = Integer.parseInt(index);

				if (this.loreIndex != -1) {
					this.handledString = this.handledString.replaceFirst(index, "");
				}
			}

			this.handledString = AnvilParser.parseFormatting(this.handledString);

		}

		public static AnvilParser parseField(String field) {
			return new AnvilParser(field);
		}

		private static String parseFormatting(String field) {
			String formatter = Component.translatable("tooltip.enigmaticlegacy.paragraph").getString();
			String subformat = Component.translatable("tooltip.enigmaticlegacy.subformat").getString();

			return field.replace(subformat, formatter);
		}

		private static String parseIndex(String field) {
			String number = "";
			int index = -1;

			for (char symbol : field.toCharArray()) {
				if (Character.isDigit(symbol)) {
					number = number + symbol;
				} else {
					break;
				}

				if (number.length() >= 2) {
					break;
				}
			}

			if (!number.equals(""))
				return number;

			return "" + index;
		}

		public boolean isLoreString() {
			return this.isLore;
		}

		public boolean shouldRemoveString() {
			return this.removeString;
		}

		public int getLoreIndex() {
			return this.loreIndex;
		}

		public String getFormattedString() {
			return this.handledString;
		}

	}

}
