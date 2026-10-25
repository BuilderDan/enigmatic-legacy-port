package com.aizistral.enigmaticlegacy.helpers;

import com.aizistral.enigmaticlegacy.platform.Services;

/**
 * Port: Patchouli has no 26.3 build yet, so every Patchouli call is guarded by this flag.
 * Kept in its own class so reading it never touches Patchouli or item classes.
 */
public final class PatchouliState {
	public static final boolean LOADED = Services.PLATFORM.isModLoaded("patchouli");

	private PatchouliState() {
	}
}
