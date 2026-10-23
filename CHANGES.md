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
  Creation, Blazing Core, Scroll of Postmortal Recall, Etherium Ore and the Etherium tools and
  armor), registered in upstream order.
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
- `EnigmaticRecipes`: all 8 recipe serializers are ported, registered into vanilla's
  `RECIPE_SERIALIZER` registry.
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
  - Etherium Ore (weight 60, 1-2) is in. Lore Inscriber and the second End city pool
    (Corrupted Tome) came with later batches.
  - Vanilla 1.20.1 loot pools have no name, so `SuperpositionHandler.constructLootPool`
    ignores its `poolName` parameter.
  - The other dungeon pools came with later batches (spellstones, nether, The Acknowledgment and
    Revelation Tomes).
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
    `TierSortingRegistry` entries are dropped (see the Etherium batch).
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
    (`SuperpositionHandler.getNetherDungeons`); upstream's nether "epic" pool came later.
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
    (no portal travel, as upstream). The Soul Crystal and `SoulArchive` parts were filled in
    with those items (see below).
  - `PacketHandleItemPickup` uses `PacketContext`; `ClientProxy.handleItemPickup` is inlined
    into its handler. `PermanentItemRenderer` and `PermanentItemPickupParticle` drop `@OnlyIn`.
    The empty `IPermanentCrystal` interface is copied for the renderer.
  - `EscapeScroll` drops `@OnlyIn` on its tooltip; `javax` `Nullable` becomes `org.jetbrains`.
- Enigmatic Amulet (`EnigmaticAmulet`):
  - Attributes: Forge's `ENTITY_GRAVITY` and `SWIM_SPEED` become port-only
    `EnigmaticAttributes.GRAVITY` and `SWIM_SPEED`, added to players in `MixinPlayer.createAttributes`
    and applied in `LivingEntity.travel` (gravity constants 0.08/0.01 and the water
    `moveRelative` speed).
  - Events: Forge's `LivingDamageEvent` becomes a `CombatTracker.recordDamage` `@WrapOperation` in
    `actuallyHurt` of `LivingEntity` and `Player` (`onLivingDamage`, BLACK lifesteal);
    `ProjectileImpactEvent` becomes a `@WrapWithCondition` on `onHit` in the `tick` of
    `AbstractArrow`, `AbstractHurtingProjectile` and `ThrowableProjectile` (`onProjectileImpact`,
    VIOLET deflection), with `AccessorAbstractArrow` replacing `AccessorAbstractArrowEntity`;
    the first-join gift runs on `ServerPlayConnectionEvents.JOIN` (`onPlayerJoin`). Only the
    amulet's terms of these upstream handlers are ported (Angel's Blessing and The Cube
    deflection, Unwitnessed Amulet conversion and the Extradimensional Vessel
    with saved XP come with their items; until then the amulet drops like any other item on
    death, and its vessel tooltip and configs have no effect). Fishing hooks, llama spit,
    shulker bullets and firework rockets are not hooked yet, so they are never deflected.
  - `SuperpositionHandler.hasItem` reads `Inventory.items`, `armor` and `offhand`, because
    `compartments` is private on Fabric.
  - `PacketForceArrowRotations` (to client) is registered with `Services.NETWORK`. The color item
    property is registered in `EnigmaticLegacyFabricClient`; the world seed comes from
    `Services.PLATFORM.getServer()`; the Curios attribute-tooltip override is dropped and
    `curios.modifiers.charm` is added to the port's lang file.
