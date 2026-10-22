# Event hooks plan (1.20.1 Fabric)

Upstream's `EnigmaticEventHandler` subscribes to about 45 Forge events. Fabric API covers
some of them. The rest need small mixins that fire our own events in
`com.aizistral.enigmaticlegacy.platform.event` (to be created), so the handler code stays
close to upstream. Mark each one "verify" until a devcheck or playtest confirms it.

The **Blocks** column lists the items that wait on each hook. Port the hooks in table order:
the first rows unblock the most items.

| Forge event | Fabric route | Blocks |
|---|---|---|
| LivingHurtEvent (4x, changes amount) | Mixin: `LivingEntity.actuallyHurt` `@ModifyVariable` on `amount` at HEAD (Forge fires it there, before armor) (done for Monster Charm; verify in game) | Monster Charm, Berserk Emblem, Angel's Blessing, Golem Heart, most curses |
| LivingDeathEvent (4x) | `ServerLivingEntityEvents.ALLOW_DEATH` (cancel) + `AFTER_DEATH` | Escape Scroll, Ring of the Seven Curses, Soul Crystal |
| LivingDropsEvent (2x) | Mixin: `LivingEntity.dropAllDeathLoot`, collect the drops into a list (Forge's capture) and fire the event | Escape Scroll (postmortal possessions), Soul Crystal, Avarice Scroll |
| PlayerEvent.Clone / PlayerRespawnEvent | `ServerPlayerEvents.COPY_FROM` / `AFTER_RESPAWN` | Escape Scroll, Soul Crystal, keep-inventory items |
| LivingExperienceDropEvent | Mixin: `LivingEntity.dropExperience`, `@ModifyArg` on `ExperienceOrb.award` amount (done; verify) | Monster Charm (double XP), Avarice Scroll |
| LootingLevelEvent | Mixin: `EnchantmentHelper.getMobLooting` RETURN; add `getLootingLevel` to the accessory API (done; verify) | Monster Charm, Avarice Scroll |
| LivingDamageEvent | Mixin: `LivingEntity.actuallyHurt` after armor/absorption (`@ModifyVariable` at `setHealth`) | Cursed Ring (verify which items) |
| LivingAttackEvent | `ServerLivingEntityEvents.ALLOW_DAMAGE` (already used) | Angel's Blessing, Ocean Stone, Blazing Core |
| LivingTickEvent | Mixin: `LivingEntity.tick` TAIL (players: `ServerTickEvents` loop is cheaper) | Ocean Stone, Eye of Nebula, Heaven Scroll |
| LivingHealEvent | Mixin: `LivingEntity.heal` `@ModifyVariable` on amount | Cursed Ring, Forbidden Fruit |
| LivingKnockBackEvent | Mixin: `LivingEntity.knockback` HEAD | Golem Heart |
| LivingChangeTargetEvent | Mixin: `Mob.setTarget` HEAD (cancellable) | Ring of the Seven Curses (neutral mobs) |
| PlayerEvent.BreakSpeed / HarvestCheck | Mixin: `Player.getDestroySpeed` RETURN / `Player.hasCorrectToolForDrops` | Mining Charm, Etherium tools |
| AttackEntityEvent | `AttackEntityCallback` | Forbidden Axe, Berserk Emblem |
| ProjectileImpactEvent | Mixin: `Projectile.onHit` HEAD | Angel's Blessing (deflect) |
| EntityTeleportEvent.EnderPearl / EnderEntity | Mixin: `ThrownEnderpearl.onHit` / `EnderMan.teleport` | Eye of Nebula, curses |
| MobEffectEvent.Applicable | Mixin: `LivingEntity.canBeAffected` RETURN | Blazing Core, Ocean Stone |
| MobSpawnEvent.FinalizeSpawn | Mixin: `Mob.finalizeSpawn` TAIL | curses (stronger mobs) |
| LootTableLoadEvent | `LootTableEvents.MODIFY` | Potion of Recall (End city), lore fragments, all loot injection |
| AnvilUpdateEvent | Mixin: `AnvilMenu.createResult` HEAD | Tome of Hungering Knowledge (upstream uses it; verify) |
| EnchantmentLevelSetEvent | Mixin: `EnchantmentMenu.slotsChanged` | Enchanter's Pearl |
| ItemCraftedEvent | Mixin: `ResultSlot.checkTakeAchievements` | Unholy Grail/curse checks (verify) |
| FurnaceFuelBurnTimeEvent | `FuelRegistry.INSTANCE.add` | Blazing Core? (verify) |
| AdvancementEvent | Mixin: `PlayerAdvancements.award` RETURN | The Acknowledgment |
| RegisterCommandsEvent | `CommandRegistrationCallback` | commands |
| ServerStartedEvent | `ServerLifecycleEvents.SERVER_STARTED` | world data |
| PlayerLoggedInEvent | `ServerPlayConnectionEvents.JOIN` | config sync |
| PlayerChangedDimensionEvent | `ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD` | Extradimensional Eye (verify) |
| EntityJoinLevelEvent | `ServerEntityEvents.ENTITY_LOAD` | misc |
| Client: ItemTooltipEvent | `ItemTooltipCallback` | tooltips on vanilla items |
| Client: TickEvent.ClientTickEvent | `ClientTickEvents.END_CLIENT_TICK` | keybinds |
| Client: ScreenEvent.Init | `ScreenEvents.AFTER_INIT` | magnet toggle button (TransientPlayerData task) |
| Client: RenderTooltipEvent.Color | Mixin: `GuiGraphics.renderTooltipInternal` (verify) | colored tooltip borders |
| Client: RenderGuiOverlayEvent.Pre | `HudRenderCallback` (cannot cancel vanilla; mixin if needed) | curse HUD |
| Client: RenderNameTagEvent, RenderCapeEvent | Mixins in `EntityRenderer` / `CapeLayer` | cosmetic, low priority |
| Mod-specific: SummonedEntityEvent, EnterBlockEvent, DropRulesEvent, EndPortalActivatedEvent, LivingGetProjectileEvent | Upstream's own events; port with their item | per item |

## Suggested first step

1. Build `platform/event` with a tiny event bus (one functional interface + `Event<T>`
   per hook, using Fabric's `EventFactory`).
2. Add the first four rows: hurt amount, death, drops capture and clone/respawn.
3. Port Monster Charm and Escape Scroll on top of those hooks as the first test, then
   check both in the dev client (kill a zombie with and without the charm; die with
   the scroll).
