package com.aizistral.enigmaticlegacy.mixin.patchouli;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;


/**
 * Port addition: Patchouli is optional on 26.3 (no 26.3 build exists yet), so its GUI mixins
 * only apply when the mod is present.
 */
public class PatchouliMixinPlugin implements IMixinConfigPlugin {
	private static final boolean LOADED = PatchouliMixinPlugin.class.getClassLoader()
			.getResource("vazkii/patchouli/api/PatchouliAPI.class") != null;

	@Override public void onLoad(String mixinPackage) {}
	@Override public String getRefMapperConfig() { return null; }
	@Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return LOADED; }
	@Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override public List<String> getMixins() { return null; }
	@Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
