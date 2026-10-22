package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Port: tabs are built with FabricItemGroup, and their contents come from each item's
 * {@link ICreativeTabMember} methods in displayItems (upstream fills them in Forge's
 * BuildCreativeModeTabContentsEvent).
 */
public class EnigmaticTabs extends AbstractRegistry<CreativeModeTab> {
	// Object holders don't work for creative tabs for some reason
	public static final CreativeModeTab MAIN;

	// Port: POTIONS ("tab_potions", icon RECALL_POTION) returns with the potion items.

	static {
		// Port: icon is THICC_SCROLL until ENIGMATIC_ITEM is ported.
		MAIN = FabricItemGroup.builder().title(Component.translatable("itemGroup.enigmaticCreativeTab"))
				.icon(() -> new ItemStack(EnigmaticItems.THICC_SCROLL))
				.displayItems((params, output) -> fillTab(EnigmaticTabs.MAIN, output)).build();
	}

	private static final EnigmaticTabs INSTANCE = new EnigmaticTabs();

	private EnigmaticTabs() {
		super(BuiltInRegistries.CREATIVE_MODE_TAB);
		this.register("tab_main", () -> MAIN);
	}

	private static void fillTab(CreativeModeTab tab, CreativeModeTab.Output output) {
		BuiltInRegistries.ITEM.forEach(item -> {
			if (item instanceof ICreativeTabMember member && member.getCreativeTab() == tab) {
				member.getCreativeTabStacks().forEach(output::accept);
			}
		});
	}

	/** Port: forces class loading, which registers the tabs. */
	public static void init() {
		// NO-OP
	}
}
