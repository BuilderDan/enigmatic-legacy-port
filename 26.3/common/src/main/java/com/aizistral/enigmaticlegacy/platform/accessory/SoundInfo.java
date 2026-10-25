package com.aizistral.enigmaticlegacy.platform.accessory;

import net.minecraft.sounds.SoundEvent;

/**
 * Port-only stand-in for Curios' ICurio.SoundInfo.
 */
public record SoundInfo(SoundEvent soundEvent, float volume, float pitch) {
}
