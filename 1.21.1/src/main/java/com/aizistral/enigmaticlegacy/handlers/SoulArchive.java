package com.aizistral.enigmaticlegacy.handlers;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

public class SoulArchive {
	private static final Type SOUL_RECORDS_TYPE = new TypeToken<List<SoulData>>(){}.getType();
	private static SoulArchive instance;
	private static MinecraftServer server;
	private final File saveFile;
	private final Multimap<ResourceKey<Level>, SoulData> data = HashMultimap.create();

	public SoulArchive(File saveFolder) {
		Preconditions.checkArgument(saveFolder.exists() && saveFolder.isDirectory(),
				"File " + saveFolder + " does not exist or is not a folder!");
		this.saveFile = new File(saveFolder, "soul_archive.json");
	}

	public Optional<Tuple<UUID, BlockPos>> findNearest(Player player, BlockPos pos) {
		var data =  this.data.get(player.level().dimension()).stream()
				.filter(record -> record.type == 0)
				.filter(record -> Objects.equals(record.ownerID, player.getUUID()))
				.reduce((r1, r2) -> pos.distSqr(r1.pos()) > pos.distSqr(r2.pos()) ? r2 : r1)
				.orElse(null);

		return data != null ? Optional.of(new Tuple<>(data.id, data.pos())) : Optional.empty();
	}

	public Optional<Tuple<UUID, BlockPos>> findNearest(Level level, BlockPos pos) {
		var data =  this.data.get(level.dimension()).stream()
				.filter(record -> record.type == 0)
				.reduce((r1, r2) -> pos.distSqr(r1.pos()) > pos.distSqr(r2.pos()) ? r2 : r1)
				.orElse(null);

		return data != null ? Optional.of(new Tuple<>(data.id, data.pos())) : Optional.empty();
	}

	// Port: commons-io FileUtils/IOUtils -> java.nio Files.
	public void save() {
		try {
			Files.write(this.saveFile.toPath(), this.saveToBytes());
		} catch (Exception ex) {
			EnigmaticLegacy.LOGGER.fatal("FAILED TO SAVE FILE: " + this.saveFile);
			throw new RuntimeException(ex);
		}
	}

	public void load() {
		try {
			if (!this.saveFile.exists() || !this.saveFile.isFile())
				return;

			this.loadFromBytes(Files.readAllBytes(this.saveFile.toPath()));
		} catch (Exception ex) {
			// Port: upstream threw here, which stops the server. A file in upstream's Forge
			// format (or a corrupted one) is set aside instead, and the archive starts empty.
			this.data.clear();
			File invalid = new File(this.saveFile.getParentFile(), "soul_archive.json.invalid");
			EnigmaticLegacy.LOGGER.error("Could not read " + this.saveFile + ", moving it to " + invalid + ": " + ex);

			try {
				Files.move(this.saveFile.toPath(), invalid.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception moveEx) {
				EnigmaticLegacy.LOGGER.error("Could not move " + this.saveFile + ": " + moveEx);
			}
		}
	}

	private byte[] saveToBytes() {
		String text = new GsonBuilder().setPrettyPrinting().create().toJson(new ArrayList<>(this.data.values()));
		return text.getBytes(StandardCharsets.UTF_8);
	}

	private void loadFromBytes(byte[] bytes) {
		this.data.clear();

		String text = new String(bytes, StandardCharsets.UTF_8);
		List<SoulData> list =  new Gson().fromJson(text, SOUL_RECORDS_TYPE);

		if (list == null)
			return;

		list.forEach(record -> {
			ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(record.dimension));
			this.data.put(key, record);
		});
	}

	private void synchronize() {
		// Port: upstream reads the server from ServerLifecycleHooks; initialize stores it here.
		server.getPlayerList().getPlayers().forEach(player -> {
			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.SOUL_COMPASS)) {
				SuperpositionHandler.updateSoulCompass(player);
			}
		});
	}

	public void addItem(PermanentItemEntity item) {
		SoulData record = new SoulData(item.level().dimension().location(), item.getUUID(), item.getOwnerId(),
				item.blockPosition(), item.containsSoul() ? 0 : 1);

		if (this.data.put(item.level().dimension(), record)) {
			this.save();
			this.synchronize();
		}
	}

	public void removeItem(UUID id) {
		if (this.data.values().removeIf(record -> record.id.equals(id))) {
			this.save();
			this.synchronize();
		}
	}

	public void removeItem(PermanentItemEntity item) {
		if (this.data.values().removeIf(record -> record.isEqual(item))) {
			this.save();
			this.synchronize();
		}
	}

	public static SoulArchive getInstance() {
		return instance;
	}

	public static void initialize(MinecraftServer server) {
		SoulArchive.server = server;
		instance = new SoulArchive(server.getWorldPath(LevelResource.ROOT).toFile());
		instance.load();
	}

	/**
	 * Port: upstream let Gson serialize ResourceLocation and BlockPos by reflection, which
	 * writes obfuscated field names outside the dev environment. The dimension is stored
	 * as a string and the position as plain x/y/z here, so the file reads the same everywhere.
	 */
	private static class SoulData {
		private String dimension;
		private UUID id;
		private UUID ownerID;
		private int x, y, z;
		private int type;

		SoulData(ResourceLocation dimension, UUID id, UUID ownerID, BlockPos pos, int type) {
			this.dimension = dimension.toString();
			this.id = id;
			this.ownerID = ownerID;
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			this.type = type;
		}

		BlockPos pos() {
			return new BlockPos(this.x, this.y, this.z);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.dimension, this.id, this.ownerID, this.x, this.y, this.z, this.type);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (this.getClass() != obj.getClass())
				return false;
			SoulData other = (SoulData) obj;
			return Objects.equals(this.dimension, other.dimension) && Objects.equals(this.id, other.id) && Objects.equals(this.ownerID, other.ownerID)
					&& this.x == other.x && this.y == other.y && this.z == other.z && this.type == other.type;
		}

		@Override
		public String toString() {
			return "SoulData [dimension=" + this.dimension + ", id=" + this.id + ", ownerID=" + this.ownerID + ", pos=" + this.pos() + ", type=" + this.type + "]";
		}

		boolean isEqual(PermanentItemEntity item) {
			return Objects.equals(this.id, item.getUUID()) && Objects.equals(this.ownerID, item.getOwnerId());
		}
	}

}
