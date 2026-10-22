package com.aizistral.enigmaticlegacy.api.generic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.aizistral.enigmaticlegacy.registries.AbstractRegistry;

/**
 * Port-only. Stands in for Forge's ObjectHolder: {@link AbstractRegistry} fills the
 * annotated static field when the object with this id is registered.
 */
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface ObjectHolder {
	String value();

	String registryName();
}
