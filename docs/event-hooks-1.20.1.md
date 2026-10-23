# Event hooks plan (1.20.1 Fabric)

Upstream's `EnigmaticEventHandler` subscribes to about 45 Forge events. Fabric API covers
some of them. The rest need small mixins that fire our own events in
`com.aizistral.enigmaticlegacy.platform.event` (to be created), so the handler code stays
close to upstream. Mark each one "verify" until a devcheck or playtest confirms it.

The **Blocks** column lists the items that wait on each hook. Port the hooks in table order:
the first rows unblock the most items.

| Forge event | Fabric route | Blocks |
|---|---|---|
| LivingHurtEvent (4x, changes amount) | Mixin: `LivingEntity.actuallyHurt` `@ModifyVariable` on `amount` at HEAD (Forge fires it there, before armor) (done for Monster Charm; players use `Player.actuallyHurt` instead, done for Blazing Core; verify in game) | Monster Charm, Berserk Emblem, Angel's Blessing, Golem Heart, most curses |
| LivingDeathEvent (4x) | `ServerLivingEntityEvents.ALLOW_DEATH` (cancel; done for Heart of Creation) + `AFTER_DEATH`; player confirmation: `ServerPlayer.die` HEAD mixin (done for Escape Scroll) | Escape Scroll, Ring of the Seven Curses, Soul Crystal |
| LivingDropsEvent (2x) | Mixin: `LivingEntity.dropAllDeathLoot` HEAD/RETURN with `Entity.spawnAtLocation` RETURN recording the drops (done for Axe of Executioner; verify in game). Since the Escape Scroll, drops are captured (wrapped `addFreshEntity` in `spawnAtLocation` and `ServerPlayer.drop`) and spawned at RETURN, so handlers can add and remove them (done; verify in game) | Escape Scroll (postmortal possessions), Soul Crystal, Avarice Scroll |
| PlayerEvent.Clone / PlayerRespawnEvent | `ServerPlayerEvents.COPY_FROM` (done for persistent data) / `AFTER_RESPAWN` | Escape Scroll, Soul Crystal, keep-inventory items, Soul Compass (done), Eternal Binding armor restore (done) |
| LivingExperienceDropEvent | Mixin: `LivingEntity.dropExperience`, `@ModifyArg` on `ExperienceOrb.award` amount (done; verify) | Monster Charm (double XP), Avarice Scroll |
| LootingLevelEvent | Mixin: `EnchantmentHelper.getMobLooting` RETURN; add `getLootingLevel` to the accessory API (done; verify) | Monster Charm, Avarice Scroll |
| LivingDamageEvent | Mixin: `@WrapOperation` on `CombatTracker.recordDamage` in `actuallyHurt` of both `LivingEntity` and `Player` (Player overrides it); calls `onLivingDamage` | Cursed Ring (verify which items); done for Enigmatic Amulet (BLACK lifesteal) |
| LivingAttackEvent | `ServerLivingEntityEvents.ALLOW_DAMAGE` (already used; spellstone immunities done) | Angel's Blessing, Ocean Stone (Blazing Core, Animal Guidebook protection done) |
| LivingTickEvent | Mixin: `LivingEntity.tick` HEAD (done for Ender Slayer, `onLivingTick`; players: `ServerTickEvents` loop is cheaper) | Ocean Stone, Eye of Nebula, Heaven Scroll |
| LivingHealEvent | Mixin: `LivingEntity.heal` `@ModifyVariable` on amount | Cursed Ring, Forbidden Fruit |
| LivingKnockBackEvent | Mixin: `LivingEntity.knockback` HEAD | Golem Heart |
| LivingChangeTargetEvent | Mixin: `Mob.setTarget` HEAD (cancellable) | Ring of the Seven Curses (neutral mobs) |
| PlayerEvent.BreakSpeed / HarvestCheck | Mixin: `Player.getDestroySpeed` RETURN / `Player.hasCorrectToolForDrops` | Mining Charm, Etherium tools |
| AttackEntityEvent | `AttackEntityCallback` (done for Ender Slayer, `RegisteredMeleeAttack`) | Forbidden Axe, Berserk Emblem |
| ProjectileImpactEvent | Mixin: `@WrapWithCondition` on `onHit` in `tick` of `AbstractArrow`, `AbstractHurtingProjectile` and `ThrowableProjectile`; calls `onProjectileImpact` (true cancels) | Angel's Blessing (deflect); done for Enigmatic Amulet (VIOLET deflection) |
| EntityTeleportEvent.EnderPearl / EnderEntity | Mixin: `ThrownEnderpearl.onHit` / `EnderMan.teleport(DDD)Z` and `Shulker.teleportSomewhere` HEAD (done for Ender Slayer) | Eye of Nebula, curses |
| MobEffectEvent.Applicable | Mixin: `LivingEntity.canBeAffected` HEAD, cancellable (done for Heart of Creation) | Ocean Stone |
| MobSpawnEvent.FinalizeSpawn | Mixin: `Mob.finalizeSpawn` TAIL | curses (stronger mobs) |
| LootTableLoadEvent | `LootTableEvents.MODIFY` (End city done; verify in a chest) | Potion of Recall (End city), lore fragments, all loot injection |
| AnvilUpdateEvent | Mixin: `AnvilMenu.createResult` HEAD | Tome of Hungering Knowledge (upstream uses it; verify) |
| EnchantmentLevelSetEvent | Mixin: `EnchantmentMenu.slotsChanged` | Enchanter's Pearl |
| ItemCraftedEvent | Mixin: `ResultSlot.checkTakeAchievements` | Unholy Grail/curse checks (verify) |
| FurnaceFuelBurnTimeEvent | `FuelRegistry.INSTANCE.add` | none found yet (Blazing Core does not use it) |
| AdvancementEvent | Mixin: `PlayerAdvancements.award` RETURN | The Acknowledgment |
| RegisterCommandsEvent | `CommandRegistrationCallback` | commands |
| ServerStartedEvent | `ServerLifecycleEvents.SERVER_STARTED` | world data |
| PlayerLoggedInEvent | `ServerPlayConnectionEvents.JOIN` | config sync; done for Enigmatic Amulet (first-join gift, `onPlayerJoin`) |
| PlayerChangedDimensionEvent | `ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD` | Extradimensional Eye (verify), Soul Compass (done) |
| EntityJoinLevelEvent | `ServerEntityEvents.ENTITY_LOAD` | misc |
| Client: ItemTooltipEvent | `ItemTooltipCallback` | tooltips on vanilla items |
| Client: TickEvent.ClientTickEvent | `ClientTickEvents.END_CLIENT_TICK` (done for The Infinitum hold counters) | keybinds |
| Client: ScreenEvent.Init | `ScreenEvents.AFTER_INIT` | magnet toggle button (TransientPlayerData task) |
| Client: RenderTooltipEvent.Color | Mixin: `GuiGraphics.renderTooltipInternal` (verify) | colored tooltip borders |
| Client: RenderGuiOverlayEvent.Pre (experience bar) | Mixin: `Gui.renderExperienceBar` HEAD, cancellable (done for Blazing Core) | Blazing Core heat bar |
| Client: ViewportEvent.RenderFog | Mixin: `FogRenderer.setupFog` TAIL (done for Blazing Core) | Blazing Core, Molten Heart effect |
| Client: RenderGuiOverlayEvent.Pre | `HudRenderCallback` (cannot cancel vanilla; mixin if needed) | curse HUD |
| Client: RenderNameTagEvent, RenderCapeEvent | Mixins in `EntityRenderer` / `CapeLayer` | cosmetic, low priority |
| Forge item hooks: `canPerformAction(SHIELD_BLOCK)`, shield durability, axe disabling | Mixins: `LivingEntity.isDamageSourceBlocked` HEAD, `Player.disableShield` HEAD, `@ModifyExpressionValue` in `Player.hurtCurrentlyUsedShield`; the item implements `Equipable` (offhand) (done for the Bulwark of Blazing Pride) | Bulwark of Blazing Pride, Eldritch Pan |
| Mod-specific: SummonedEntityEvent, EnterBlockEvent, DropRulesEvent, EndPortalActivatedEvent, LivingGetProjectileEvent | Upstream's own events; port with their item | per item (DropRulesEvent: `AccessoryDrops` rule, done for Eternal Binding) |

## Suggested first step

1. Build `platform/event` with a tiny event bus (one functional interface + `Event<T>`
   per hook, using Fabric's `EventFactory`).
2. Add the first four rows: hurt amount, death, drops capture and clone/respawn.
3. Port Monster Charm and Escape Scroll on top of those hooks as the first test, then
   check both in the dev client (kill a zombie with and without the charm; die with
   the scroll).
