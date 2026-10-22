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
- `EnigmaticItems` holds only the ported items (so far: Mending Mixture, Blank Scroll, Iron
  Ring, Extradimensional Eye, Potion of Recall, Etherium Ingot, Etherium Nugget, Etherium Scraps, Ender Rod, Astral Dust, Lore
  Fragment, Heart of the Earth, Twisted Heart, Exquisite Ring, Nefarious Essence, Nefarious
  Ingot, Unholy Stone, Darkest Scroll, Tome of Hungering Knowledge, Heart of the Cosmos,
  Heart of the Abyss, Soul Dust, Emblem of Monster Slayer, Scroll of Ageless Wisdom, Ring of
  Ender, Essence of Raging Life, Astral Potato, Charm of Treasure Hunter, Axe of Executioner, Heart of
  Creation, Blazing Core, Scroll of Postmortal Recall), registered in upstream order.
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
- Accessories: `ItemBaseCurio` implements the port's `IAccessoryItem` instead of Curios'
  `ICurioItem`. `AbstractRegistry` registers every `IAccessoryItem` with the accessory
  service as it is registered (Trinkets needs each item registered; Curios finds them by
  interface). `SuperpositionHandler.hasCurio` uses the same service.
- Right-click equip: Trinkets only does this for its own `TrinketItem`, so the Trinkets
  backend adds a `UseItemCallback` that equips any accessory whose `canEquipFromUse`
  allows it, like Curios does.
