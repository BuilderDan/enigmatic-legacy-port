package com.aizistral.enigmaticlegacy.fabric.platform;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.resources.ResourceLocation;

/**
 * Port-only. Cardinal Components entry point; replaces upstream's Forge capabilities.
 */
public class EnigmaticComponents implements EntityComponentInitializer {
	public static final ComponentKey<PlaytimeCounterComponent> PLAYTIME_COUNTER = ComponentRegistry.getOrCreate(
			new ResourceLocation(EnigmaticLegacy.MODID, "playtime_counter"), PlaytimeCounterComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(PLAYTIME_COUNTER, PlaytimeCounterComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
}
