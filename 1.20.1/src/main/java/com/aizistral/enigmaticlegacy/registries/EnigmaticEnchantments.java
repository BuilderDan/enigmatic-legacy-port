package com.aizistral.enigmaticlegacy.registries;

import static net.minecraft.world.entity.EquipmentSlot.CHEST;
import static net.minecraft.world.entity.EquipmentSlot.FEET;
import static net.minecraft.world.entity.EquipmentSlot.HEAD;
import static net.minecraft.world.entity.EquipmentSlot.LEGS;
import static net.minecraft.world.entity.EquipmentSlot.MAINHAND;
import static net.minecraft.world.entity.EquipmentSlot.OFFHAND;

import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.enchantments.CeaselessEnchantment;
import com.aizistral.enigmaticlegacy.enchantments.EternalBindingCurse;
import com.aizistral.enigmaticlegacy.enchantments.NemesisCurse;
import com.aizistral.enigmaticlegacy.enchantments.SharpshooterEnchantment;
import com.aizistral.enigmaticlegacy.enchantments.SlayerEnchantment;
import com.aizistral.enigmaticlegacy.enchantments.SorrowCurse;
import com.aizistral.enigmaticlegacy.enchantments.TorrentEnchantment;
import com.aizistral.enigmaticlegacy.enchantments.WrathEnchantment;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Upstream registry. Port: BuiltInRegistries instead of ForgeRegistries; the fields are not
 * final, since the port's ObjectHolder handling assigns them after registration.
 */
public class EnigmaticEnchantments extends AbstractRegistry<Enchantment> {
	private static final EnigmaticEnchantments INSTANCE = new EnigmaticEnchantments();

	@ConfigurableItem("Sharpshooter Enchantment")
	@ObjectHolder(value = MODID + ":sharpshooter", registryName = "enchantment")
	public static SharpshooterEnchantment SHARPSHOOTER;

	@ConfigurableItem("Ceaseless Enchantment")
	@ObjectHolder(value = MODID + ":ceaseless", registryName = "enchantment")
	public static CeaselessEnchantment CEASELESS;

	@ConfigurableItem("Torrent Enchantment")
	@ObjectHolder(value = MODID + ":torrent", registryName = "enchantment")
	public static TorrentEnchantment TORRENT;

	@ConfigurableItem("Wrath Enchantment")
	@ObjectHolder(value = MODID + ":wrath", registryName = "enchantment")
	public static WrathEnchantment WRATH;

	@ConfigurableItem("Slayer Enchantment")
	@ObjectHolder(value = MODID + ":slayer", registryName = "enchantment")
	public static SlayerEnchantment SLAYER;

	@ConfigurableItem("Curse of Nemesis")
	@ObjectHolder(value = MODID + ":nemesis", registryName = "enchantment")
	public static NemesisCurse NEMESIS;

	@ConfigurableItem("Curse of Eternal Binding")
	@ObjectHolder(value = MODID + ":eternal_binding", registryName = "enchantment")
	public static EternalBindingCurse ETERNAL_BINDING;

	@ConfigurableItem("Curse of Sorrow")
	@ObjectHolder(value = MODID + ":sorrow", registryName = "enchantment")
	public static SorrowCurse SORROW;

	private EnigmaticEnchantments() {
		super(BuiltInRegistries.ENCHANTMENT);

		this.register("sharpshooter", () -> new SharpshooterEnchantment(MAINHAND, OFFHAND));
		this.register("ceaseless", () -> new CeaselessEnchantment(MAINHAND, OFFHAND));
		this.register("nemesis", () -> new NemesisCurse(MAINHAND));
		this.register("torrent", () -> new TorrentEnchantment(MAINHAND));
		this.register("wrath", () -> new WrathEnchantment(MAINHAND));
		this.register("slayer", () -> new SlayerEnchantment(MAINHAND));
		this.register("eternal_binding", () -> new EternalBindingCurse(HEAD, CHEST, LEGS, FEET));
		this.register("sorrow", () -> new SorrowCurse(HEAD, CHEST, LEGS, FEET));
	}

	/** Port: forces class loading, which registers the enchantments. Call after the config loads. */
	public static void init() {
		// NO-OP
	}
}