- `ItemBaseCurio` drops Forge's `isBookEnchantable` override. It returned true either way
  (Forge's default), so it had no effect. Curse of Binding still reaches accessories
  through upstream's own `MixinBindingCurseEnchantment` and `IBindable`, copied unchanged.
- Etherium items live in the `com.aizistral.etherium` package as upstream. `EtheriumUtil.
  defaultProperties` no longer reads the Etherium config, which upstream fetched but never
  used.
- `objects/Vector3`: `glVertex()` removed. It was an unused immediate-mode GL helper and
  the only client-only member, so the class is now safe on dedicated servers. `objects/Quat`
  is copied unchanged.
- `SuperpositionHandler`: added `setEntityMotionFromVector`, `canPickStack`,
  `canMergeStacks` and `stackEqualExact`, copied unchanged from upstream.
- Magnetic Ring and Dislocation Ring: the inventory button that toggles magnet effects
  (and the `TransientPlayerData` flag and packet behind it) is not ported yet, so magnet
  effects are always on. The client config keys for the button (`ButtonEnabled`,
  `ButtonOffset*`) are still read but do nothing yet.
- Magnetic Ring and Dislocation Ring: upstream skipped items whose Forge entity
  persistent data held `PreventRemoteMovement` (a cross-mod convention). Fabric entities
  have no such data map, so that check is dropped and those items are pulled too.
- `EnigmaticRecipes`: 6 of the 8 recipe serializers are ported (mending_mixture_repair,
  enchantment_transposing, bind_to_player, shapeless_no_return_craft,
  crafting_shaped_cursed, crafting_shaped_blessed), registered into vanilla's
  `RECIPE_SERIALIZER` registry. Not yet ported: `void_stone_combine` (needs the Void Stone)
  and `crafting_shaped_hidden` (needs the Enigmatic Amulet, Ascension Amulet and Cosmic
  Scroll); recipes of those two types do not load until then.
- `BlessedShapedRecipe` and `CursedShapedRecipe`: Forge's `getRecipeWidth()` /
  `getRecipeHeight()` replaced by vanilla's `getWidth()` / `getHeight()` (same values).
- `shapeless_no_return_craft`: upstream registers `ShapelessNoReturnRecipe.Serializer`,
  which only inherits vanilla's shapeless serializer, so loaded recipes are plain shapeless
  recipes. Kept as upstream does (a faithful quirk, not a port change).
- `DevotedBelieversHandler`: reads its file from the platform game directory
  (`Services.PLATFORM.getGameDir()`) instead of Forge's `FMLPaths.GAMEDIR`.
- `EnigmaticLegacy.SOUL_OF_THE_ARCHITECT` and `SuperpositionHandler.isTheBlessedOne`:
  copied unchanged from upstream (needed by the blessed recipes and the Tome).
- Dev only, port-only: `DevSelfCheck` also parses every bundled recipe JSON (with load
  conditions) and reports parse failures; recipes that name not-yet-ported content are
  reported as pending.
- Exquisite Ring (`GoldenRing`): Forge's piglin hooks do not exist on Fabric. The new
  port-only `mixin/MixinPiglinAi` makes piglins neutral when an equipped accessory's
  `IAccessoryItem.makesPiglinsNeutral` returns true (`PiglinAi.isWearingGold`), and lets
  piglins accept the ring as barter currency (`PiglinAi.isBarterCurrency`).
  `isPiglinCurrency` is now a plain method instead of a Forge override. Upstream's armor-slot
  overload `makesPiglinsNeutral(ItemStack, LivingEntity)` is dropped, since the ring is never
  worn as armor.
- `api/items/IEldritch`: copied unchanged from upstream.
- Heart of the Abyss (`AbyssalHeart`): the shift tooltip (the worthy-ones indicator) is
  empty until the Cursed Ring is ported. The Ender Dragon does not drop it yet: upstream's
  `MixinEnderDragon` is not ported.
- Dev only, port-only: `DevSelfCheck` also checks that `MixinPiglinAi` applied (piglins
  take the Exquisite Ring as barter currency).
- `handlers/EnigmaticEventHandler`: upstream's Forge event subscriber is ported piece by
  piece as plain static methods that each loader wires to its own events. So far it has
  the Extradimensional Eye part of the `LivingAttackEvent` handler, called on Fabric from
  `ServerLivingEntityEvents.ALLOW_DAMAGE` (returning false instead of cancelling the event).
- Loot injection (`EnigmaticEventHandler.onLootTablesLoaded`): the End city part of
  upstream's `LootTableLoadEvent` handler, called on Fabric from `LootTableEvents.MODIFY`.
  It adds upstream's "epic" pool (1-2 rolls, same weights and counts) with Potion of Recall,
  Mending Mixture, Astral Dust and Extradimensional Eye. Differences from upstream:
  - Lore Inscriber and Etherium Ore are left out of the pool (not ported yet).
  - The second End city pool (Corrupted Tome) is not ported.
  - Vanilla 1.20.1 loot pools have no name, so `SuperpositionHandler.constructLootPool`
    ignores its `poolName` parameter.
  - The other dungeon pools and the lore fragments are not injected yet.
- `SuperpositionHandler`: added `constructLootPool` and `createOptionalLootEntry` (both
  overloads), close to upstream.
- Dev only, port-only: `DevSelfCheck` also checks that the End city table gets one pool.
- `PacketPortalParticles` and `PacketRecallParticles`: use `PacketContext`; `@OnlyIn` removed.
- `SuperpositionHandler`: added `getParticleMultiplier`, copied unchanged from upstream
  (client only; `@OnlyIn` removed).
- Potion of Recall (`RecallPotion`, `ItemBasePotion`, `helpers/AdvancedSpawnLocationHelper`):
  `canDrink` used `EnigmaticLegacy.PROXY.isInVanillaDimension`; it now calls the new
  `SuperpositionHandler.isInVanillaDimension`, which does the same check on both sides.
  `@OnlyIn` removed from `isFoil` and the tooltip.
- `SuperpositionHandler.backToSpawn`: upstream moved the player to another dimension with
  Forge's `changeDimension(level, RealSmoothTeleporter)`; the port uses vanilla
  `ServerPlayer.teleportTo(level, ...)`, which also keeps the position, before the same
  spawn-point teleport. `getWorld` uses the platform server instead of `ServerLifecycleHooks`.
- Brewing (`brewing/`): upstream's `AbstractBrewingRecipe`, `SpecialBrewingRecipe` and
  `ValidationBrewingRecipe` are copied as is (`javax` `@Nonnull` becomes JetBrains `@NotNull`).
  Fabric has no equivalent of Forge's `IBrewingRecipe` and `BrewingRecipeRegistry`, so the
  port adds its own (port-only, mod recipes only) plus three port-only mixins:
  `MixinPotionBrewing` (`isIngredient`, `hasMix`, `mix`; vanilla mixes always win),
  `MixinBrewingStandPotionSlot` (bottle slots accept mod inputs) and
  `MixinBrewingStandBlockEntity` (hoppers can insert them). Registration moved from upstream's
  common setup to `EnigmaticRecipes.registerBrewingRecipes()`. Only the Potion of Recall recipe
  (Awkward Potion + Eye of Ender) is registered; the Twisted, Common and Ultimate potion recipes
  return with those items. Upstream's `PotionHelper.createVanillaPotion` is replaced by
  `PotionUtils.setPotion`. Brewing-stand slot and hopper behavior: verify in game.
- `javax.annotation.Nullable` (from Forge's classpath) is replaced by
  `org.jetbrains.annotations.Nullable` where ported files used it.
- Emblem of Monster Slayer (`MonsterCharm`): upstream's Forge event cases are port-only
  mixins in `mixin/MixinLivingEntity` and `mixin/MixinEnchantmentHelper`, calling static
  methods on the item:
  - `LivingHurtEvent` -> `@ModifyVariable` on the amount at the head of
    `LivingEntity.actuallyHurt`. `Player` overrides `actuallyHurt`, so hits on players are not
    covered (the charm only affects monsters, so nothing is lost).
  - `LivingExperienceDropEvent` -> `@ModifyArg` on the `ExperienceOrb.award` amount in
    `LivingEntity.dropExperience`, with the last player who hurt the entity as attacker.
  - `LootingLevelEvent` -> `EnchantmentHelper.getMobLooting` RETURN adds the accessory
    looting bonus. The damage source and target passed to `getLootingLevel` are null there.
  - Behavior change: the `BonusLooting` and `DoubleXP` config options are honored. Upstream
    ignores both (always +1 looting, always double XP) while the tooltip follows them.
  - The Quote played when hitting the Ender Dragon is skipped; quotes are not ported.
- Scroll of Ageless Wisdom (`XPScroll`, `helpers/ExperienceHelper`, `registries/EnigmaticSounds`,
  `packets/server/PacketXPScrollKey`, `handlers/EnigmaticKeybindHandler`):
  - `ExperienceHelper` drops upstream's Forge `PlayerXpEvent` objects, which were built but
    never posted, so behavior is unchanged.
  - `EnigmaticSounds` registers all upstream sounds; the `Quote.getByID(0)` class-load in its
    init is skipped.
  - Keybinds: `KeyBindingHelper` + `ClientTickEvents.START_CLIENT_TICK` replace
    `RegisterKeyMappingsEvent` and the Forge client tick. The scroll (J) and Ender Ring (I) keys
    exist so far; the spellstone (K) key and the Angel's Blessing double jump come with their
    items.
  - `SuperpositionHandler.getCurioStack` uses the accessory service (Trinkets on Fabric).
- Ring of Ender (`EnderRing`, `packets/server/PacketEnderRingKey`):
  - The I key opens the ender chest through vanilla `ServerPlayer.openMenu` instead of
    upstream's hand-built open (manual container id, open-screen packet, Forge
    `PlayerContainerEvent.Open`). Same menu, title and open/close sounds.
  - The key does not accept the Ring of the Seven Curses yet (not ported).
  - The inventory-screen ender chest button (`EnderChestInventoryButton`) is not ported yet;
    its client config options are kept.
- Essence of Raging Life (`Infinimeal`):
  - Crop growth uses vanilla `BoneMealItem.growCrop` instead of Forge's `applyBonemeal`, so no
    Forge `BonemealEvent` fires and dispensers no longer use a fake player.
  - Vine growth particles call vanilla `BoneMealItem.addGrowthParticles` instead of the
    client proxy's identical copy.
- Astral Potato: unchanged (`ItemBaseFood` copied as-is).
- `IAccessoryItem.getLootingLevel` / `AccessorySlots`: the `target` parameter is now
  `@Nullable`.
- Dev only, port-only: `DevSelfCheck` also checks that the `LivingEntity` and
  `EnchantmentHelper` mixin handlers were merged.
- Charm of Treasure Hunter (`MiningCharm`, port-only `registries/EnigmaticAttributes`):
  - Reach: Forge's `BLOCK_REACH` attribute does not exist on Fabric. The port registers its own
    attribute, `enigmaticlegacy:block_reach` (added to players by `MixinPlayer`), and the charm
    uses it. Port-only mixins apply it to the client pick range (`MixinMultiPlayerGameMode`) and
    to the server's distance checks for breaking (`MixinServerPlayerGameMode`) and using blocks
    (`MixinServerGamePacketListenerImpl`). It covers block reach only, like upstream; other
    mods' reach attributes do not stack with it.
  - Fortune: Curios' fortune loot modifier is replaced by port-only mixins on
    `ApplyBonusCount` and `BonusLevelTableCondition`, which add accessory Fortune where vanilla
    reads the tool's Fortune level.
  - Break speed: Forge's `BreakSpeed` event becomes a `Player.getDestroySpeed` RETURN mixin
    calling `EnigmaticEventHandler.onBreakSpeed`. Only the charm's term of upstream's
    `miningStuff` handler is ported; the other items' terms come with those items.
  - `EnigmaticEventHandler.isApplyingNightVision` is kept as a static flag, as upstream.
- Dev only, port-only: `DevSelfCheck` also checks the new mixins and that players get the
  block reach attribute.
- Axe of Executioner (`ForbiddenAxe`, `triggers/BeheadingTrigger`):
  - `api/materials/EnigmaticMaterials` holds only the `FORBIDDEN_AXE` tier for now; Forge's
    `TierSortingRegistry` entries for the higher tiers come with the Etherium tools.
    `items/generic/ItemBaseTool` holds only `getDefaultProperties()` for now.
  - Tooltip: the Curios looting bonus becomes `Services.ACCESSORIES.getLootingBonus` (Trinkets).
  - Beheading: Forge's `LivingDropsEvent` becomes port-only `MixinLivingEntity`
    (`dropAllDeathLoot` HEAD/RETURN) plus `MixinEntity` (records `spawnAtLocation` results
    through the port-only `objects/DropCapture` interface), calling
    `EnigmaticEventHandler.onLivingDrops`. Since the Escape Scroll, drops are captured before
    they spawn (see below), so handlers can add and remove drops like Forge. The looting level passed in is `EnchantmentHelper.getMobLooting`
    of the killer (which includes accessory looting).
  - `SuperpositionHandler.ifDroplistContainsItem` is a private helper in the handler for now.
  - `BeheadingTrigger` is registered with `CriteriaTriggers.register` in the Fabric entry
    point; `javax.annotation.Nonnull` becomes `org.jetbrains.annotations.NotNull`.
- Dev only, port-only: `DevSelfCheck` also checks the drop-capture mixins.
- Heart of Creation (`EnigmaticItem`, `ItemSpellstoneCurio`, `ISpellstone`, entity
  `UltimateWitherSkullEntity`):
  - The main creative tab icon is now the Heart of Creation, as upstream.
  - `EnigmaticItems.SPELLSTONES.add(ENIGMATIC_ITEM)` moves from upstream's `onLoadComplete` to
    the Fabric entry point, right after item registration.
  - Entities: port-only `registries/EnigmaticEntities` registers the Ultimate Wither Skull through
    `AbstractRegistry`; the renderer is registered with `EntityRendererRegistry` in the client
    entry point. The skull uses the vanilla spawn packet instead of Forge's `NetworkHooks`.
  - `WitherExplosion`: Forge's `onBlockExploded` is inlined. The skull drops Forge's
    `ExplosionEvent.Start` hook (the explosion always runs) and uses its own position instead
    of Forge's `Explosion.getPosition()`.
  - Renderer: the Etherium `ShieldAuraLayer` texture and offset are inlined (same values).
  - Spellstone cooldowns: upstream's `TransientPlayerData` field becomes a server-only
    `SuperpositionHandler.SPELLSTONE_COOLDOWNS` map (cleared when the server stops); the
    client cooldown overlay comes from vanilla item cooldowns, as upstream also sets them.
  - `SuperpositionHandler`: spellstone, advanced curio and observed-entity helpers added;
    `hasArchitectsFavor` returns false until that item is ported.
    `helpers/AOEMiningHelper` holds only `calcRayTrace` for now.
  - Events: damage immunities run in `onLivingAttack` (`ALLOW_DAMAGE`); death prevention
    becomes `ServerLivingEntityEvents.ALLOW_DEATH` (`onLivingDeath`); flight and cooldown
    ticking become `ServerTickEvents.END_SERVER_TICK` (`onPlayerTick`); Forge's
    `MobEffectEvent.Applicable` becomes a `LivingEntity.canBeAffected` HEAD mixin
    (`onPotionApplicable`); the airborne ×5 break speed joins `onBreakSpeed`. Only the Heart's
    terms of these upstream handlers are ported.
  - Keybind: the spellstone ability key (K) is registered with `KeyBindingHelper`.
  - Network: `PacketWitherParticles` (to client) and `PacketSpellstoneKey` (to server) are
    registered with `Services.NETWORK` and handled with `PacketContext`; sends use
    `sendToNear` with `TargetPoint` instead of Forge `PacketDistributor.NEAR`.
  - `EnigmaticItem` and `ItemSpellstoneCurio` use the platform `SlotContext` (Trinkets) instead
    of Curios, `BuiltInRegistries` instead of `ForgeRegistries`, and drop `@OnlyIn`.
- Blazing Core (`BlazingCore`):
  - The lava immunity timer moves from upstream's `TransientPlayerData` to port-only
    `objects/FireImmunityData` (server map cleared when the server stops, plus the client
    copy) and the port-only `PacketSyncFireImmunity`, sent when the timer changes and once
    for each new server player object (the timer restarts at 0 after respawn or a
    dimension return instead of carrying over).
  - Events: lava immunity and the timer run in `onLivingAttack` (`ALLOW_DAMAGE`) and
    `onPlayerTick`; the faster effect ticking runs in `onPlayerTick` on the server and in
    `ClientTickEvents.END_CLIENT_TICK` on the client (local player only). Forge's `LivingHurtEvent` for players
    becomes a `Player.actuallyHurt` HEAD `@ModifyVariable` (`onPlayerHurt`), because Player
    overrides that method without calling super. Only the Blazing Core's terms of these
    upstream handlers are ported (other spellstones' hurt branches come with their items).
  - Client: the heat bar replaces the experience bar through a `Gui.renderExperienceBar` HEAD
    mixin instead of `RenderGuiOverlayEvent.Pre`; lava fog uses a `FogRenderer.setupFog` TAIL
    mixin instead of `ViewportEvent.RenderFog` (the Molten Heart effect branch comes later).
  - Loot: the spellstone pool is added to nether dungeon chests
    (`SuperpositionHandler.getNetherDungeons`); upstream's nether "epic" pool is not ported yet.
- Scroll of Postmortal Recall (`EscapeScroll`, entity `PermanentItemEntity`):
  - Drops: Forge's `captureDrops` is emulated. Port-only `MixinEntity` and `MixinServerPlayer`
    wrap `Level.addFreshEntity` in `Entity.spawnAtLocation` and `ServerPlayer.drop` while
    `dropAllDeathLoot` runs (Trinkets' death drops included); `MixinLivingEntity` calls
    `onLivingDrops` at RETURN and then spawns whatever is left in the list.
  - Death: the `LivingDeathEvent` (LOWEST) confirmation becomes a `ServerPlayer.die` HEAD mixin
    calling `onConfirmedDeath`.
  - Persistent data: Forge's `PERSISTED_NBT_TAG` becomes port-only `objects/PersistentData` on
    the player (NBT key `EnigmaticLegacyPersisted`), copied with `ServerPlayerEvents.COPY_FROM`.
    `SuperpositionHandler.getRespawnPoint` and the persistent-data helpers are added.
  - `PermanentItemEntity` uses the vanilla entity spawn packet; `changeDimension` returns null
    (no portal travel, as upstream). Soul Crystal and `SoulArchive` parts are left as TODOs
    until those items are ported (`containsSoul` returns false).
  - `PacketHandleItemPickup` uses `PacketContext`; `ClientProxy.handleItemPickup` is inlined
    into its handler. `PermanentItemRenderer` and `PermanentItemPickupParticle` drop `@OnlyIn`.
    The empty `IPermanentCrystal` interface is copied for the renderer.
  - `EscapeScroll` drops `@OnlyIn` on its tooltip; `javax` `Nullable` becomes `org.jetbrains`.
