package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.gui.containers.LoreInscriberContainer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class EnigmaticMenus extends AbstractRegistry<MenuType<?>> {
	private static final EnigmaticMenus INSTANCE = new EnigmaticMenus();

	@ObjectHolder(value = MODID + ":lore_inscriber_container", registryName = "menu")
	public static MenuType<LoreInscriberContainer> LORE_INSCRIBER_CONTAINER;

	private EnigmaticMenus() {
		super(BuiltInRegistries.MENU);

		// Port: IForgeMenuType.create -> vanilla MenuType; the menu sends no extra data
		this.register("lore_inscriber_container", () -> new MenuType<>(LoreInscriberContainer::new, FeatureFlags.VANILLA_SET));
	}

	/** Port: forces class loading, which registers the menus. */
	public static void init() {
		// NO-OP
	}

}
