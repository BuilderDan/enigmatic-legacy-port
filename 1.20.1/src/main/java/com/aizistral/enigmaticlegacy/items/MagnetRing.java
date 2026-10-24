package com.aizistral.enigmaticlegacy.items;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncMagnetEffects;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;


public class MagnetRing extends ItemBaseCurio {
	public static final String disabledMagnetTag = "DisabledMagnetEffects";
	public static Omniconfig.IntParameter range;
	public static Omniconfig.BooleanParameter invertShift;
	public static Omniconfig.BooleanParameter inventoryButtonEnabled;
	public static Omniconfig.IntParameter buttonOffsetX;
	public static Omniconfig.IntParameter buttonOffsetY;
	public static Omniconfig.IntParameter buttonOffsetXCreative;
	public static Omniconfig.IntParameter buttonOffsetYCreative;

	@SubscribeConfig(receiveClient = true)
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("MagnetRing");

		if (builder.config.getSidedType() != Configuration.SidedConfigType.CLIENT) {
			range = builder.comment("The radius in which Magnetic Ring will attract items.")
					.min(1)
					.max(256)
					.getInt("Range", 8);

			invertShift = builder.comment("Inverts the Shift behaviour of Magnetic Ring and Dislocation Ring.")
					.getBoolean("InvertShift", false);
		} else {
			inventoryButtonEnabled = builder
					.comment("Whether or not button for toggling magnet effects should be added to inventory GUI when player has Ring of Ender equipped.")
					.getBoolean("ButtonEnabled", true);

			buttonOffsetX = builder
					.comment("Allows to set offset for Magnet Effects button on X axis.")
					.minMax(32768)
					.getInt("ButtonOffsetX", 0);

			buttonOffsetY = builder
					.comment("Allows to set offset for Magnet Effects button on Y axis.")
					.minMax(32768)
					.getInt("ButtonOffsetY", 0);

			buttonOffsetXCreative = builder
					.comment("Allows to set offset for Magnet Effects button on X axis, for creative inventory specifically.")
					.minMax(32768)
					.getInt("ButtonOffsetXCreative", 0);

			buttonOffsetYCreative = builder
					.comment("Allows to set offset for Magnet Effects button on Y axis, for creative inventory specifically.")
					.minMax(32768)
					.getInt("ButtonOffsetYCreative", 0);

		}

		builder.popPrefix();
	}

	public MagnetRing() {
		this(ItemBaseCurio.getDefaultProperties().rarity(Rarity.RARE));
	}

	public MagnetRing(Properties properties) {
		super(properties);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.magnetRing1", ChatFormatting.GOLD, range.getValue());
			ItemLoreHelper.addLocalizedString(list, invertShift.getValue() ? "tooltip.enigmaticlegacy.magnetRing2_alt" : "tooltip.enigmaticlegacy.magnetRing2");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public void curioTick(SlotContext context, ItemStack stack) {
		LivingEntity living = context.entity();

		if ((invertShift.getValue() ? !living.isShiftKeyDown() : living.isShiftKeyDown()) || !(living instanceof Player))
			return;

		if (this.hasMagnetEffectsDisabled((Player) living))
			return;

		double x = living.getX();
		double y = living.getY() + 0.75;
		double z = living.getZ();

		List<ItemEntity> items = living.level().getEntitiesOfClass(ItemEntity.class, new AABB(x - range.getValue(), y - range.getValue(), z - range.getValue(), x + range.getValue(), y + range.getValue(), z + range.getValue()));
		int pulled = 0;
		for (ItemEntity item : items)
			if (this.canPullItem(item)) {
				if (pulled > 200) {
					break;
				}

				if (!SuperpositionHandler.canPickStack((Player) living, item.getItem())) {
					continue;
				}

				SuperpositionHandler.setEntityMotionFromVector(item, new Vector3(x, y, z), 0.45F);
				item.setNoPickUpDelay();

				//for (int counter = 0; counter <= 2; counter++)
				//	living.world.addParticle(ParticleTypes.WITCH, item.getPosX(), item.getPosY() - item.getYOffset() + item.getHeight() / 2, item.getPosZ(), (Math.random() - 0.5D) * 0.1D, (Math.random() - 0.5D) * 0.1D, (Math.random() - 0.5D) * 0.1D);
				pulled++;
			}

	}

	@Override
	public boolean canEquip(SlotContext context, ItemStack stack) {
		return super.canEquip(context, stack) && !SuperpositionHandler.hasCurio(context.entity(), EnigmaticItems.SUPER_MAGNET_RING);
	}

	/**
	 * Port: the client's own copy of the flag, set by {@link PacketSyncMagnetEffects}. Upstream
	 * kept it in {@code TransientPlayerData}, which is not ported.
	 */
	public static boolean clientMagnetEffectsDisabled = false;

	/**
	 * Port: upstream read {@code TransientPlayerData}, which saves to the same persistent tag.
	 * The server reads the player's persistent NBT; the client reads the synced copy.
	 */
	public boolean hasMagnetEffectsDisabled(Player player) {
		if (player.level().isClientSide)
			return clientMagnetEffectsDisabled;

		return SuperpositionHandler.getPersistentBoolean(player, disabledMagnetTag, false);
	}

	/** Port-only: flips the flag (inventory magnet button) and syncs it to its player. */
	public static void toggleMagnetEffects(ServerPlayer player) {
		boolean disabled = !SuperpositionHandler.getPersistentBoolean(player, disabledMagnetTag, false);
		SuperpositionHandler.setPersistentBoolean(player, disabledMagnetTag, disabled);
		syncMagnetEffects(player);
	}

	/** Port-only: sends the flag to its player (on change and on join). */
	public static void syncMagnetEffects(ServerPlayer player) {
		Services.NETWORK.sendToPlayer(player, new PacketSyncMagnetEffects(
				SuperpositionHandler.getPersistentBoolean(player, disabledMagnetTag, false)));
	}

	protected boolean canPullItem(ItemEntity item) {
		// Port: upstream also skipped items whose Forge persistent data had "PreventRemoteMovement"
		// (a cross-mod convention). Entities have no persistent-data map on Fabric, so that check is dropped.
		ItemStack stack = item.getItem();
		if (!item.isAlive() || stack.isEmpty())
			return false;

		return true;
	}

}
