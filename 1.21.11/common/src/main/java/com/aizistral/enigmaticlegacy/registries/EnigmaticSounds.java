package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.client.Quote;
import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class EnigmaticSounds extends AbstractRegistry<SoundEvent> {
	private static final EnigmaticSounds INSTANCE = new EnigmaticSounds();

	@ObjectHolder(value = MODID + ":misc.hhon", registryName = "sound_event")
	public static SoundEvent CHARGED_ON;

	@ObjectHolder(value = MODID + ":misc.hhoff", registryName = "sound_event")
	public static SoundEvent CHARGED_OFF;

	@ObjectHolder(value = MODID + ":misc.shield_trigger", registryName = "sound_event")
	public static SoundEvent SHIELD_TRIGGER;

	@ObjectHolder(value = MODID + ":misc.deflect", registryName = "sound_event")
	public static SoundEvent DEFLECT;

	@ObjectHolder(value = MODID + ":misc.write", registryName = "sound_event")
	public static SoundEvent WRITE;

	@ObjectHolder(value = MODID + ":misc.learn", registryName = "sound_event")
	public static SoundEvent LEARN;

	@ObjectHolder(value = MODID + ":misc.sword_hit_reject", registryName = "sound_event")
	public static SoundEvent SWORD_HIT_REJECT;

	@ObjectHolder(value = MODID + ":misc.uneat", registryName = "sound_event")
	public static SoundEvent EAT_REVERSE;

	@ObjectHolder(value = MODID + ":misc.pan_clang", registryName = "sound_event")
	public static SoundEvent PAN_CLANG;

	@ObjectHolder(value = MODID + ":misc.pan_clang_fr", registryName = "sound_event")
	public static SoundEvent PAN_CLANG_FR;

	private EnigmaticSounds() {
		super(BuiltInRegistries.SOUND_EVENT);
		this.register("misc.hhon");
		this.register("misc.hhoff");
		this.register("misc.shield_trigger");
		this.register("misc.deflect");
		this.register("misc.write");
		this.register("misc.learn");
		this.register("misc.sword_hit_reject");
		this.register("misc.uneat");
		this.register("misc.pan_clang");
		this.register("misc.pan_clang_fr");
	}

	private void register(String name) {
		this.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MODID, name)));
	}

	public static void init() {
		// Loading the class registers the sounds; loading Quote registers the quote sounds.
		Quote.getByID(0);
	}

}
