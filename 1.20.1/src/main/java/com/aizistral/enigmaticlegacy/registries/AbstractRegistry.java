package com.aizistral.enigmaticlegacy.registries;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Port: Forge's DeferredRegister and RegisterEvent are replaced by direct vanilla
 * registration. Each object is registered as soon as {@link #register} is called, and the
 * subclass's static field marked with a matching {@link ObjectHolder} is filled right away.
 * Holder fields must be static and non-final, with no initializer.
 */
public abstract class AbstractRegistry<T> {
	protected static final String MODID = EnigmaticLegacy.MODID;
	private final Registry<T> registry;

	protected AbstractRegistry(Registry<T> registry) {
		this.registry = registry;
	}

	protected void register(String name, Supplier<T> supplier) {
		T value = Registry.register(this.registry, new ResourceLocation(MODID, name), supplier.get());
		this.fillHolder(MODID + ":" + name, value);

		// Port: Curios finds ICurioItem by interface; Trinkets needs each item registered.
		if (value instanceof Item item && value instanceof IAccessoryItem) {
			Services.ACCESSORIES.registerAccessory(item);
		}
	}

	private void fillHolder(String id, T value) {
		for (Field field : this.getClass().getDeclaredFields()) {
			ObjectHolder holder = field.getAnnotation(ObjectHolder.class);

			if (holder == null || !holder.value().equals(id)) {
				continue;
			}

			if (!Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
				throw new IllegalStateException("Object holder " + field + " must be static and non-final");

			try {
				field.setAccessible(true);
				field.set(null, value);
			} catch (IllegalAccessException ex) {
				throw new IllegalStateException("Couldn't fill object holder " + field, ex);
			}

			return;
		}
	}
}
