package com.aizistral.enigmaticlegacy.items;

import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.IPermanentCrystal;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;

public class SoulCrystal extends ItemBase implements IPermanentCrystal {
	public Map<Player, Multimap<Holder<Attribute>, AttributeModifier>> attributeDispatcher = new WeakHashMap<>();

	public SoulCrystal() {
		super(ItemBase.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).fireResistant());
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.soulCrystal1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.soulCrystal2");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	public ItemStack createCrystalFrom(Player player) {
		int lostFragments = this.getLostCrystals(player);
		this.setLostCrystals(player, lostFragments + 1);

		return new ItemStack(this);
	}

	public boolean retrieveSoulFromCrystal(Player player, ItemStack stack) {
		int lostFragments = this.getLostCrystals(player);

		if (lostFragments > 0) {
			this.setLostCrystals(player, lostFragments - 1);

			if (!player.level().isClientSide()) {
				player.level().playSound(null, BlockPos.containing(player.position()), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
			}

			return true;
		} else
			return false;
	}

	public void setLostCrystals(Player player, int lost) {
		SuperpositionHandler.setCurrentWorldFractured(lost >= 10);
		SuperpositionHandler.setPersistentInteger(player, "enigmaticlegacy.lostsoulfragments", lost);
		this.updatePlayerSoulMap(player);
	}

	public int getLostCrystals(Player player) {
		return SuperpositionHandler.getPersistentInteger(player, "enigmaticlegacy.lostsoulfragments", 0);
	}

	public Multimap<Holder<Attribute>, AttributeModifier> getOrCreateSoulMap(Player player) {
		if (this.attributeDispatcher.containsKey(player))
			return this.attributeDispatcher.get(player);
		else {
			Multimap<Holder<Attribute>, AttributeModifier> playerAttributes = HashMultimap.create();
			this.attributeDispatcher.put(player, playerAttributes);
			return playerAttributes;
		}
	}

	public void applyPlayerSoulMap(Player player) {
		Multimap<Holder<Attribute>, AttributeModifier> soulMap = this.getOrCreateSoulMap(player);
		AttributeMap attributeManager = player.getAttributes();
		attributeManager.addTransientAttributeModifiers(soulMap);
	}

	public void updatePlayerSoulMap(Player player) {
		Multimap<Holder<Attribute>, AttributeModifier> soulMap = this.getOrCreateSoulMap(player);
		AttributeMap attributeManager = player.getAttributes();

		// Removes former attributes
		attributeManager.removeAttributeModifiers(soulMap);

		soulMap.clear();

		int lostFragments = SuperpositionHandler.getPersistentInteger(player, "enigmaticlegacy.lostsoulfragments", 0);

		if (lostFragments > 0) {
			soulMap.put(Attributes.MAX_HEALTH, new AttributeModifier(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "soul_crystal_lost_health"), -0.1F * lostFragments, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}

		// Applies new attributes
		attributeManager.addTransientAttributeModifiers(soulMap);

		this.attributeDispatcher.put(player, soulMap);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);

		if (this.retrieveSoulFromCrystal(player, stack)) {
			Vector3 playerCenter = Vector3.fromEntityCenter(player);
			if (!player.level().isClientSide()) {
				// Port: Forge PacketDistributor.NEAR -> Services.NETWORK.sendToNear with TargetPoint.
				Services.NETWORK.sendToNear(new TargetPoint(playerCenter.x, playerCenter.y, playerCenter.z, 64, player.level().dimension()), new PacketRecallParticles(playerCenter.x, playerCenter.y, playerCenter.z, 48, false));
			}

			player.swing(hand, SwingAnimation.DEFAULT, false);
			stack.setCount(0);
			return InteractionResult.SUCCESS;
		} else
			return InteractionResult.PASS;
	}

}
