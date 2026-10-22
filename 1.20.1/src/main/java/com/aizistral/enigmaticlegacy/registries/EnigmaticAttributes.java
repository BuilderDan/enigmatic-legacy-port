package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Fabric has no block reach attribute (upstream uses Forge's BLOCK_REACH), so the port
 * adds its own. Players get it through {@code MixinPlayer}; its value is added to the client's pick
 * range ({@code MixinMultiPlayerGameMode}) and to the server's distance checks for breaking and
 * using blocks ({@code MixinServerPlayerGameMode}, {@code MixinServerGamePacketListenerImpl}).
 * Created eagerly so it exists before vanilla builds the player's attributes.
 * <p>
 * GRAVITY and SWIM_SPEED stand in for Forge's ENTITY_GRAVITY and SWIM_SPEED (used by the Enigmatic
 * Amulet). Both are multipliers (base 1.0) applied in {@code MixinLivingEntity.travel}.
 */
public class EnigmaticAttributes {
	public static final Attribute BLOCK_REACH = new RangedAttribute("attribute.name.enigmaticlegacy.block_reach",
			0.0D, 0.0D, 1024.0D).setSyncable(true);
	public static final Attribute GRAVITY = new RangedAttribute("attribute.name.enigmaticlegacy.gravity",
			1.0D, 0.0D, 16.0D).setSyncable(true);
	public static final Attribute SWIM_SPEED = new RangedAttribute("attribute.name.enigmaticlegacy.swim_speed",
			1.0D, 0.0D, 1024.0D).setSyncable(true);

	public static void init() {
		Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(EnigmaticLegacy.MODID, "block_reach"), BLOCK_REACH);
		Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(EnigmaticLegacy.MODID, "gravity"), GRAVITY);
		Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(EnigmaticLegacy.MODID, "swim_speed"), SWIM_SPEED);
	}

	/** Value of a multiplier attribute (GRAVITY, SWIM_SPEED), 1 if the entity lacks it. */
	public static double getMultiplier(LivingEntity entity, Attribute attribute) {
		AttributeInstance instance = entity.getAttribute(attribute);
		return instance != null ? instance.getValue() : 1.0D;
	}

	/** Extra block reach of this player, 0 if none. */
	public static double getBlockReach(Player player) {
		AttributeInstance instance = player.getAttribute(BLOCK_REACH);
		return instance != null ? instance.getValue() : 0.0D;
	}

	/** Widens a squared vanilla interaction distance by the player's extra block reach. */
	public static double widenDistanceSqr(double maxDistanceSqr, Player player) {
		double bonus = getBlockReach(player);
		if (bonus <= 0)
			return maxDistanceSqr;

		double distance = Math.sqrt(maxDistanceSqr) + bonus;
		return distance * distance;
	}
}
