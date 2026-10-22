package com.aizistral.enigmaticlegacy.helpers;

import com.aizistral.enigmaticlegacy.objects.Vector3;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port: only calcRayTrace (used by the Ultimate Wither Skull) is ported so far.
 * The area-mining helpers come with the tools that use them.
 */
public class AOEMiningHelper {

	public static Vector3 calcRayTrace(Level worldIn, Player player, ClipContext.Fluid fluidMode, double distance) {
		float f = player.getXRot();
		float f1 = player.getYRot();
		Vec3 vector3d = player.getEyePosition(1.0F);
		float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
		float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = distance;
		Vec3 vector3d1 = vector3d.add(f6 * d0, f5 * d0, f7 * d0);
		HitResult result = worldIn.clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.OUTLINE, fluidMode, player));

		if (result.getType() == HitResult.Type.BLOCK)
			return new Vector3(result.getLocation());
		else {
			Vector3 vec = new Vector3(player.getLookAngle()).multiply(64F).add(new Vector3(player.position()));
			return vec;
		}
	}

}
