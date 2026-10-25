package com.aizistral.enigmaticlegacy.neoforge.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;

/** Port-only: the dev playtest switches creative pages to open the mod's tab. */
@Mixin(CreativeModeInventoryScreen.class)
public interface AccessorCreativeModeInventoryScreen {
	@Accessor(value = "pages", remap = false)
	List<CreativeTabsScreenPage> enigmaticlegacy$getPages();
}
