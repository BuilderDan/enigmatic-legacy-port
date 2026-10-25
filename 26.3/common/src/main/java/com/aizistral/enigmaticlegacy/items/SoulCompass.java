package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.google.common.base.Objects;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;

/**
 * Upstream item. Port: the client-only members swap @OnlyIn for @Environment(EnvType.CLIENT); they only run on
 * the client (registerVariants from EnigmaticLegacyFabricClient, setNearestCrystal from
 * PacketUpdateCompass).
 */
// TODO Make wayfinder make soul crystals glow, also give it the mode to point to personal crystals only
public class SoulCompass extends ItemBase implements ICursed {
	private final CompassWobble wobble = new CompassWobble(0.1);
	private final CompassWobble wobbleRandom = new CompassWobble(0.6); // 0.6 to make it GO FASTA
	@Environment(EnvType.CLIENT)
	@Nullable
	private BlockPos nearestCrystal;

	public SoulCompass() {
		super(getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).fireResistant());
	}

	@Environment(EnvType.CLIENT)
	public void setNearestCrystal(BlockPos nearestCrystal) {
		if (!Objects.equal(this.nearestCrystal, nearestCrystal)) {
			if (nearestCrystal != null && this.nearestCrystal != null) {
				this.wobble.rotation = 1.0;
				this.wobble.deltaRotation = 0.3;
			} else {
				this.wobble.rotation = this.wobbleRandom.rotation;
				this.wobble.deltaRotation = this.wobbleRandom.deltaRotation;
			}

			this.nearestCrystal = nearestCrystal;
		}
	}

	@Environment(EnvType.CLIENT)
	public float getAngle(BlockPos one, BlockPos two) {
		float angle = (float) Math.toDegrees(Math.atan2(one.getZ() - two.getZ(), one.getX() - two.getX()));

		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.soulCompass1");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

	/**
	 * Port: upstream's "angle" item property function. 26.3 item models read it through the
	 * enigmaticlegacy:soul_compass range property (EnigmaticItemModelProperties).
	 */
	@Environment(EnvType.CLIENT)
	public float getNeedleAngle(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		if (owner == null)
			return 0.0F;

		if (level == null && owner.level() instanceof ClientLevel clientLevel) {
			level = clientLevel;
		}

		if (level == null)
			return 0.0F;

		LivingEntity living = owner.asLivingEntity();
		BlockPos target = this.nearestCrystal;
		long gameTime = level.getGameTime();

		if (target != null && !(owner.position().distanceToSqr(target.getX() + 0.5D, owner.position().y(), target.getZ() + 0.5D) < 1.0E-5F)) {
			if (!(living instanceof Player player) || !player.isLocalPlayer())
				return this.randomAngle(gameTime, seed);

			if (!SuperpositionHandler.isTheCursedOne(player)
					|| !SuperpositionHandler.hasExactStack(player, stack)
					|| level.getBiome(player.blockPosition()).is(Biomes.SOUL_SAND_VALLEY))
				return this.randomAngle(gameTime, seed);

			double bodyRotation = Mth.positiveModulo(player.getYRot() / 360.0D, 1.0D);
			double angle = this.getAngleTo(Vec3.atCenterOf(target), player) / ((float)Math.PI * 2F);

			if (this.wobble.shouldUpdate(gameTime)) {
				this.wobble.update(gameTime, 0.5D - (bodyRotation - 0.25D));
			}

			return Mth.positiveModulo((float) (angle + this.wobble.rotation), 1.0F);
		} else
			return this.randomAngle(gameTime, seed);
	}

	private float randomAngle(long gameTime, int seed) {
		if (this.wobbleRandom.shouldUpdate(gameTime)) {
			this.wobbleRandom.update(gameTime, Math.random());
		}

		double randomAngle = this.wobbleRandom.rotation + seed * 1327217883 / 2.14748365E9F;
		return Mth.positiveModulo((float)randomAngle, 1.0F);
	}

	private double getAngleTo(Vec3 pos, Entity entity) {
		return Math.atan2(pos.z() - entity.getZ(), pos.x() - entity.getX());
	}

	private static class CompassWobble {
		double needleMobility;
		double rotation;
		private double deltaRotation;
		private long lastUpdateTick;

		CompassWobble(double needleMobility) {
			this.needleMobility = needleMobility;
		}

		boolean shouldUpdate(long pGameTime) {
			return this.lastUpdateTick != pGameTime;
		}

		void update(long gameTime, double wobbleAmount) {
			this.lastUpdateTick = gameTime;
			double var = wobbleAmount - this.rotation;
			var = Mth.positiveModulo(var + 0.5D, 1.0D) - 0.5D;
			this.deltaRotation += var * this.needleMobility;
			this.deltaRotation *= 0.8D;

			this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0D);
		}
	}

}
