package com.aizistral.enigmaticlegacy;

import com.aizistral.enigmaticlegacy.api.capabilities.PlayerPlaytimeCounter;
import com.aizistral.enigmaticlegacy.api.items.IBindable;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.enchantments.EternalBindingCurse;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.EldritchPan;
import com.aizistral.enigmaticlegacy.items.TheAcknowledgment;
import com.aizistral.enigmaticlegacy.packets.clients.*;
import com.aizistral.enigmaticlegacy.packets.server.*;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryDrops;
import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.network.PacketDirection;
import com.aizistral.enigmaticlegacy.registries.*;
import com.aizistral.enigmaticlegacy.triggers.BeheadingTrigger;
import com.aizistral.enigmaticlegacy.triggers.CursedInventoryChangedTrigger;
import com.aizistral.enigmaticlegacy.triggers.CursedRingEquippedTrigger;
import com.aizistral.enigmaticlegacy.triggers.EquipAccessoryTrigger;
import com.aizistral.enigmaticlegacy.triggers.ForbiddenFruitTrigger;
import com.aizistral.enigmaticlegacy.triggers.RevelationGainTrigger;
import com.aizistral.enigmaticlegacy.triggers.RevelationTomeBurntTrigger;
import com.aizistral.enigmaticlegacy.triggers.UseUnholyGrailTrigger;
import com.aizistral.omniconfig.packets.PacketSyncOptions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Port-only. Setup shared by the Fabric and NeoForge entry points; each loader calls these
 * in its own registration phase.
 */
public class EnigmaticSetup {

