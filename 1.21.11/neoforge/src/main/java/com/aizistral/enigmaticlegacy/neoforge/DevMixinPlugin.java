package com.aizistral.enigmaticlegacy.neoforge;

import com.aizistral.enigmaticlegacy.dev.DevHooks;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Port-only: applies enigmaticlegacy.dev.mixins.json only in playtest and devcheck runs. The
 * monitor-2 window mixin also applies to every dev client started by runClient.
 */
public final class DevMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.endsWith(".DevMixinWindow"))
			return DevHooks.enabled() || Boolean.getBoolean("enigmaticlegacy.monitor2");

		return DevHooks.enabled();
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