- Potions batch (Ultimate and Common potions, normal/splash/lingering; Potion of Twisted Mercy,
  Bottle of Ichor, Unholy Grail, Forbidden Fruit, Redemption Potion, Celestial Fruit):
  - Effects `blazing_strength`, `molten_heart`, `growing_hunger` and `growing_bloodlust` are
    registered in the port's `EnigmaticEffects`. `MobEffectInstance.duration` is private on
    Fabric, so `AccessorMobEffectInstance` replaces Forge's access transformer. The Molten Heart
    lava fog reuses `BlazingCoreClient`.
  - Brewing: upstream's Forge `BrewingRecipeRegistry` calls go to the port's own brewing registry
    (`ComplexBrewingRecipe` added); `PotionUtils` stands in for `PotionHelper.createVanillaPotion`.
  - `EnigmaticPotionEntity` uses the vanilla spawn packet and entity builder (update interval 10).
    Fix: upstream's `isLingering` only checked the ultimate lingering potion, so common
    lingering potions splashed instead of leaving a cloud. Item colors use Fabric's
    `ColorProviderRegistry` instead of `RegisterColorHandlersEvent`.
  - `SuperpositionHandler.sendToDimension` and Twisted Potion use vanilla `teleportTo` instead of
    Forge's `changeDimension` with `RealSmoothTeleporter` (same result: position kept, no portal).
  - `unlockSpecialSlot` (Celestial Fruit's extra ring slot) goes through `Services.ACCESSORIES`
    instead of Curios' slot modifier.
  - Forbidden Fruit: the consumed flag lives in the persistent tag (`ConsumedForbiddenFruit`, kept
    on respawn) and is synced to the client with `PacketSyncForbiddenFruit` on join and on eating,
    instead of `TransientPlayerData`. Forge's `LivingHealEvent` becomes a server-side
    `@ModifyVariable` on `LivingEntity.heal`; the hunger-bar texture swap is a `@WrapOperation`
    on the food-bar `blit` calls in `Gui.renderPlayerHealth` (only the shank background is
    redrawn). Food is held at 20 with no saturation in the player tick.
  - Not ported yet: the Cursed Scroll heal boost in `onHeal`, Eldritch Pan's `cannotHunger` check, the Forbidden Fruit bastion loot, the
    Bottle of Ichor special loot and its `LootedIchorBottle` flag (its dungeon pool entries came with the spellstones batch). The Common Haste dungeon loot
    came with the Revelation Tomes.
- Spellstones and scrolls batch (Angel's Blessing, Heart of the Golem, Will of the Ocean, Eye of
  the Nebula, Pearl of the Void, The Cube; Gift of the Heaven, Fabulous Scroll, Scroll of a
  Thousand Curses, Scroll of Avarice, Cosmic Scroll):
  - Forge `LivingHurtEvent`/`LivingDeathEvent` parts go through `EnigmaticEventHandler`
    (called from `MixinLivingEntity`/`MixinPlayer` at the head of `actuallyHurt`). The Berserk
    Emblem and Hunter's Guidebook damage terms come with those items.
  - Spellstone loot pools (the earthen, air, water and ender structure groups) are built in `LootTableEvents.MODIFY`, with upstream's
    weights (Ichor Bottle included).
  - Curios' `getAttributesTooltip` override (hides attribute lines on Will of the Ocean, The Cube
    and Cosmic Scroll) becomes `IAccessoryItem.hidesAttributeTooltip`; `TrinketAdapter` returns
    no modifiers while `MixinItemStack` marks a tooltip pass, so Trinkets shows none.
  - Ender pearl self-damage is zeroed by `MixinThrownEnderPearl` (`@ModifyArg` on the hurt
    call) for wearers of the Eye of the Nebula or The Cube, instead of Forge's
    `EntityTeleportEvent.EnderPearl`.
  - The air bar is hidden by a `@WrapOperation` on its `blit` calls in `Gui.renderPlayerHealth`
    instead of `RenderGuiOverlayEvent` (Will of the Ocean, Pearl of the Void, The Cube).
  - Fabulous Scroll's beacon check: upstream walks `ChunkMap.visibleChunkMap`; the port scans
    loaded chunks within 4 chunks of the player (`isInBeaconRange`, beacon levels through
    `AccessorBeaconBlockEntity`). Beacon reach tops out at 50 blocks, so the result is the same.
  - Cosmic Scroll's extra scroll slot uses `AccessorySlots.addSlotModifier` (Trinkets) instead
    of Curios' slot modifier. Its tooltip lines come from Fabric's `ItemTooltipCallback`.
  - Eye of the Nebula's empowered-hit flag is held in the item (`EMPOWERED_PLAYERS`, cleared on
    server stop) instead of `TransientPlayerData`.
  - The Cube's hurt parts (35% reflect, then the damage-limit block that upstream runs at LOW
    priority) run in one `onLivingHurt` call, in the same order, after the other damage changes.
  - Heart of the Golem's armor exclusion list is built on server start instead of common setup.
  - New packets `PacketCosmicRevive`, `PacketPlayerMotion`, `PacketPlayerSetlook` use the port's
    network service.
  - Scroll of Avarice: the Forge `makesPiglinsNeutral` override is dropped; the Curios-style
    `IAccessoryItem.makesPiglinsNeutral` override stays. Its effects stay dormant until the Ring of Seven
    Curses is ported, as for every cursed-only item.
  - Not ported yet: Pearl of the Void's Animal Guidebook check (Tomes batch), and the Ring of
    Seven Curses part of `getCurseAmount`.
