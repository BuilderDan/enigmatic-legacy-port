package com.aizistral.enigmaticlegacy.items;

import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
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
 * 26.3: the GLIDER and EQUIPPABLE components (chest slot, equip sound, wings asset) replace
 * ElytraItem and Equipable; the Etherium repair tag replaces isValidRepairItem.
 */
public class EnigmaticElytra extends ItemBaseCurio {

	/**
	 * Port: upstream calls its @OnlyIn(CLIENT) handleBoosting directly. Here the client
	 * entrypoint sets this to {@code EnigmaticElytraClient::handleBoosting}, so common code
	 * never loads client classes.
	 */
	public static Consumer<Player> clientBoostHandler = player -> {};

	public EnigmaticElytra() {
		super(ItemBaseCurio.getDefaultProperties().durability(5000).fireResistant().rarity(Rarity.EPIC)
				.repairable(EnigmaticMaterials.REPAIRS_ETHERIUM)
				.component(DataComponents.GLIDER, Unit.INSTANCE)
				.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
						.setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
						.setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "enigmatic_elytra")))
						.setDamageOnHurt(false)
						.build()));
		DispenserBlock.registerBehavior(this, EquipmentDispenseItemBehavior.INSTANCE);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.enigmaticElytra3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public void curioTick(SlotContext context, ItemStack stack) {
		if (context.entity() instanceof Player player && player.level().isClientSide()) {
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
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, Identifier id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		return attributes;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(itemstack);
		ItemStack itemstack1 = player.getItemBySlot(equipmentslot);

		if (itemstack1.isEmpty()) {
			player.setItemSlot(equipmentslot, itemstack.copy());

			if (!level.isClientSide()) {
				player.awardStat(Stats.ITEM_USED.get(this));
			}

			itemstack.setCount(0);
			return InteractionResult.SUCCESS;
		} else
			return InteractionResult.FAIL;
	}

	public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
		return entity instanceof Player && !stack.nextDamageWillBreak();
	}

	public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
		if (entity instanceof Player player) {
			if (!entity.level().isClientSide()) {
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
