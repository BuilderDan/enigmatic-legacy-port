package com.aizistral.enigmaticlegacy.api.materials;

import java.util.Map;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Port: ArmorMaterial is an unregistered record in 26.3, so this is no longer an enum. Its
 * durability field keeps upstream's factor of 132, and the repair item is the
 * enigmaticlegacy:repairs/etherium item tag instead of the config's Ingredient. The armor
 * texture is the equipment asset assets/enigmaticlegacy/equipment/etherium.json.
 */
public final class EnigmaticArmorMaterials {
	public static final int ETHERIUM_DURABILITY_FACTOR = 132;

	public static final ArmorMaterial ETHERIUM = new ArmorMaterial(ETHERIUM_DURABILITY_FACTOR, Map.of(
			ArmorType.BOOTS, 4,
			ArmorType.LEGGINGS, 7,
			ArmorType.CHESTPLATE, 9,
			ArmorType.HELMET, 4,
			ArmorType.BODY, 9), 24, SoundEvents.ARMOR_EQUIP_IRON, 4F, 0F, EnigmaticMaterials.REPAIRS_ETHERIUM,
			ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "etherium")));

	private EnigmaticArmorMaterials() {
		throw new IllegalStateException("Can't touch this");
	}

}
