package com.aizistral.enigmaticlegacy.platform.accessory;

import net.minecraft.world.entity.LivingEntity;

/**
 * Port-only stand-in for Curios' SlotContext, so ported item code keeps its shape.
 * The identifier is the upstream Curios slot id ("charm", "ring", "back", "spellstone",
 * "scroll"); slots added by other mods keep their own slot name.
 */
public record SlotContext(String identifier, LivingEntity entity, int index, boolean cosmetic, boolean visible) {
}