- Etherium batch (Etherium Ore, Pickaxe, Waraxe, Shovel, Broadsword, Scythe, Armor):
  - `EnigmaticMaterials.ETHERIUM` (level 5) is not registered with Forge's
    `TierSortingRegistry`. Vanilla 1.20.1 compares tiers by `getLevel()`, so no registry is
    needed. The Creation tier (6) comes with its item.
  - `EtheriumConfigHandler` is a static singleton (`instance()`) instead of an instance made in
    the mod constructor; `EnigmaticMaterials` and `EnigmaticArmorMaterials` read it lazily.
  - Tools: Forge's `ToolActions`/`canPerformAction` overrides are dropped (vanilla item classes
    already give the strip/flatten/till actions). Harvest checks use vanilla
    `isCorrectToolForDrops(BlockState)`.
  - `AOEMiningHelper`: the AOE methods take the tool and a damage consumer directly;
    `attemptBreakNeighbors` (unused upstream) is dropped. Block XP is dropped by vanilla
    `Block.dropResources` instead of Forge's `getExpDrop`.
  - Etherium Armor shield: upstream's `EtheriumEventHandler` subscribes to Forge's
    `LivingHurtEvent`/`LivingAttackEvent`. The port calls its static methods from
    `EnigmaticEventHandler.onLivingAttack` (projectile deflection) and at the end of
    `onLivingHurt` (knockback and damage reduction). The standalone End city loot pool is
    dropped (`isStandalone()` is always false inside Enigmatic Legacy). The unused cooldown
    map field is dropped.
  - Armor rendering: upstream's armor textures are fully transparent, so the armor is
    invisible. The port registers a no-op Fabric `ArmorRenderer` for the four pieces, which
    gives the same result and avoids vanilla's texture lookup on the namespaced material name.
  - `ShieldAuraLayer` is added to player renderers through Fabric's
    `LivingEntityFeatureRendererRegistrationCallback` instead of `EntityRenderersEvent.AddLayers`.
- Astral Breaker, Keystone of the Oblivion (Void Stone) and Curse Transposer batch:
  - `ItemBaseTool` and `AstralBreaker`: Forge's `ToolAction` set and `canPerformAction`
    override are dropped (no Fabric equivalent). Mining speed and harvest checks still come
    from the block tags, as upstream.
  - `PacketFlameParticles` uses the port's packet context instead of Forge's
    `NetworkEvent.Context`; it is registered in `EnigmaticLegacyFabric`.
  - `OblivionStone`, `OblivionStoneCombineRecipe`: `ForgeRegistries.ITEMS` ->
    `BuiltInRegistries.ITEM`. When the item list hits the hardcap, the combine recipe returns
    an empty stack instead of upstream's `null`, which would crash.
  - Crafting sounds (Curse/Enchantment Transposer, Cursed Stone): upstream listens to Forge's
    `PlayerEvent.ItemCraftedEvent`. The port's `MixinResultSlot` calls
    `EnigmaticEventHandler.onItemCrafted` from the head of `ResultSlot.checkTakeAchievements`,
    where Forge fires that event.
  - Nether fortress and bastion chest loot: upstream's "epic" pool (golden tools, Void Stone,
    Forbidden Fruit in bastions, and others) and the Bastion Treasure "darkest_scroll" pool are
    ported. Upstream's "netherLiterature" pool (Withered Tome) came with the Revelation Tomes.
  - `SuperpositionHandler`: `itemEntryBuilderED` and `getBastionChests` added, same as upstream.
