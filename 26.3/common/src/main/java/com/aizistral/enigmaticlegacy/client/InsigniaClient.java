package com.aizistral.enigmaticlegacy.client;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Port: the client parts of the Charming Insignia. Holds upstream's
 * {@code SuperpositionHandler.renderInsigniaNameplate} and the {@code RenderNameTagEvent}
 * handler, moved here so the common handler does not touch client-only classes.
 */
public class InsigniaClient {

	/**
	 * Port: upstream's {@code EnigmaticEventHandler.renderNameplate}, which sets the
	 * RenderNameTagEvent result to ALLOW. Called from MixinLivingEntityRenderer.
	 */
	public static boolean shouldShowOwnName(Entity entity) {
		if (entity == Minecraft.getInstance().player) {
			Player player = Minecraft.getInstance().player;
			ItemStack insignia = SuperpositionHandler.getCurioStack(player, EnigmaticItems.INSIGNIA);

			if (insignia != null && ItemNBTHelper.getBoolean(insignia, "tagDisplayEnabled", true))
				return true;
		}

		return false;
	}

	/** Port: render-state field holding the small "(TrueName)" line; added by MixinEntityRenderState. */
	public interface TrueNameState {
		@Nullable Component enigmaticlegacy$getTrueName();

		void enigmaticlegacy$setTrueName(@Nullable Component name);
	}

	/**
	 * Port: upstream's {@code renderInsigniaNameplate}, split for 26.3. Runs after vanilla picked
	 * the name tag (so only when vanilla would show one) and swaps in the Insignia's custom name.
	 */
	public static void extractInsigniaNameplate(Entity entity, EntityRenderState state) {
		TrueNameState extra = (TrueNameState) state;
		extra.enigmaticlegacy$setTrueName(null);

		if (state.nameTag == null || !(entity instanceof Player player))
			return;

		ItemStack insignia = SuperpositionHandler.getCurioStack(player, EnigmaticItems.INSIGNIA);

		if (insignia == null || insignia.isEmpty())
			return;

		Optional<Component> customName = EnigmaticItems.INSIGNIA.getCustomName(insignia);

		if (customName.isEmpty())
			return;

		Minecraft mc = Minecraft.getInstance();
		Component name = customName.get();
		boolean seeTrueName = entity != mc.player && EnigmaticItems.INSIGNIA.canSeeTrueName(mc.player);

		if (mc.font.width(name) > 0) {
			state.nameTag = name;

			if (seeTrueName) {
				extra.enigmaticlegacy$setTrueName(Component.literal("(" + entity.getDisplayName().getString() + ")"));
			}
		} else {
			state.nameTag = seeTrueName ? entity.getDisplayName() : null;
		}
	}

	/** Port: the 0.4-scale true-name line under the custom name, from upstream's renderInsigniaNameplate. */
	public static void submitTrueName(EntityRenderState state, PoseStack stack, SubmitNodeCollector collector,
			CameraRenderState camera, int offset) {
		Component trueName = ((TrueNameState) state).enigmaticlegacy$getTrueName();

		if (trueName == null || state.nameTag == null || state.nameTagAttachment == null)
			return;

		float scale = 0.4F;
		Vec3 at = state.nameTagAttachment;
		stack.pushPose();

		if (state.scoreText != null) {
			stack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
		}

		// submitNameTag moves to the attachment point under this pose; shift first so the scale leaves that point in place.
		stack.translate(at.x * (1 - scale), (at.y + 0.5) * (1 - scale), at.z * (1 - scale));
		stack.scale(scale, scale, scale);
		collector.submitNameTag(stack, at, (int) (offset / scale) + (int) (10 / scale), trueName, !state.isDiscrete,
				state.lightCoords, camera);
		stack.popPose();
	}

}
