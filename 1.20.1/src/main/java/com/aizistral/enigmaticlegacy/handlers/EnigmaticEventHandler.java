package com.aizistral.enigmaticlegacy.handlers;

import java.util.function.Consumer;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Port: upstream's handler is one big Forge event subscriber. Here it holds only the
 * handlers ported so far, as plain static methods; each loader wires them to its own
 * events (Fabric: {@code EnigmaticLegacyFabric}).
 */
public class EnigmaticEventHandler {

	/**
	 * Port: was part of the Forge {@code LivingAttackEvent} handler. Fabric calls it from
	 * {@code ServerLivingEntityEvents.ALLOW_DAMAGE}.
	 *
	 * @return false to cancel the attack (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onLivingAttack(LivingEntity entity, DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof Player && source.is(DamageTypes.PLAYER_ATTACK)) {
			Player player = (Player) source.getDirectEntity();

			/*
			 * Handler for triggering Extradimensional Eye's effects when player left-clicks
			 * (or, more technically correct, directly attacks) the entity with the Eye in
			 * main hand.
			 */

			if (player.getMainHandItem() != null && player.getMainHandItem().getItem() == EnigmaticItems.EXTRADIMENSIONAL_EYE)
				if (ItemNBTHelper.verifyExistance(player.getMainHandItem(), "BoundDimension"))
					if (ItemNBTHelper.getString(player.getMainHandItem(), "BoundDimension", "minecraft:overworld").equals(entity.level().dimension().location().toString())) {
						ItemStack stack = player.getMainHandItem();

						Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketPortalParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 96, 1.5D, false));

						entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
						entity.teleportTo(ItemNBTHelper.getDouble(stack, "BoundX", 0D), ItemNBTHelper.getDouble(stack, "BoundY", 0D), ItemNBTHelper.getDouble(stack, "BoundZ", 0D));
						entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));

						Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketRecallParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 48, false));

						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
						}

						return false;
					}
		}

		return true;
	}

	/**
	 * Port: the End city part of upstream's {@code LootTableLoadEvent} handler. Fabric calls it
	 * from {@code LootTableEvents.MODIFY}; {@code addPool} adds the pool to the table.
	 * The other dungeon pools are not ported yet.
	 */
	public static void onLootTablesLoaded(ResourceLocation id, Consumer<LootPool> addPool) {
		if (!OmniconfigHandler.customDungeonLootEnabled.getValue())
			return;

		if (id.equals(BuiltInLootTables.END_CITY_TREASURE)) {
			// Port: upstream also adds Lore Inscriber (weight 10) and Etherium Ore (weight 60,
			// 1-2); neither is ported yet, so they are left out.
			LootPool epic = SuperpositionHandler.constructLootPool("epic", 1F, 2F,
					LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(40).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))),
					LootItem.lootTableItem(Items.ENDER_EYE).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))),
					LootItem.lootTableItem(Items.GLISTERING_MELON_SLICE).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))),
					LootItem.lootTableItem(Items.GOLDEN_CARROT).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))),
					LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))),
					LootItem.lootTableItem(Items.ENCHANTING_TABLE).setWeight(10),
					LootItem.lootTableItem(Items.CAKE).setWeight(15),
					LootItem.lootTableItem(Items.END_CRYSTAL).setWeight(7),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.RECALL_POTION, 15),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.MENDING_MIXTURE, 40),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ASTRAL_DUST, 85, 1F, 4F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EXTRADIMENSIONAL_EYE, 20));

			addPool.accept(epic);
			// Port: upstream's second End pool (Corrupted Tome with a revelation function) is not ported.
		}
	}

}