- The Architect's Inkwell (Lore Inscriber):
  - New port registry `EnigmaticMenus` registers `lore_inscriber_container` as a vanilla
    `MenuType` (upstream uses Forge's `IForgeMenuType.create`; the menu sends no extra data).
  - `LoreInscriberScreen` is registered with `MenuScreens.register` in the Fabric client
    initializer. It sends the text field through `Services.NETWORK` instead of upstream's
    `EnigmaticLegacy.packetInstance`; `@OnlyIn` removed.
  - `PacketInkwellField` uses the port's packet context; registered in `EnigmaticLegacyFabric`.
  - `LoreInscriber`, `LoreInscriberContainer`: javax `@Nullable` -> jetbrains; logic unchanged.
- Extradimensional Vessel (Storage Crystal) and Soul Crystal:
  - `SoulArchive`: the save file is still `soul_archive.json` in the world folder, but each
    record stores the dimension as a string and the position as plain `x`/`y`/`z`. Upstream let
    Gson write `ResourceLocation` and `BlockPos` by reflection, which gives obfuscated field
    names outside the dev environment. File access uses `java.nio.file.Files` instead of
    commons-io. The archive is created on Fabric's `SERVER_STARTING`.
    If the file cannot be read (corrupted, or written by upstream in its Forge format), it is
    renamed to `soul_archive.json.invalid` and the archive starts empty. Upstream threw an
    exception, which stopped the server from starting.
  - `StorageCrystal`: Forge's `serializeNBT()` -> `ItemStack.save()`. Upstream called
    `deserializeNBT()` on an empty tag when embedding a soul, which stored an empty compound;
    the port saves the soul crystal stack instead. Only the tag's presence is read, so
    behavior is the same.
  - `SoulCrystal`, `PermanentItemEntity`: Forge's `PacketDistributor.NEAR` -> the port's
    `Services.NETWORK.sendToNear`. `PermanentItemEntity.remove()` only touches the archive on
    the logical server (null check added).
  - The lost-soul health penalty is reapplied on join and on Fabric's
    `ServerPlayerEvents.AFTER_RESPAWN` (upstream: `PlayerEvent.Clone`).
  - Not ported yet: the permadeath screen and "fractured world" flag at 10 lost crystals
    (`PacketPermadeath`, `EnigmaticTransience`), Soul Compass updates, and the Cursed Ring
    check in soul-loss mode 0 (treated as "no ring" for now).
- The Acknowledgment and the Revelation Tomes (Tattered, Withered, Corrupted):
  - `TheAcknowledgment`: vanilla `getEnchantmentValue()` and `getDefaultAttributeModifiers(slot)`
    instead of Forge's stack-aware overloads. Forge's `isBookEnchantable` and
    `canApplyAtEnchantingTable` become port-only mixins: `MixinEnchantment` (`canEnchant`, used by
    the anvil) and `MixinEnchantmentHelper` (`getAvailableEnchantmentResults`, the enchanting
    table) allow Bane of Arthropods. The `allowAllEnchantments` branch is not hooked; no item
    in the mod sets it.
  - Arthropods ignore and avoid Acknowledgment holders: port-only `MixinMob` cancels
    `setTarget` (upstream: `LivingChangeTargetEvent`), and the avoid goal is added from Fabric's
    `ServerEntityEvents.ENTITY_LOAD` (upstream: `EntityJoinLevelEvent`) through a port-only
    `AccessorMob` (Forge's access transformer makes `goalSelector` public).
  - `RevelationTome`: the 400-tick burn time is a Fabric `FuelRegistry` entry; the burnt
    advancement trigger fires from a port-only `MixinAbstractFurnaceBlockEntity` on
    `getBurnDuration` (upstream: `FurnaceFuelBurnTimeEvent`). The toast moved to the client-only
    `RevelationTomeClient` (upstream: `ClientProxy`). The tooltip checks for a null client player.
  - Loot: the literature pools (End, nether, overworld, libraries), the overworld "epic" pool
    (with Common Haste potion and Lore Inscriber), the library "el_special" pool and the
    underwater ruin trident are ported with upstream's weights. Upstream's Desert Pyramid branch
    is left out: it follows the overworld branch in an else-if chain and never runs.
  - Dev only: `DevSelfCheck` now expects 6 loot pools and checks the new mixins.
- Relic of Testing and Ring of the Seven Curses (`CursedRing`):
  - `RelicOfTesting`: the TransientPlayerData (narrator unlock) reset is left out until that
    system is ported. Mob goals are reached through `AccessorMob`.
  - `CursedRing`: Curios slot context and drop rule go through the accessory service (Trinkets).
    Enderman teleporting and piglin anger use `AccessorEnderMan` and `AccessorPiglinAi`. The Animal Guidebook checks are left out until
    that item is ported.
  - Forge's `isBookEnchantable` (no Curse of Vanishing from books) is port-only
    `MixinEnchantment.canEnchant`, so an anvil drops only that enchantment instead of refusing
    the whole book.
  - The enchanting table bonus (Forge's EnchantmentLevelSetEvent) is port-only
    `MixinEnchantmentMenu` (works out the bonus around `slotsChanged`) plus a
    `MixinEnchantmentHelper.getEnchantmentCost` inject.
  - Knockback debuff: port-only `@ModifyVariable` at the head of `LivingEntity.knockback`, where
    Forge fires LivingKnockBackEvent. Experience bonus: `EnigmaticEventHandler.onExperienceDrop`,
    which also applies the Monster Charm bonus. Insomnia uses `AccessorPlayer` for the sleep
    counter.
  - Auto-equip runs in the player tick (upstream: PlayerTickEvent inventory loop), main inventory
    only.
  - Special drops are split out of `onLivingDrops` into `addCursedRingDrops`. Left out until
    their items are ported: the Guardian Heart choice from Elder Guardians and the Abyssal Heart
    drop from the Ender Dragon.
  - Monster damage debuff: upstream's Twist, Infinitum and Eldritch Pan exemptions are dropped
    (items not ported). Also not ported yet: Unholy Stone lava, the
    KNOCKBACK_THAT_BASTARD flag, the playtime counter (IPlaytimeCounter), harvest checks and the
    cursed main menu flag (`setCurrentWorldCursed` is a no-op).
- Twisted Mirror and Emblem of Bloodstained Valor (`BerserkEmblem`):
  - `TwistedMirror`: `EnigmaticLegacy.PROXY.isInVanillaDimension` is replaced with
    `SuperpositionHandler.isInVanillaDimension`. Upstream's commented-out use-duration variant is
    not carried over. The 400-tick mirror cooldown that Ender Slayer puts on players it hits
    is in `EnigmaticEventHandler.onLivingHurt`.
  - `BerserkEmblem`: Curios slot hooks go through the accessory service (Trinkets). Its damage
    boost and resistance terms are in `EnigmaticEventHandler.onLivingHurt` and `onPlayerHurt`.
    `SuperpositionHandler.getMissingHealthPool` is ported with it.
  - `GuardianHeart`: the guardian-curse particle packet is `PacketGenericParticleEffect`, sent
    through the network service. Piglin anger calls go through invoker mixins (`AccessorPiglinAi`,
    new `AccessorPiglinBruteAi`) instead of access transformers. `SuperpositionHandler`
    `getClosestEntity`, `doesObserveEntity` and `getRandomNegative` are ported with it. Upstream's
    `LivingChangeTargetEvent` Guardian check lives in `MixinMob` (head of `Mob.setTarget`). The
    Elder Guardian drop pool includes the Heart again.
  - `TheTwist`: the knockback bonus and the cancel for non-cursed wielders are in
    `EnigmaticEventHandler.onLivingAttack` (Fabric `ALLOW_DAMAGE`; upstream cancels in
    `LivingHurtEvent`). The boss bonus and the Cursed Ring debuff bypass are in `onLivingHurt`.
    `KNOCKBACK_THAT_BASTARD` and `AGERED_GUARDIANS` are cleared on server stop, as upstream's
    `performCleanup` does.
  - `UnwitnessedAmulet`: the swap to a bound Enigmatic Amulet runs in
    `EnigmaticEventHandler.onPlayerTick` (server tick), as in upstream's `PlayerTickEvent`. The
    `getAttributesTooltip` override returned the list unchanged, so it is dropped. The loop walks
    `Inventory.items`, `armor` and `offhand` directly, since `compartments` is private on Fabric.
  - `PetGuidebook` (Guide to Animal Companionship), `HunterGuidebook` (Guide to Feral Hunt):
    `@OnlyIn` removed; `ForgeRegistries.ENTITY_TYPES` -> `BuiltInRegistries.ENTITY_TYPE`. The
    Hunter's Guide pet-damage redirect runs in `EnigmaticEventHandler.onLivingHurt` and returns 0
    instead of `event.setCanceled(true)`. Upstream's `LivingAttackEvent` check that stops the
    player hitting protected animals runs at the end of `EnigmaticEventHandler.onLivingAttack`
    (`ALLOW_DAMAGE`); a local flag stands in for upstream's cancel/un-cancel sequence. It reads
    `Mob.targetSelector` and `TargetGoal.targetMob` through `AccessorMob` and the new
    `AccessorTargetGoal` (Forge's access transformer made them public). `MixinHoglinTasks` is
    copied unchanged. The Cursed Ring and Void Pearl guidebook checks are restored.
  - `EnchanterPearl` (Enchanter's Pearl): `@OnlyIn` removed; the charm slot bonus goes through
    `Services.ACCESSORIES.addSlotModifier`; the `getAttributesTooltip` override is dropped.
    Upstream's `MixinEnchantmentContainer` is merged into `MixinEnchantmentMenu`. Its
    `clickMenuButton` inject used `@At("INVOKE")` with no target; the port uses `HEAD` plus a
    button-index bounds check. The `getGoldCount` inject (64 lapis) was client-only upstream and
    runs on both sides here. `SuperpositionHandler.mergeEnchantments` is ported; the Eternal
    Binding roll is skipped until that enchantment is ported.
  - `DesolationRing` (The Burden of Desolation): `@OnlyIn` removed; Curios `SlotContext` and
    `DropRule` -> platform equivalents. Forge's `MobSpawnEvent.FinalizeSpawn` handler is replaced
    by the port-only `MixinNaturalSpawner` (HEAD of `isValidPositionForMob`), which refuses
    natural spawns of piglins, zombified piglins, iron golems and endermen inside a desolation
    box. Upstream also cleared `DESOLATION_BOXES` on server stop; the port relies on its
    `WeakHashMap` keys and the per-tick removal. The death-handler lava-pool destruction is
    ported separately (see Cursed Ring lava-pool destruction below).
  - `EnderSlayer` (The Ender Slayer): `@OnlyIn` removed. The Slayer and Torrent enchantment
    terms are dropped (enchantments not ported). Forge's `getPersistentData()` on mobs is the
    port-only `EntityPersistentData`, kept by `MixinEntity` and saved under
    `"enigmaticlegacy:data"`. Forge's `EntityTeleportEvent.EnderEntity` is replaced by HEAD
    mixins on `EnderMan.teleport(DDD)Z` (port-only `MixinEnderMan`) and
    `Shulker.teleportSomewhere` (`MixinShulker`, reading `EntityPersistentData`). The
    `ELTeleportBlock` countdown runs from a `LivingEntity.tick` HEAD inject
    (`EnigmaticEventHandler.onLivingTick`, Forge's `LivingTickEvent`). `RegisteredMeleeAttack`
    is fed by `AttackEntityCallback` and drops `ticksSinceLastSwing`; its registry is cleared in
    `onServerStopped`. The hit cancel for non-cursed holders is in `ALLOW_DAMAGE`; the End XP
    conversion clears the drop list instead of cancelling the drops event.
  - `SoulCompass` (Wayfinder of the Damned): `@OnlyIn` removed; `registerVariants` runs from
    `EnigmaticLegacyFabricClient`. `SuperpositionHandler.hasExactStack` checks the three
    inventory lists instead of Forge's `compartments`. `updateSoulCompass` sends
    `PacketUpdateCompass` through the platform network. `SoulArchive.synchronize` keeps the
    server from `initialize` instead of `ServerLifecycleHooks`, and keeps upstream's `hasCurio`
    check. `LAST_SOUL_COMPASS_UPDATE` is cleared on respawn (`AFTER_RESPAWN`), dimension change
    (`AFTER_PLAYER_CHANGE_WORLD`) and server stop.
  - Curse of Eternal Binding (`EternalBindingCurse`, `EnigmaticEnchantments`): Forge's
    `isAllowedOnBooks` is dropped (no vanilla equivalent). Upstream's `hasBindingCurse` inject is
    in `MixinEnchantmentHelper`; the Enchanter Pearl roll (`maybeApplyEternalBinding`) runs from
    `MixinEnchantmentMenu`. Bound armor is stored in `onConfirmedDeath` and restored by
    `EnigmaticEventHandler.onPlayerClone`, called from `COPY_FROM` after the persistent data
    copy. Curios' `DropRulesEvent` becomes an `AccessoryDrops` rule (`ALWAYS_KEEP`). Port
    addition: `TrinketAdapter.canUnequip` refuses items with a binding curse outside creative,
    because Trinkets (unlike Curios' slot) does not check it; this also covers vanilla Binding.
    The Eldritch Amulet branch came with that item.
  - Cursed Ring lava-pool destruction: `onConfirmedDeath` now records and consumes the Unholy
    Stone (Forge's private `Inventory.compartments` -> the items, armor and offhand lists). The
    lava-pool part of upstream's `onCurioDrops` (Curios `DropRulesEvent`) moves to
    `destroyRingsInLavaPool`, run once at the end of `onConfirmedDeath`, because Trinkets asks for
    drop rules once per stack. It empties the ring slots before the drops instead of adding a
    DESTROY override; `SuperpositionHandler.destroyCurio` clears every matching slot (upstream's
    Curios loop skipped the last slot). `Level.NETHER` replaces the proxy's Nether key. The
    `DestroyedCursedRing` flag is stored, but the respawn quote (`Quote.RING_DESTRUCTION`) is a
    TODO until quotes are ported.
  - Bulwark of Blazing Pride (`InfernalShield`): Forge's `canPerformAction(SHIELD_BLOCK)` has no
    Fabric equivalent; vanilla blocking only needs `UseAnim.BLOCK`. The item implements
    `Equipable` (offhand), because Forge sends shield-capable items to the offhand for mobs and
    dispensers and vanilla does not. Upstream's `disableShield` mixin is in `MixinPlayer`, which
    also makes `hurtCurrentlyUsedShield` damage the Bulwark (vanilla only damages
    `Items.SHIELD`, Forge any shield). Upstream's `isDamageSourceBlocked` mixin is in
    `MixinLivingEntity`; `IProperShieldUser.isActuallyReallyBlocking` becomes
    `SuperpositionHandler.isActuallyReallyBlocking`, and `onDamageSourceBlocking` drops the
    unused callback parameter, the piercing-arrow check (forced off upstream) and the Eldritch
    Pan branch (not ported yet). The Unbreaking `canApplyAtEnchantingTable` override is dropped
    (vanilla's BREAKABLE category already allows it); `getEnchantmentValue` uses vanilla's
    signature without a stack. The `blocking` item property is registered in
    `EnigmaticLegacyFabricClient`. Blazing Strength removal and the x1.5 damage when hit from
    behind run in `onPlayerHurt` (upstream's `LivingHurtEvent`).
  - `TheInfinitum` (The Infinitum): `@OnlyIn` removed. Upstream's `LivingHurtEvent` cancel for
    unworthy wielders, with its six debuffs, is in `ALLOW_DAMAGE` like The Twist's. The death
    save checks only the main hand: upstream checks the main hand twice (probably meant the
    offhand), so the behavior is the same. Lifesteal stays a flat 10% as upstream, which does
    not read the `lifestealBonus` config. The `the_infinitum_open` item property and its hold
    counters move from upstream's `ClientProxy` to port-only `client/InfinitumClient`, advanced
    from `END_CLIENT_TICK` once per world tick instead of a client `LivingTickEvent`, and
    cleared on disconnect. The Acknowledgment's custom-name rendering for it is not ported.
  - `AscensionAmulet` (Amulet of Ascension): upstream's `getAttributesTooltip` override is
    dropped (the port's accessory tooltips do not list modifiers). `EnigmaticAmulet.hasColor`
    returns true for every color while it is worn. `onConfirmedDeath` records it (worn or
    carried) in `POSTMORTAL_POSESSIONS`, and the Extradimensional Vessel counts it like the
    Enigmatic Amulet (`hadEnigmaticAmulet`).
  - `EldritchAmulet` (The Testament of Contempt): `storeInventory` saves stacks with vanilla
    `ItemStack.save` instead of Forge's `serializeNBT`; upstream's `getAttributesTooltip`
    override is dropped. For a worthy wearer, `onConfirmedDeath` stores the main inventory,
    armor and offhand (minus Curse of Vanishing items) instead of the Eternal Binding armor,
    and `onPlayerClone` restores them and skips the bound-armor restore. Curios'
    `DropRulesEvent` becomes an `AccessoryDrops` rule that keeps every accessory without Curse
    of Vanishing. The 15% lifesteal is in `onLivingDamage`.
- `HiddenRecipe` (`crafting_shaped_hidden`: The Cube, Amulet of Ascension, The Testament of
  Contempt, Cosmic Scroll): upstream adds the four recipes in `onLoadComplete`; the port adds
  them in `EnigmaticRecipes.registerHiddenRecipes`, called from the Fabric setup. `assemble`
  skips the amulet NBT copy when the input amulet has no NBT (upstream would crash). `getRecipe`
  uses `BuiltInRegistries.ITEM` instead of `ForgeRegistries.ITEMS`. `HiddenRecipeProcessor`
  (The Acknowledgment's hidden-recipe pages) is copied unchanged.
- `EnigmaticElytra` (Majestic Elytra): Fabric API's `EntityElytraEvents.CUSTOM` replaces Forge's
  `canElytraFly`/`elytraFlightTick` and Caelus' flight attribute; it accepts the elytra in the
  chest slot or the back accessory slot. The item implements `Equipable` for the chest slot and
  equip sound. Upstream's `TransientPlayerData` boost flag becomes `ElytraBoostData` (port-only),
  and the new `PacketSyncElytraBoosting` sends it to tracking players and the player, so others
  see the boost particles. The client boost goes through a static hook set by
  `EnigmaticElytraClient` instead of the PROXY. The server boost runs from the end-of-tick player
  loop (`onPlayerTick`); upstream ran it at PlayerTickEvent START. A `CapeLayer` mixin replaces
  Caelus' `RenderCapeEvent`, and `EnigmaticElytraLayer` is added to players and armor stands with
  `LivingEntityFeatureRendererRegistrationCallback`.
- `TheJudgement` (The Judgement): copied unchanged except Forge's `@OnlyIn` client markers.
- Quote system (`Quote`, `Subtitles`, `QuoteHandler`, `PacketPlayQuote`): the narrator flag from
  upstream's `TransientPlayerData` becomes `NarratorData` (port-only, persistent boolean
  `ELUnlockedNarrator`). `QuoteHandler` hooks Fabric's client tick, HUD and screen-render events
  instead of Forge's PlayerTickEvent, RenderGuiEvent and ScreenEvent; it counts the delay down
  twice per client tick, because upstream's PlayerTickEvent fired at START and END. The subtitle
  box uses `GuiGraphics.fill` (upstream's gradient rect used two equal colors), and
  `wrapString`/`greatestWidth` moved from `SuperpositionHandler` into `QuoteHandler` to keep
  client classes out of common code. Quote sounds register through the vanilla registry
  (`SuperpositionHandler.registerSound`); `EnigmaticSounds.init` loads `Quote` so they register
  in time.
  - Upstream's custom Forge events become three port-only mixins that call
    `EnigmaticEventHandler` directly: `MixinEnderEyeItem` (eye placed in an End portal frame
    that completes it), `MixinSummonedEntityTrigger` (Wither/Ender Dragon summoned) and
    `MixinEnterBlockTrigger` (entering an End gateway).
  - Respawn quotes run from Fabric's AFTER_RESPAWN (its `alive` flag equals upstream's
    `isEndConquered`); Nether/End arrival quotes from AFTER_PLAYER_CHANGE_WORLD. The Wither kill
    counter quotes, the Ender Dragon hit quote (Monster Charm hurt hook, worn or not, as
    upstream) and the Relic of Testing narrator reset are wired in.
  - `WITH_DRAGONS` is not wired yet: it plays after the Abyssal Heart drop, which is not ported.
