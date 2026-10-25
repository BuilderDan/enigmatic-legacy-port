package com.aizistral.enigmaticlegacy.client;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet;
import com.aizistral.enigmaticlegacy.items.EnigmaticEye;
import com.aizistral.enigmaticlegacy.items.SoulCompass;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/**
 * Port: upstream registers these as ItemProperties in FMLClientSetupEvent. 26.3 item models read
 * numeric properties through range_dispatch item definitions (assets/enigmaticlegacy/items), and
 * tints (upstream's item color handler) through tint sources.
 */
public final class EnigmaticItemModelProperties {

	private EnigmaticItemModelProperties() {
	}

	public static void register() {
		register("amulet_color", AmuletColor.CODEC);
		register("eye_activated", EyeActivated.CODEC);
		register("infinitum_open", InfinitumOpen.CODEC);
		register("soul_compass", SoulCompassAngle.CODEC);
		ItemTintSources.ID_MAPPER.put(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "potion"), PotionTint.CODEC);
	}

	private static void register(String name, MapCodec<? extends RangeSelectItemModelProperty> codec) {
		RangeSelectItemModelProperties.ID_MAPPER.put(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, name), codec);
	}

	/** Upstream EnigmaticAmulet.registerVariants: the assigned color. */
	public enum AmuletColor implements RangeSelectItemModelProperty {
		INSTANCE;

		public static final MapCodec<AmuletColor> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return ItemNBTHelper.getFloat(stack, EnigmaticAmulet.amuletColorTag, 0F);
		}

		@Override
		public MapCodec<AmuletColor> type() {
			return CODEC;
		}
	}

	/** Upstream EnigmaticEye.registerVariants: dormant, activation animation, active. */
	public enum EyeActivated implements RangeSelectItemModelProperty {
		INSTANCE;

		public static final MapCodec<EyeActivated> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			if (stack.getItem() instanceof EnigmaticEye eye && !eye.isDormant(stack))
				return 1F;

			int animTicks = ItemNBTHelper.getInt(stack, "ActivationAnimation", -1);

			if (animTicks > -1)
				return animTicks > 2 ? 0.4F : 0.8F;

			return 0F;
		}

		@Override
		public MapCodec<EyeActivated> type() {
			return CODEC;
		}
	}

	/** Upstream ClientProxy: The Infinitum opening animation. */
	public enum InfinitumOpen implements RangeSelectItemModelProperty {
		INSTANCE;

		public static final MapCodec<InfinitumOpen> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return InfinitumClient.getOpenProperty(stack, level, owner != null ? owner.asLivingEntity() : null, seed);
		}

		@Override
		public MapCodec<InfinitumOpen> type() {
			return CODEC;
		}
	}

	/** Upstream SoulCompass.registerVariants: the needle angle. */
	public enum SoulCompassAngle implements RangeSelectItemModelProperty {
		INSTANCE;

		public static final MapCodec<SoulCompassAngle> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return stack.getItem() instanceof SoulCompass compass ? compass.getNeedleAngle(stack, level, owner, seed) : 0F;
		}

		@Override
		public MapCodec<SoulCompassAngle> type() {
			return CODEC;
		}
	}

	/** Upstream ClientProxy item color handler: the potion color for the ultimate and common potions. */
	public enum PotionTint implements ItemTintSource {
		INSTANCE;

		public static final MapCodec<PotionTint> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(PotionHelper.getColor(stack));
		}

		@Override
		public MapCodec<PotionTint> type() {
			return CODEC;
		}
	}

}
