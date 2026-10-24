package com.aizistral.enigmaticlegacy.api.materials;

import java.util.EnumMap;
import java.util.List;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

/**
 * Port: the repair material is read from EtheriumConfigHandler.instance() instead of a
 * config set by the mod constructor.
 * <p>
 * Port: ArmorMaterial is a registered record in 1.21, so this is no longer an enum. Durability
 * moved to the item properties; ETHERIUM_DURABILITY_FACTOR keeps upstream's factor of 132.
 */
public final class EnigmaticArmorMaterials {
	public static final int ETHERIUM_DURABILITY_FACTOR = 132;

	public static final Holder<ArmorMaterial> ETHERIUM = register("etherium", Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
		map.put(ArmorItem.Type.BOOTS, 4);
		map.put(ArmorItem.Type.LEGGINGS, 7);
		map.put(ArmorItem.Type.CHESTPLATE, 9);
		map.put(ArmorItem.Type.HELMET, 4);
		map.put(ArmorItem.Type.BODY, 9);
	}), 24, 4F, 0F);

	private EnigmaticArmorMaterials() {
		throw new IllegalStateException("Can't touch this");
	}

	private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense, int enchantability, float toughness, float knockbackResistance) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, name);
		ArmorMaterial material = new ArmorMaterial(defense, enchantability, SoundEvents.ARMOR_EQUIP_IRON,
				() -> EtheriumConfigHandler.instance().getRepairMaterial(), List.of(new ArmorMaterial.Layer(id)), toughness, knockbackResistance);
		return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, id, material);
	}

}
