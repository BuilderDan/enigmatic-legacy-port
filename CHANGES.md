# Changes from upstream

Enigmatic Legacy is owned by Aizistral (Aizistral Studios). This file lists every way
this port differs from the upstream mod, as the Enigmatic Legacy License requires.

Upstream base: https://github.com/Aizistral-Studios/Enigmatic-Legacy, branch `1.20.X`,
commit `a0294f1` (mod version 2.30.1, Forge 1.20.1).

## All versions

- None shared yet; each version's code changes are listed under its own section below.

## Per version

Sections are added here as each Minecraft version / loader is ported.

### 1.20.1 Fabric (`1.20.1/`, in progress)

- Build: Forge Gradle replaced with Fabric Loom (Mojang mappings + Parchment).
  Upstream's `EL_logo.png` is used as the mod icon.
- New files (not in upstream): `com.aizistral.enigmaticlegacy.fabric.EnigmaticLegacyFabric`
  and `EnigmaticLegacyFabricClient` (Fabric entry points), `fabric.mod.json`.
- Dependencies: Curios -> Trinkets; playtime capability -> Cardinal Components API;
  Patchouli kept (Fabric build).
- Mod version: `2.30.1+fabric.N`, where N counts port releases.
- Build only: Trinkets is pulled from the Modrinth maven (the TerraformersMC maven jar of
  3.7.2 crashes Mojang-mapped dev runs). No effect on the released mod.
- Resources: upstream `assets/` and `data/` are copied unchanged by `tools/sync_resources.py`,
  except for the conversions below.
- Accessory slots (Curios -> Trinkets), via `data/trinkets/`:
  - charm -> new slot `chest/charm` (one slot for amulets and charms, as upstream).
  - ring -> Trinkets' standard `hand/ring` and `offhand/ring` (2 ring slots, as upstream;
    shared with other Trinkets mods).
  - back -> Trinkets' standard `chest/back`.
  - spellstone -> new slot `legs/spellstone`, scroll -> new slot `head/scroll`. Both start
    at 0 slots and are unlocked per player, like upstream.
  - Curios item tags become Trinkets tags; all slots are enabled for players in
    `data/trinkets/entities/enigmaticlegacy.json`. `data/curios/` is not copied.
- Recipes: Forge `"conditions"` become Fabric API `"fabric:load_conditions"`.
  `EnabledCondition` is rewritten as a Fabric resource condition. Until the config is
  ported it only checks that the item exists, so every recipe is enabled.
- Not copied (to be ported in code): `data/forge/` and the Forge global loot modifier
  under `data/enigmaticlegacy/loot_modifiers/`.
- Code abstraction layer (port-only, package `platform/`): `Services` loads loader backends
  through `ServiceLoader`. `IAccessoryItem` and `AccessorySlots` stand in for the Curios API,
  `NetworkPlatform` and `PacketContext` for Forge `SimpleChannel` and `NetworkEvent.Context`,
  and `PlayerDataPlatform` for Forge capabilities. Fabric backends are in `fabric/platform/`
  (Trinkets, Fabric API networking, Cardinal Components). Each packet class gets its own
  channel, `enigmaticlegacy:<class name>`.
- New common class `EnigmaticLegacy` holds `MODID` and `LOGGER`. Upstream keeps them in the
  Forge `@Mod` class.
- `IPlaytimeCounter` / `PlayerPlaytimeCounter`: no longer Forge capabilities. The counter
  is a Cardinal Components player component, `enigmaticlegacy:playtime_counter`.
  - It is copied on every respawn, including returning from the End. Upstream copies it
    only on death.
  - The two custom stats are now registered in code (`registerStats()`).
- `PacketSyncPlayTime`: uses `PacketContext`; `@OnlyIn` removed.
- Trinkets has no looting or "recently hit" info when it decides drop rules, so accessory
  drop rules get looting 0 and recently-hit false. Upstream's extra drop rules (Curios
  `onCurioDrops`) go through `AccessoryDrops` and Trinkets' drop callback.
- Accessory `onEquip`/`onUnequip` get an empty stack as the previous/new stack; Trinkets
  does not pass it. Upstream gets the real stack from Curios.
- `EnigmaticLegacy.LOGGER` is upstream's `LoggerWrapper` (log4j) instead of an slf4j logger.
- Config holder discovery: upstream finds `@SubscribeConfig` and `@ConfigurableItem` through
  Forge scan data. The port lists the holder classes in `config/ConfigHolders` and reads
  the annotations by reflection; classes not ported yet are skipped.
- `SuperpositionHandler` is ported piece by piece; methods are added as callers are ported.
- Omniconfig (`com.aizistral.omniconfig`):
  - Config and game folders come from `Services.PLATFORM` instead of `FMLPaths`.
  - `SidedConfigType.getDist()` is replaced by `isActiveOnThisSide()`; sided actions run
    on the physical side directly instead of through `DistExecutor`.
  - New port-only `FileWatcher` (Java `WatchService`) replaces night-config's, which
    Fabric does not ship.
  - `PacketSyncOptions` is sent with `Services.NETWORK` and handled with `PacketContext`.
- Config sync on login/logout moved from `EnigmaticEventHandler` to the new
  `config/OmniconfigSync`, called from Fabric connection events.
- `OmniconfigHandler.isBoss` uses `BuiltInRegistries` instead of `ForgeRegistries`.
- Registries: Forge `DeferredRegister`/`RegisterEvent` replaced by direct vanilla
  `Registry.register` in `registries/AbstractRegistry`. Forge `@ObjectHolder` replaced by a
  port-only `api/generic/ObjectHolder` annotation that `AbstractRegistry` fills by reflection.
  Holder fields are non-final with no initializer. `init()` methods (port-only) force class
  loading from the Fabric entry point. The `onRegister` block-item hook is left out until
  blocks are ported.
- `EnigmaticItems` holds only the ported items (so far: Blank Scroll, Astral Dust, Lore
  Fragment, Heart of the Earth, Twisted Heart, Nefarious Essence, Nefarious Ingot, Unholy
  Stone, Darkest Scroll, Soul Dust), registered in upstream order.
- Creative tabs: built with `FabricItemGroup`; contents come from `ICreativeTabMember` in
  `displayItems` instead of Forge's `BuildCreativeModeTabContentsEvent`. Temporary: the main
  tab icon is Blank Scroll until the Enigmatic Amulet is ported; the potions tab returns with
  the potion items.
- `@OnlyIn` is removed (not replaced with Fabric `@Environment`) so common code stays
  loader-neutral; only client code calls the client-only methods (`ItemLoreHelper`,
  `LoreFragment`).
- `ItemLoreHelper.indicateWorthyOnesOnly` / `indicateBlessedOnesOnly` are left out until the
  Cursed Ring and the Architect/Believer items are ported.
- Temporary: `SuperpositionHandler.isTheCursedOne` always returns false until the Cursed Ring
  is ported (cursed-only tooltips and Twisted Heart/Soul Dust curse checks are inactive).
- `SuperpositionHandler.isExplosion` uses the vanilla `DamageTypeTags.IS_EXPLOSION` tag.
- Dev only, port-only: `fabric/DevSelfCheck`, a client smoke test that runs only when the
  `enigmaticlegacy.devcheck` system property is set (Gradle `-Pdevcheck=true|exit`). It is
  inactive in normal play.
- Resources: the upstream folder `textures/item/3d/alt textures/` is renamed to
  `alt_textures/` by `tools/sync_resources.py`. A space is not valid in a resource path,
  and nothing references these textures.
