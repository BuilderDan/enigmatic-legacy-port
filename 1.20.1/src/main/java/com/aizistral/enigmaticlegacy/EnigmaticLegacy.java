package com.aizistral.enigmaticlegacy;

import java.util.UUID;

import com.aizistral.enigmaticlegacy.objects.LoggerWrapper;

/**
 * Port: loader-neutral home of upstream's EnigmaticLegacy constants. Upstream's Forge
 * mod class is split up as it is ported; loader entry points live in their own packages.
 */
public class EnigmaticLegacy {
	public static final String MODID = "enigmaticlegacy";
	public static final LoggerWrapper LOGGER = new LoggerWrapper("Enigmatic Legacy");
	public static final UUID SOUL_OF_THE_ARCHITECT = UUID.fromString("bfa45411-874a-4ee0-b3bd-00c716059d95");
}
