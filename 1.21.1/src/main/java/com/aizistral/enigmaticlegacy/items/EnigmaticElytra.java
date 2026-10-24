package com.aizistral.enigmaticlegacy.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.objects.ElytraBoostData;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/**
 * Port: Forge's canElytraFly/elytraFlightTick and Caelus' flight attribute are replaced by
 * Fabric's {@code EntityElytraEvents.CUSTOM} (registered in EnigmaticLegacyFabric).
 * Vanilla {@link Equipable} supplies the chest slot and the equip sound.
 */
public class EnigmaticElytra extends ItemBaseCurio implements Equipable {

	/**
	 * Port: upstream calls its @OnlyIn(CLIENT) handleBoosting directly. Here the client
	 * entrypoint sets this to {@code EnigmaticElytraClient::handleBoosting}, so common code
	 * never loads client classes.
	 */
	public static Consumer<Player> clientBoostHandler = player -> {};

	public EnigmaticElytra() {
		super(ItemBaseCurio.getDefaultProperties().durability(5000).fireResistant().rarity(Rarity.EPIC));
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.CHEST;
	}

	@Override
	public Holder<SoundEvent> getEquipSound() {
		return SoundEvents.ARMOR_EQUIP_ELYTRA;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public void curioTick(SlotContext context, ItemStack stack) {
		if (context.entity() instanceof Player player && player.level().isClientSide) {
			clientBoostHandler.accept(player);
		}

		LivingEntity livingEntity = context.entity();
		int ticks = livingEntity.getFallFlyingTicks();

		if (ticks > 0 && livingEntity.isFallFlying()) {
			this.elytraFlightTick(stack, livingEntity, ticks);
		}
	}

	public static boolean boostPlayer(Player player) {
		if (player.isFallFlying()) {
			Vec3 vec31 = player.getLookAngle();
			Vec3 vec32 = player.getDeltaMovement();
			player.setDeltaMovement(vec32.add(vec31.x * 0.1D + (vec31.x * 1.5D - vec32.x) * 0.5D, vec31.y * 0.1D + (vec31.y * 1.5D - vec32.y) * 0.5D, vec31.z * 0.1D + (vec31.z * 1.5D - vec32.z) * 0.5D));
			return true;
		} else
			return false;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		return attributes;
	}

	@Override
	public boolean isValidRepairItem(ItemStack repairedItem, ItemStack material) {
		return material.is(EnigmaticItems.ETHERIUM_INGOT);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(itemstack);
		ItemStack itemstack1 = player.getItemBySlot(equipmentslot);

		if (itemstack1.isEmpty()) {
			player.setItemSlot(equipmentslot, itemstack.copy());

			if (!level.isClientSide()) {
				player.awardStat(Stats.ITEM_USED.get(this));
			}

			itemstack.setCount(0);
			return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
		} else
			return InteractionResultHolder.fail(itemstack);
	}

	public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
		return entity instanceof Player && ElytraItem.isFlyEnabled(stack);
	}

	public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
		if (entity instanceof Player player) {
			if (!entity.level().isClientSide) {
				int nextFlightTick = flightTicks + 1;

				if (nextFlightTick % 10 == 0) {
					if (nextFlightTick % 20 == 0) {
						stack.hurtAndBreak(1, entity, EquipmentSlot.CHEST);
					}

					entity.gameEvent(GameEvent.ELYTRA_GLIDE);
				}
			} else {
				clientBoostHandler.accept(player);
			}

			return true;
		} else
			return false;
	}

	/**
	 * Server half of upstream's onPlayerTick (Forge PlayerTickEvent), called from
	 * EnigmaticEventHandler.onPlayerTick. The Caelus attribute part is not needed on Fabric.
	 */
	public static void onServerPlayerTick(ServerPlayer player) {
		if (ElytraBoostData.isBoosting(player)) {
			boostPlayer(player);
			ItemStack stack = SuperpositionHandler.getEnigmaticElytra(player);

			if (stack != null && stack.is(EnigmaticItems.ENIGMATIC_ELYTRA)) {
				int flightTicks = player.getFallFlyingTicks();
				int nextFlightTick = flightTicks + 1;

				if (nextFlightTick % 5 == 0) {
					stack.hurtAndBreak(1, player, EquipmentSlot.CHEST);
				}
			}
		}
	}

}
