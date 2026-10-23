package com.aizistral.enigmaticlegacy.fabric;

import java.io.IOException;
import java.io.Reader;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port-only. Stands in for Forge's {@code forge:separate_transforms} model loader, which
 * upstream item models use to show one model in the inventory and another in hand. Vanilla
 * reads such a file as an empty model, so this plugin reads the {@code base} and
 * {@code perspectives} entries itself and wraps the model. {@code MixinItemRenderer} then
 * swaps in the model for the current display context, like Forge's
 * {@code BakedModel.applyTransform}. Only parent-only entries are supported (all upstream
 * models are).
 */
public class SeparateTransformsModel {
	private static final String LOADER = "forge:separate_transforms";

	public static void register() {
		PreparableModelLoadingPlugin.register((manager, executor) -> CompletableFuture.supplyAsync(() -> {
			Map<ResourceLocation, Definition> definitions = new HashMap<>();

			manager.listResources("models", id -> id.getNamespace().equals(EnigmaticLegacy.MODID)
					&& id.getPath().endsWith(".json")).forEach((file, resource) -> {
						Definition definition = readDefinition(file, resource);

						if (definition != null) {
							String path = file.getPath();
							definitions.put(new ResourceLocation(file.getNamespace(),
									path.substring("models/".length(), path.length() - ".json".length())), definition);
						}
					});

			return definitions;
		}, executor), (definitions, context) -> context.modifyModelOnLoad().register((model, modelContext) -> {
			Definition definition = definitions.get(toModelPath(modelContext.id()));

			if (definition != null && model instanceof BlockModel owner)
				return new Unbaked(owner, definition);
			else
				return model;
		}));
	}

	@Nullable
	private static Definition readDefinition(ResourceLocation file, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

			if (!LOADER.equals(GsonHelper.getAsString(json, "loader", "")))
				return null;

			ResourceLocation base = new ResourceLocation(GsonHelper.getAsString(json.getAsJsonObject("base"), "parent"));
			Map<ItemDisplayContext, ResourceLocation> perspectives = new EnumMap<>(ItemDisplayContext.class);
			JsonObject perspectivesJson = GsonHelper.getAsJsonObject(json, "perspectives", new JsonObject());

			for (ItemDisplayContext displayContext : ItemDisplayContext.values()) {
				if (perspectivesJson.has(displayContext.getSerializedName())) {
					perspectives.put(displayContext, new ResourceLocation(GsonHelper.getAsString(
							perspectivesJson.getAsJsonObject(displayContext.getSerializedName()), "parent")));
				}
			}

			return new Definition(base, perspectives);
		} catch (IOException | RuntimeException ex) {
			EnigmaticLegacy.LOGGER.warn("Could not read model " + file + ": " + ex);
			return null;
		}
	}

	// Item models load under "namespace:path#inventory"; their file is models/item/path.json.
	private static ResourceLocation toModelPath(ResourceLocation id) {
		if (id instanceof ModelResourceLocation modelId) {
			if ("inventory".equals(modelId.getVariant()))
				return new ResourceLocation(id.getNamespace(), "item/" + id.getPath());
			else
				return new ResourceLocation(id.getNamespace(), "blockstates/" + id.getPath());
		}

		return id;
	}

	/**
	 * Called from {@code MixinItemRenderer} before an item is transformed and drawn.
	 */
	public static BakedModel forContext(BakedModel model, ItemDisplayContext displayContext) {
		return model instanceof Baked baked ? baked.perspectives.getOrDefault(displayContext, baked.base) : model;
	}

	private record Definition(ResourceLocation base, Map<ItemDisplayContext, ResourceLocation> perspectives) {}

	private static class Unbaked implements UnbakedModel {
		private final BlockModel owner;
		private final Definition definition;

		private Unbaked(BlockModel owner, Definition definition) {
			this.owner = owner;
			this.definition = definition;
		}

		@Override
		public Set<ResourceLocation> getDependencies() {
			Set<ResourceLocation> dependencies = new HashSet<>(this.owner.getDependencies());
			dependencies.add(this.definition.base());
			dependencies.addAll(this.definition.perspectives().values());
			return dependencies;
		}

		@Override
		public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
			// The owner's parents decide its gui_light; the parts are not top-level models.
			this.owner.resolveParents(resolver);
			resolver.apply(this.definition.base()).resolveParents(resolver);
			this.definition.perspectives().values().forEach(id -> resolver.apply(id).resolveParents(resolver));
		}

		@Override
		public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state, ResourceLocation location) {
			Map<ItemDisplayContext, BakedModel> perspectives = new EnumMap<>(ItemDisplayContext.class);
			this.definition.perspectives().forEach((displayContext, id) -> perspectives.put(displayContext, baker.bake(id, state)));
			return new Baked(baker.bake(this.definition.base(), state), perspectives, this.owner.getGuiLight().lightLikeBlock());
		}
	}

	private static class Baked implements BakedModel {
		private final BakedModel base;
		private final Map<ItemDisplayContext, BakedModel> perspectives;
		private final boolean usesBlockLight;

		private Baked(BakedModel base, Map<ItemDisplayContext, BakedModel> perspectives, boolean usesBlockLight) {
			this.base = base;
			this.perspectives = perspectives;
			this.usesBlockLight = usesBlockLight;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
			return this.base.getQuads(state, side, random);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return this.base.useAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return this.base.isGui3d();
		}

		@Override
		public boolean usesBlockLight() {
			return this.usesBlockLight;
		}

		@Override
		public boolean isCustomRenderer() {
			return this.base.isCustomRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return this.base.getParticleIcon();
		}

		@Override
		public ItemTransforms getTransforms() {
			return this.base.getTransforms();
		}

		@Override
		public ItemOverrides getOverrides() {
			return ItemOverrides.EMPTY;
		}
	}

}
