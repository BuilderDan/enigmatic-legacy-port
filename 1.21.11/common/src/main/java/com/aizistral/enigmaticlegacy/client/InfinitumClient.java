package com.aizistral.enigmaticlegacy.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Port: The Infinitum's "the_infinitum_open" item property from upstream's ClientProxy. Upstream
 * advanced the counters from a client-side {@code LivingTickEvent} once per world tick; here the
 * Fabric client tick does the same.
 */
public class InfinitumClient {
	private static final List<InfinitumCounterEntry> theInfinitumHoldTicks = new ArrayList<>();
	private static long clientWorldTicks = 0;

	public static float getOpenProperty(ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
		if (entity instanceof Player player) {
			for (InfinitumCounterEntry entry : theInfinitumHoldTicks) {
				if (entry.getPlayer() == player && entry.getStack() == stack)
					return entry.animValue;
			}

			ItemStack mainhand = player.getMainHandItem();
			ItemStack offhand = player.getOffhandItem();

			if (mainhand == stack || offhand == stack)
				if (SuperpositionHandler.isTheWorthyOne(player)) {
					theInfinitumHoldTicks.add(new InfinitumCounterEntry(player, stack));
				}
		}

		return 0;
	}

	public static void onClientTick(Minecraft mc) {
		if (mc.level != null && clientWorldTicks != mc.level.getGameTime()) {
			clientWorldTicks = mc.level.getGameTime();
			updateInfinitumCounters();
		}
	}

	public static void onDisconnect() {
		theInfinitumHoldTicks.clear();
	}

	private static void updateInfinitumCounters() {
		for (InfinitumCounterEntry entry : new ArrayList<>(theInfinitumHoldTicks)) {
			if (entry.getPlayer() == null || entry.getStack() == null) {
				theInfinitumHoldTicks.remove(entry);
				continue;
			}

			ItemStack stack = entry.getStack();
			Player player = entry.getPlayer();
			ItemStack mainhand = player.getMainHandItem();
			ItemStack offhand = player.getOffhandItem();

			if (mainhand == stack || offhand == stack)
				if (SuperpositionHandler.isTheWorthyOne(player)) {
					if (entry.counter > 5) {
						entry.animValue = 1F;
					} else if (entry.counter == 5) {
						entry.animValue = 0.5F;
					} else {
						entry.animValue = 0F;
					}

					entry.counter++;
					continue;
				}

			if (entry.counter <= 0) {
				theInfinitumHoldTicks.remove(entry);
			} else {
				if (entry.counter > 5) {
					entry.counter = 5;
				}

				if (entry.counter > 1) {
					entry.animValue = 1F;
				} else if (entry.counter == 1) {
					entry.animValue = 0.5F;
				} else {
					entry.animValue = 0F;
				}

				entry.counter--;
			}
		}
	}

	private static class InfinitumCounterEntry {
		private final WeakReference<ItemStack> stack;
		private final WeakReference<Player> player;
		private int counter = 0;
		private float animValue = 0F;

		public InfinitumCounterEntry(Player player, ItemStack stack) {
			this.player = new WeakReference<>(player);
			this.stack = new WeakReference<>(stack);
		}

		public Player getPlayer() {
			return this.player.get();
		}

		public ItemStack getStack() {
			return this.stack.get();
		}
	}
}
