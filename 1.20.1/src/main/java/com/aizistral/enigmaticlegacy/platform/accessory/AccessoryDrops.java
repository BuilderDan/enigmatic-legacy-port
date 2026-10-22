package com.aizistral.enigmaticlegacy.platform.accessory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only stand-in for Curios' DropRulesEvent. Overrides are asked when an entity dies;
 * the first one that returns a rule wins over the item's own {@link IAccessoryItem#getDropRule}.
 */
public final class AccessoryDrops {
	private static final List<DropOverride> OVERRIDES = new CopyOnWriteArrayList<>();

	private AccessoryDrops() {
		// NO-OP
	}

	@FunctionalInterface
	public interface DropOverride {
		/** The rule for this stack, or null to leave it alone. */
		@Nullable
		DropRule getRule(LivingEntity entity, ItemStack stack);
	}

	public static void register(DropOverride override) {
		OVERRIDES.add(override);
	}

	@Nullable
	public static DropRule getOverride(LivingEntity entity, ItemStack stack) {
		for (DropOverride override : OVERRIDES) {
			DropRule rule = override.getRule(entity, stack);

			if (rule != null)
				return rule;
		}

		return null;
	}
}