	/** Registry entries. Calls {@code Registry.register} on the built-in registries. */
	public static void registerContent() {
		EnigmaticEffects.init();
		EnigmaticPotions.init();
		EnigmaticSounds.init();
		EnigmaticAttributes.init();
		EnigmaticEnchantments.init();
		EnigmaticBlocks.init();
		EnigmaticTiles.init();
		EnigmaticItems.init();
		EnigmaticEntities.init();
		EnigmaticMenus.init();
		EnigmaticLootFunctions.init();
		// Port: upstream adds this in onLoadComplete.
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.ANGEL_BLESSING);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.GOLEM_HEART);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.OCEAN_STONE);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.BLAZING_CORE);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.EYE_OF_NEBULA);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.VOID_PEARL);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.THE_CUBE);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.ENIGMATIC_ITEM);
		EnigmaticRecipes.init();
		EnigmaticTabs.init();
	}

	/** Drop rules, custom stats and advancement triggers. Also registers into built-in registries. */
	public static void registerShared() {
		// Port: upstream onCurioDrops (Curios DropRulesEvent).
		AccessoryDrops.register((entity, stack) -> EnigmaticEnchantments.getLevel(EnigmaticEnchantments.ETERNAL_BINDING, stack) > 0 ? DropRule.ALWAYS_KEEP : null);
		AccessoryDrops.register((entity, stack) -> entity instanceof ServerPlayer player
				&& EnigmaticEventHandler.POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.ELDRITCH_AMULET)
				&& EnigmaticEnchantments.getLevel(Enchantments.VANISHING_CURSE, stack) <= 0 ? DropRule.ALWAYS_KEEP : null);
		PlayerPlaytimeCounter.registerStats();
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, BeheadingTrigger.ID, BeheadingTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, UseUnholyGrailTrigger.ID, UseUnholyGrailTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, CursedRingEquippedTrigger.ID, CursedRingEquippedTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, EquipAccessoryTrigger.ID, EquipAccessoryTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, CursedInventoryChangedTrigger.ID, CursedInventoryChangedTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, ForbiddenFruitTrigger.ID, ForbiddenFruitTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, RevelationGainTrigger.ID, RevelationGainTrigger.INSTANCE);
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, RevelationTomeBurntTrigger.ID, RevelationTomeBurntTrigger.INSTANCE);
	}

	/**
	 * Port: 1.21 enchantments are data-driven. Stands in for upstream's per-enchantment canEnchant
	 * overrides, the config toggles, and the items' canApplyAtEnchantingTable/isBookEnchantable.
	 *
	 * @param primary true for the enchanting table check, false for the anvil/acceptable check
	 * @return whether the enchantment may go on the stack, or null for the default behavior
	 */
	@Nullable
	public static Boolean allowEnchanting(Holder<Enchantment> enchantment, ItemStack stack, boolean primary) {
		ResourceKey<Enchantment> own = EnigmaticEnchantments.ownKey(enchantment);
		if (own != null && !OmniconfigHandler.isItemEnabled(own))
			return false;

		if (enchantment.is(EnigmaticEnchantments.ETERNAL_BINDING)) {
			Boolean applies = EternalBindingCurse.canApplyTo(stack);
			if (applies != null)
				return applies;
		}

		// Port: upstream's MixinBindingCurseEnchantment (BindingCurseEnchantment is gone in 1.21).
		if (enchantment.is(Enchantments.BINDING_CURSE) && stack.getItem() instanceof IBindable)
			return true;

		if (stack.getItem() instanceof EldritchPan) {
			ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
			return primary ? enchantment.value().isPrimaryItem(sword) : enchantment.value().canEnchant(sword);
		}

		if (stack.getItem() instanceof TheAcknowledgment && enchantment.is(Enchantments.BANE_OF_ARTHROPODS))
			return true;

		if (stack.getItem() instanceof CursedRing && enchantment.is(Enchantments.VANISHING_CURSE))
			return false;

		return null;
	}

	public static void registerPackets() {
		var network = Services.NETWORK;
		network.register(PacketSyncPlayTime.class, PacketDirection.TO_CLIENT, PacketSyncPlayTime::encode,
				PacketSyncPlayTime::decode, PacketSyncPlayTime::handle);
		network.register(PacketUpdateCompass.class, PacketDirection.TO_CLIENT, PacketUpdateCompass::encode,
				PacketUpdateCompass::decode, PacketUpdateCompass::handle);
		network.register(PacketSyncFireImmunity.class, PacketDirection.TO_CLIENT, PacketSyncFireImmunity::encode,
				PacketSyncFireImmunity::decode, PacketSyncFireImmunity::handle);
		network.register(PacketPlayQuote.class, PacketDirection.TO_CLIENT, PacketPlayQuote::encode,
				PacketPlayQuote::decode, PacketPlayQuote::handle);
		network.register(PacketSyncForbiddenFruit.class, PacketDirection.TO_CLIENT, PacketSyncForbiddenFruit::encode,
				PacketSyncForbiddenFruit::decode, PacketSyncForbiddenFruit::handle);
		network.register(PacketSyncOptions.class, PacketDirection.TO_CLIENT, PacketSyncOptions::encode,
				PacketSyncOptions::decode, PacketSyncOptions::handle);
		network.register(PacketPortalParticles.class, PacketDirection.TO_CLIENT, PacketPortalParticles::encode,
				PacketPortalParticles::decode, PacketPortalParticles::handle);
		network.register(PacketFlameParticles.class, PacketDirection.TO_CLIENT, PacketFlameParticles::encode,
				PacketFlameParticles::decode, PacketFlameParticles::handle);
		network.register(PacketGenericParticleEffect.class, PacketDirection.TO_CLIENT, PacketGenericParticleEffect::encode,
				PacketGenericParticleEffect::decode, PacketGenericParticleEffect::handle);
		network.register(PacketRecallParticles.class, PacketDirection.TO_CLIENT, PacketRecallParticles::encode,
				PacketRecallParticles::decode, PacketRecallParticles::handle);
		network.register(PacketHandleItemPickup.class, PacketDirection.TO_CLIENT, PacketHandleItemPickup::encode,
				PacketHandleItemPickup::decode, PacketHandleItemPickup::handle);
		network.register(PacketEnderRingKey.class, PacketDirection.TO_SERVER, PacketEnderRingKey::encode,
				PacketEnderRingKey::decode, PacketEnderRingKey::handle);
		network.register(PacketToggleMagnetEffects.class, PacketDirection.TO_SERVER, PacketToggleMagnetEffects::encode,
				PacketToggleMagnetEffects::decode, PacketToggleMagnetEffects::handle);
		network.register(PacketSyncMagnetEffects.class, PacketDirection.TO_CLIENT, PacketSyncMagnetEffects::encode,
				PacketSyncMagnetEffects::decode, PacketSyncMagnetEffects::handle);
		network.register(PacketXPScrollKey.class, PacketDirection.TO_SERVER, PacketXPScrollKey::encode,
				PacketXPScrollKey::decode, PacketXPScrollKey::handle);
		network.register(PacketInkwellField.class, PacketDirection.TO_SERVER, PacketInkwellField::encode,
				PacketInkwellField::decode, PacketInkwellField::handle);
		network.register(PacketEnchantingGUI.class, PacketDirection.TO_SERVER, PacketEnchantingGUI::encode,
				PacketEnchantingGUI::decode, PacketEnchantingGUI::handle);
		network.register(PacketWitherParticles.class, PacketDirection.TO_CLIENT, PacketWitherParticles::encode,
				PacketWitherParticles::decode, PacketWitherParticles::handle);
		network.register(PacketSpellstoneKey.class, PacketDirection.TO_SERVER, PacketSpellstoneKey::encode,
				PacketSpellstoneKey::decode, PacketSpellstoneKey::handle);
		network.register(PacketForceArrowRotations.class, PacketDirection.TO_CLIENT, PacketForceArrowRotations::encode,
				PacketForceArrowRotations::decode, PacketForceArrowRotations::handle);
		network.register(PacketSlotUnlocked.class, PacketDirection.TO_CLIENT, PacketSlotUnlocked::encode,
				PacketSlotUnlocked::decode, PacketSlotUnlocked::handle);
		network.register(PacketSetEntryState.class, PacketDirection.TO_CLIENT, PacketSetEntryState::encode,
				PacketSetEntryState::decode, PacketSetEntryState::handle);
		network.register(PacketPatchouliForce.class, PacketDirection.TO_CLIENT, PacketPatchouliForce::encode,
				PacketPatchouliForce::decode, PacketPatchouliForce::handle);
		network.register(PacketPermadeath.class, PacketDirection.TO_CLIENT, PacketPermadeath::encode,
				PacketPermadeath::decode, PacketPermadeath::handle);
		network.register(PacketPlayerMotion.class, PacketDirection.TO_CLIENT, PacketPlayerMotion::encode,
				PacketPlayerMotion::decode, PacketPlayerMotion::handle);
		network.register(PacketPlayerSetlook.class, PacketDirection.TO_CLIENT, PacketPlayerSetlook::encode,
				PacketPlayerSetlook::decode, PacketPlayerSetlook::handle);
		network.register(PacketCosmicRevive.class, PacketDirection.TO_CLIENT, PacketCosmicRevive::encode,
				PacketCosmicRevive::decode, PacketCosmicRevive::handle);
		network.register(PacketUpdateElytraBoosting.class, PacketDirection.TO_SERVER, PacketUpdateElytraBoosting::encode,
				PacketUpdateElytraBoosting::decode, PacketUpdateElytraBoosting::handle);
		network.register(PacketSyncElytraBoosting.class, PacketDirection.TO_CLIENT, PacketSyncElytraBoosting::encode,
				PacketSyncElytraBoosting::decode, PacketSyncElytraBoosting::handle);
	}
}
