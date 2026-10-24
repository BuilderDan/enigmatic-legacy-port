# Playtest: 1.20.1 Fabric

A version counts as done only after a dev-client playtest of every item. The
automated playtest (notes below) covers the core effects, tooltips, keybinds and a
dedicated server; Dan judges only look and feel on a review sheet. The DevSelfCheck
below is a smoke test only.

## Automatic smoke test (DevSelfCheck)

```bash
cd 1.20.1
./gradlew runClient --offline -Pdevcheck=exit
```

At the title screen it builds the main creative tab and checks each stack's
name, model and tooltip. Results go to the log with a `[devcheck]` prefix. With
`-Pdevcheck=true`, the client stays open after the check instead of closing.

| Date | Items in tab | Registered | Problems | Notes |
|---|---|---|---|---|
| 2026-10-21 | 10 | 10 | 0 | Only log errors: the `alt textures` path (renamed afterwards) and a Realms auth failure, which is normal in dev. |
| 2026-10-21 | 16 | 16 | 0 | Added Mending Mixture, Iron Ring and the 4 Etherium materials. Binding-curse mixin applied cleanly. |
| 2026-10-21 | 18 | 18 | 0 | Added Magnetic Ring and Dislocation Ring. Config keys `MagnetRingRange`=8, `SuperMagnetRingRange`=16 written. |
| 2026-10-21 | 20 | 20 | 0 | Added Tome of Hungering Knowledge, Heart of the Cosmos and 6 recipe serializers. Recipe pass: 32 parsed, 50 skipped by conditions, 11 pending (unported content), 0 failed. |
| 2026-10-21 | 22 | 22 | 0 | Added Exquisite Ring (piglin mixin check ok) and Heart of the Abyss. Recipe pass: 33 parsed, 49 skipped by conditions, 11 pending (unported content), 0 failed. |
| 2026-10-21 | 23 | 23 | 0 | Added Extradimensional Eye. Recipe pass: 34 parsed, 48 skipped by conditions, 11 pending (unported content), 0 failed. |
| 2026-10-21 | 24 | 24 | 0 | Added Potion of Recall. Recipe pass: 34 parsed, 48 skipped by conditions, 11 pending (unported content), 0 failed. |
| 2026-10-22 | 26 | 26 | 0 | Added Scroll of Ageless Wisdom; tooltip shows the J keybind. Sound registry loaded. |
| 2026-10-22 | 27 | 27 | 0 | Added Ring of Ender; I keybind and server packet registered. |
| 2026-10-22 | 29 | 29 | 0 | Added Essence of Raging Life (infinimeal) and Astral Potato. |
| 2026-10-22 | 30 | 30 | 0 | Added Charm of Treasure Hunter (mining_charm); 6 new mixin targets and the block reach attribute checked. |
| 2026-10-22 | 31 | 31 | 0 | Added Axe of Executioner (forbidden_axe); drop-capture mixins on `Entity` and `LivingEntity` checked. |
| 2026-10-22 | 72 | 73 | 0 | Added Astral Breaker, Void Stone and Curse Transposer. `void_stone_combine` recipe ok; recipe pass: 61 parsed, 25 skipped by conditions, 7 pending, 0 failed. `ResultSlot` crafting mixin checked. |
| 2026-10-22 | 73 | 74 | 0 | Added The Architect's Inkwell (lore_inscriber): menu type registered, screen and `PacketInkwellField` load. Recipe pass: 62 parsed, 24 skipped by conditions, 7 pending, 0 failed. GUI itself still needs an in-world playtest. |
| 2026-10-22 | 74 | 76 | 0 | Added Extradimensional Vessel (storage_crystal, hidden from the tab) and Soul Crystal. |
| 2026-10-22 | 78 | 80 | 0 | Added The Acknowledgment and the Tattered, Withered and Corrupted Tomes. Loot check now builds 6 pools (literature and overworld epic pools added). New mixins on `Mob`, `Enchantment`, `EnchantmentHelper` (Bane) and `AbstractFurnaceBlockEntity` checked. Recipe pass: 66 parsed, 23 skipped by conditions, 4 pending, 0 failed. |
| 2026-10-22 | 79 | 82 | 0 | Added Relic of Testing (hidden from the tab) and Ring of the Seven Curses. New mixins on `EnchantmentMenu`, `EnchantmentHelper` (enchanting bonus), `Enchantment` (vanishing rule) and `LivingEntity` (knockback) checked, plus the `Player` and `EnderMan` accessors. `relic_of_testing` has no item model, same as upstream. Recipe pass: 66 parsed, 23 skipped by conditions, 4 pending, 0 failed. |
| 2026-10-22 | 81 | 84 | 0 | Added Twisted Mirror and Emblem of Bloodstained Valor (berserk_charm). Recipe pass: 68 parsed, 21 skipped by conditions, 4 pending, 0 failed. Berserk scaling and the mirror teleport still need an in-world playtest (both need the Seven Curses ring worn). |
| 2026-10-22 | 84 | 86 | 0 | Added Heart of the Guardian and The Twist; all mixins ok. Needs an in-world playtest with the Seven Curses ring worn: guardian aggro, mob-debuff pulse, Twist knockback and boss bonus, and the non-cursed hit cancel. |
| 2026-10-22 | 86 | 87 | 0 | Added Unwitnessed Amulet (cannot be equipped). In-world check: picking it up should turn it into an Enigmatic Amulet inscribed with the player name and a random (or seeded) color. |
| 2026-10-22 | 87 | 88 | 0 | Added Amulet of Deception (cursed curio, tooltip only; upstream has no behavior for it, name still says [WIP] as upstream). |
| 2026-10-22 | 88 | 90 | 0 | Added Guide to Animal Companionship and Guide to Feral Hunt (Cursed Ring neutral-anger skip, Void Pearl darkness skip, pet damage redirected to the Hunter's Guide owner). |
| 2026-10-22 | 90 | 91 | 0 | Added Enchanter's Pearl (lapis-free double-roll enchanting mixin, charm slot bonus). |
| 2026-10-22 | 91 | 92 | 0 | Added The Burden of Desolation (natural-spawn block mixin; refmap target verified). |
| 2026-10-23 | 92 | 93 | 0 | Added The Ender Slayer (End-dweller damage/knockback, teleport block, XP-for-drops in the End; mixins applied). |
| 2026-10-23 | 93 | 94 | 0 | Added the Soul Compass (Wayfinder of the Damned; compass packet, item properties). |
| 2026-10-23 | 94 | 94 | 0 | Guide to Animal Companionship: attack protection for protected animals, hoglins ignore the holder (MixinHoglinTasks, AccessorTargetGoal). |
| 2026-10-23 | 94 | 94 | 0 | Added the Curse of Eternal Binding (Enchanter Pearl roll, death-keep for armor and trinkets, trinket unequip block). |
| 2026-10-23 | 94 | 94 | 0 | Added the Cursed Ring lava-pool destruction (Unholy Stone death in a Nether lava pool destroys both rings). |
| 2026-10-23 | 94 | 95 | 0 | Added the Bulwark of Blazing Pride (shield mixins, blocking property). |
| 2026-10-23 | 95 | 96 | 0 | Added The Infinitum (worthy-only; knockback, boss damage, lifesteal, death save; open property). |
| 2026-10-23 | 101 | 104 | 1 | Added the other seven enchantments (Sharpshooter, Ceaseless, Slayer, Torrent, Wrath, Nemesis, Sorrow); `Player.getProjectile`, `CrossbowItem` and `ThrownTrident` accessor mixins load. The 1 problem is a false positive from the Enigmatic Eye commit (fe96ec4): its name uses the `enigmatic_eye_dormant`/`_active` keys, as upstream. Enchantments still need an in-world test. |
| 2026-10-23 | 112 | 115 | 1 | Inventory buttons (Ender Chest, magnet toggle) and the magnet flag sync packets; client accessor mixin loads. Same Enigmatic Eye false positive. Buttons still need an in-world click test (survival and creative inventory, Ender/Cursed Ring worn). |
| 2026-10-23 | 112 | 115 | 1 | Spellstone and scroll slot unlocks: `PacketSlotUnlocked`, the slot-unlocked toast and `MixinPlayerAdvancements` (mixin load checked). Same Enigmatic Eye false positive. Earning Discover Spellstone/Scroll, the toast, the relog and death re-unlock, and the Revelation Tome toast still need an in-world test. |
| 2026-10-23 | 112 | 115 | 1 | Permadeath / world transience (fractured and cursed world flags, permadeath screen, world list lock and alt info line) and the Abyssal Heart drop with the With Dragons quote; `EnderDragon` and `WorldSelectionList$WorldListEntry` mixin handlers merge. Same Enigmatic Eye false positive. Needs in-world test: lose all soul crystals, check the world list lock; kill the dragon as the Worthy One. |
| 2026-10-23 | 112 | 115 | 1 | Cursed inventory trigger, Scroll of Avarice piglin/villager hooks, Cursed Ring insomnia phantoms, Desolation Ring golem block, poison death save, rocket deflection; the devcheck now checks the `InventoryChangeTrigger`, `PiglinAi`, `Villager`, `PhantomSpawner`, `MobEffect` and `FireworkRocketEntity` handlers (all merge). Same Enigmatic Eye false positive. Needs in-world test: barter with the scroll (thrown and by hand), villager prices, sleepless phantoms with the Cursed Ring, golems near a Desolation Ring, poison at low health, a rocket at a violet amulet wearer. |
| 2026-10-23 | 112 | 115 | 1 | Patchouli integration (book entry state and GUI scale packets, trusted book links, Twist/Infinitum names on the landing page); the devcheck now checks the `GuiBook` and `GuiBookLanding` handlers (both merge). Same Enigmatic Eye false positive. Needs in-world test: open The Acknowledgment, The Twist and The Infinitum (landing name), click a book web link, earn a `book/` advancement and check its entry is unread. |
| 2026-10-23 | 112 | 115 | 1 | Commands, enchanting GUI packet, fishing hook / llama spit / shulker bullet deflection. The FishingHook, LlamaSpit and ShulkerBullet mixins merge; the advancement suggestions accessor works. Same Enigmatic Eye false positive. Needs in-world test: the three new manual rows below. |
| 2026-10-23 | 112 | 115 | 0 | In-world playtest run (`-Pplaytest=all,exit -Pdevcheck=true`): 17/17 scenarios ok. Eye false positive fixed (the check now uses the shown name's key). Advancements that read Curios data fixed (Desolation Ring trigger, scroll/spellstone slot tags). Missing `AdvancedBrewingRecipeProcessor` added, so the book pages load. |
| 2026-10-23 | 112 | 115 | 0 | Extended playtest: 39/39 scenarios ok in single player and 39/39 on a dedicated server (`remote` client). New: keybind presses (scroll, ring, spellstones), sound recording, GUI/HUD screenshots, tooltip dump of all 178 items (0 missing keys) and a lang comparison with upstream (0 missing, 0 changed in every language). Fixed: 7 client packets and several client-only methods crashed a dedicated server (Fabric does not strip lambdas). |

## Manual checklist

Status: `-` not tested, `ok`, `bug` (put details in the notes).

| Item | Creative tab | Texture | Name + tooltip | Recipe / use | Status |
|---|---|---|---|---|---|
| Mending Mixture | ok | ok | ok (lang) | auto ok (`mending_mixture`: crafting with a damaged pickaxe repairs it); crafting-table use still to test | - |
| Magnetic Ring (`magnet_ring`) | ok | ok | ok (lang) | auto ok (`magnet_ring`, `magnet_ring_sneak`, `magnet_conflict`); rest - (pulls items in range 8, not while sneaking; can't equip with Dislocation Ring) | - |
| Extradimensional Eye (`extradimensional_eye`) | ok | ok | ok (lang) | - (sneak + use binds the position once; hitting a mob in the bound dimension teleports it there with portal/dragon-breath particles and uses up the Eye, unless creative) | - |
| Potion of Recall (`recall_potion`) | ok | ok | ok (lang) | auto ok (`recall_potion`); rest - (drink only in Overworld/Nether/End; returns you to your bed/anchor or world spawn, across dimensions; glass bottle back unless creative) | - |
| Dislocation Ring (`super_magnet_ring`) | ok | ok | ok (lang) | auto ok (`super_magnet_ring`); rest - (teleports items straight into inventory, server side; can't equip with Magnetic Ring) | - |
| Blank Scroll (`thicc_scroll`) | ok | ok | ok (lang) | - | - |
| Iron Ring (ring slot, +1 armor, right-click equips, Curse of Binding in anvil) | ok | ok | ok (lang) | auto ok (`iron_ring`) | - |
| Etherium Ingot (fire resistant) | ok | ok | ok (lang) | auto ok (`etherium_ingot`) | - |
| Etherium Nugget | ok | ok | ok (lang) | - | - |
| Etherium Scraps | ok | ok | ok (lang) | - | - |
| Ender Rod | ok | ok | ok (lang) | - | - |
| Astral Dust | ok | ok | ok (lang) | - | - |
| Lore Fragment | ok | ok | ok (lang) | - | - |
| Heart of the Earth | ok | ok | ok (lang) | - | - |
| Twisted Heart | ok | ok | ok (lang) | - | - |
| Exquisite Ring (golden_ring; ring slot, +1 luck, piglins neutral, barter currency) | ok | ok | ok (lang) | auto ok (`golden_ring`) | - |
| Nefarious Essence | ok | ok | ok (lang) | - | - |
| Nefarious Ingot | ok | ok | ok (lang) | - | - |
| Unholy Stone | ok | ok | ok (lang) | - | - |
| Darkest Scroll | ok | ok | ok (lang) | - | - |
| Tome of Hungering Knowledge | ok | ok | ok (lang) | - (enchantment transposing) | - |
| Heart of the Cosmos | ok | ok | ok (lang) | - | - |
| Heart of the Abyss (abyssal_heart) | ok | ok | ok (lang) | - | - |
| Soul Dust | ok | ok | ok (lang) | - | - |
| Emblem of Monster Slayer (monster_charm) | ok | ok | ok (lang) | auto ok (`monster_charm`); rest - (undead +25%, hostile +10%, +1 looting, double XP from monsters) | - |
| Scroll of Ageless Wisdom (xp_scroll) | ok | ok | ok (lang) | auto ok (`xp_scroll`); rest - (use/J toggles mode; absorb, extract, pull orbs in range; write/learn sounds) | - |
| Ring of Ender (ender_ring) | ok | ok | ok (lang) | auto ok (`ender_ring`); rest - (ring slot; I opens ender chest with open/close sounds; does nothing unequipped) | - |
| Essence of Raging Life (infinimeal) | ok | ok | ok (lang) | auto ok (`infinimeal`); rest - (never consumed; grows crops, cactus, sugar cane, vines, nether wart, chorus; works from a dispenser) | - |
| Astral Potato (astral_potato) | ok | ok | ok (lang) | auto ok (`astral_potato`); rest - (eat: food 8, small random teleport with chorus sound) | - |
| Charm of Treasure Hunter (mining_charm) | ok | ok | ok (lang) | - (charm slot; night vision below Y and toggle; faster mining; +Fortune on ores, gravel, leaves; +block reach for breaking and placing, also on a server) | - |
| Axe of Executioner (forbidden_axe) | ok | ok | ok (lang) | - (10% + 5%/looting head drop from skeleton, wither skeleton, zombie, creeper, dragon when killed with it; tooltip chance includes Trinkets looting; no head duplicated if one already dropped; beheading advancement; cannot harvest blocks) | - |
| Heart of Creation (enigmatic_item) | ok | ok | ok (lang) | auto ok (`enigmatic_item`); rest - (spellstone slot; creative-style flight; no fall, fire, lava, drowning, wall, void, starve damage; harmful effects blocked; never dies while worn or in inventory; ×5 mining speed in the air; K fires Ultimate Wither Skulls, hold K for rapid fire; explosion and particles) | - |
| Blazing Core (blazing_core) | ok | ok | ok (lang) | auto ok (`blazing_core`); rest - (spellstone slot; fire, burning and hot floor immunity; ~30 s lava immunity, then the heat bar fills and lava hurts; extinguish sound and particles on first lava touch; double damage from water mobs and drowned; attackers burn and take damage; status effects tick twice as fast; clear lava fog; found in nether chests) | - |
| Scroll of Postmortal Recall (escape_scroll) | ok | ok | ok (lang) | auto ok (`escape_scroll`); rest - (scroll slot; on death all drops, trinkets included, become permanent items at the respawn point; only the owner picks them up; pickup particles and sound; immune to lava, fire and other damage except /kill) | - |
| Enigmatic Amulet (enigmatic_amulet) | ok | ok | ok (lang) | - (new world: gift in hotbar slot 9 with the inscription and a seeded color, not given again on rejoin; color model; charm slot; RED/AQUA/BLUE/MAGENTA/GREEN/VIOLET/BLACK attribute bonuses; MAGENTA lower gravity and fall damage -2 (2 or less cancelled); AQUA faster swimming; GREEN mining speed; BLACK 10% lifesteal; VIOLET 15% projectile deflection with sound; kept on death via postmortal possessions) | - |
| Ultimate Potions (ultimate_potion, _splash, _lingering) | ok | ok | ok (lang) | - (brewing from vanilla potions with Ender Eye/Nether Star chain; long effects in tooltip; splash hits area; lingering leaves a cloud; colors match effects; Potions tab) | - |
| Common Potions (common_potion, _splash, _lingering; e.g. Haste, Molten Heart) | ok | ok | ok (lang) | - (brew; Molten Heart: lava immunity and clear lava fog; common lingering leaves a cloud, not a splash) | - |
| Potion of Twisted Mercy (twisted_potion) | ok | ok | ok (lang) | auto ok (`twisted_potion`); rest - (cursed-only, needs a last death location; drink: teleports to where you last died, across dimensions) | - |
| Bottle of Ichor (ichor_bottle) | ok | ok | ok (lang) | auto ok (`ichor_bottle`); rest - (drink: Regeneration III, Absorption V, Resistance III, Fire Resistance; sets ConsumedIchorBottle) | - |
| Unholy Grail (unholy_grail) | ok | ok | ok (lang) | auto ok (`unholy_grail`); rest - (drink: Wither, Poison, Nausea, Weakness, Hunger, Slowness + advancement; cursed player who ate the Forbidden Fruit gets Regeneration, Absorption, Resistance, Strength instead) | - |
| Forbidden Fruit (forbidden_fruit) | ok | ok | ok (lang) | auto ok (`forbidden_fruit`); rest - (cursed-only eat; hunger bar replaced by fruit icons; food stays full, no saturation; small heals reduced; flag survives death and relog; advancement on join) | - |
| Redemption Potion (redemption_potion) | ok | ok | ok (lang) | auto ok (`redemption_potion`); rest - (drinkable only after the Forbidden Fruit; drink: normal hunger bar and food return, also on the client) | - |
| Celestial Fruit (astral_fruit) | ok | ok | ok (lang) | auto ok (`astral_fruit`); rest - (cursed-only eat; beacon sound; effects; unlocks an extra ring slot) | - |
| Angel's Blessing (angel_blessing) | ok | ok | ok (lang) | auto ok (`angel_blessing`); rest - (spellstone key or jump mid-air: dash where you look; no fall/collision damage; deflects some projectiles; own arrows faster) | - |
| Heart of the Golem (golem_heart) | ok | ok | ok (lang) | auto ok (`golem_heart`); rest - (+armor; with no armor worn: more armor, toughness, explosion resistance; melee and knockback resistance; extra magic damage taken) | - |
| Will of the Ocean (ocean_stone) | ok | ok | ok (lang) | auto ok (`ocean_stone`); rest - (key: thunderstorm for XP; breathe underwater, air bar hidden, night vision, fast swim, no attribute lines in tooltip; extra fire damage taken) | - |
| Eye of the Nebula (eye_of_nebula) | ok | ok | ok (lang) | auto ok (`eye_of_nebula`); rest - (key: teleport behind target, next hit empowered; magic resist; chance to phase away from hits; no ender pearl damage) | - |
| Pearl of the Void (void_pearl) | ok | ok | ok (lang) | auto ok (`void_pearl`); rest - (no hunger, no air, effect immunity; hits wither; mobs in darkness nearby take damage; chance to survive lethal hit) | - |
| The Cube (the_cube) | ok | ok | ok (lang) | auto ok (`the_cube`); rest - (hidden recipe; key: random teleport across dimensions; all passive buffs in tooltip; lethal hit leaves 1 HP or revives via active) | - |
| Gift of the Heaven (heaven_scroll) | ok | ok | ok (lang) | auto ok (`heaven_scroll`); rest - (flight in survival, drains XP) | - |
| Fabulous Scroll (fabulous_scroll) | ok | ok | ok (lang) | - (flight drains XP, free within an active beacon's range; no mining penalty while flying) | - |
| Scroll of a Thousand Curses (cursed_scroll) | ok | ok | ok (lang) | auto ok (`cursed_scroll`); rest - (cursed-only; inactive until the Cursed Ring) | - |
| Scroll of Avarice (avarice_scroll) | ok | ok | ok (lang) | auto ok (`avarice_scroll`); rest - (cursed-only; inactive until the Cursed Ring) | - |
| Cosmic Scroll (cosmic_scroll, not in creative tab: /give) | - | - | - | - (blessed-only; +1 scroll slot, revive at 50% on cooldown, extra tooltip lines) | - |
| Astral Breaker (astral_breaker) | ok | ok | ok (lang) | auto ok (`astral_breaker`); rest - (mines 3x3 on pickaxe, axe and shovel blocks; sneak mines one block; flame particles on other players' clients; fireproof item) | - |
| Keystone of the Oblivion (void_stone) | ok | ok | ok (lang) | - (use toggles voiding; picked-up items on its list are voided; Ctrl tooltip shows the list; combine recipe adds an item to the list; in Nether fortress/bastion chests, weight 8) | - |
| Curse Transposer (curse_transposer) | ok | ok | ok (lang) | - (craft with an item and a book: moves only curses to the book; enchant-table sound on crafting; Cursed Stone crafting plays the beacon sound) | - |
| The Architect's Inkwell (lore_inscriber) | ok | ok | ok (lang) | auto ok (`lore_inscriber`); rest - (use opens the inkwell GUI; a Lore Fragment in the left slot gets the typed name or lore commands; taking the result plays the write sound) | - |
| Extradimensional Vessel (storage_crystal) | ok | ok | ok (lang) | - (die wearing the Enigmatic Amulet: drops and XP go into a floating vessel at the death point; only the owner can pick it up unless the config allows others; pickup returns items and XP, plus the soul if one was embedded) | - |
| Soul Crystal (soul_crystal) | ok | ok | ok (lang) | auto ok (`soul_crystal`); rest - (with soul loss on, death drops a crystal and lowers max health 10% per lost crystal; picking it up or using it restores the health; rejoining and respawning keep the penalty; `soul_archive.json` appears in the world folder) | - |
| Bulwark of Blazing Pride (infernal_shield) | ok | ok | ok (lang) | auto ok (`infernal_shield`); rest - (cursed-only block; blocks from the front, wearing down its durability; axes never disable it; x1.5 damage taken from behind while blocking; removes Blazing Strength on hit; obsidian repairs; Unbreaking at the table) | - |
| The Infinitum (the_infinitum) | ok | ok | ok (lang) | - (worthy wielder: extra knockback, bonus damage to bosses and players, 10% lifesteal, chance to survive a lethal hit, ignores invulnerability; blade opens after holding ~5 ticks; unworthy hit is cancelled and debuffs the wielder) | - |
| Amulet of Ascension (ascension_amulet) | ok | ok | ok (lang) | - (hidden recipe; worn: the modifiers of every amulet color at once; cannot be worn with an Enigmatic Amulet; on death the Extradimensional Vessel keeps the drops like the Enigmatic Amulet's) | - |
| The Testament of Contempt (eldritch_amulet) | ok | ok | ok (lang) | auto ok (`eldritch_amulet`); rest - (hidden recipe; worthy-only equip; +3 attack damage; 15% lifesteal; looked-at entities within 3 blocks get Slowness II, Mining Fatigue II and Weakness II; on death keeps inventory, armor, offhand and accessories, except Curse of Vanishing items) | - |
| Hidden recipes (crafting_shaped_hidden) | - | - | - | - (crafting table: The Cube, Amulet of Ascension (keeps the Enigmatic Amulet's color and inscription), The Testament of Contempt, Cosmic Scroll (needs a blessed Cosmic Heart); each shows on its Acknowledgment page) | - |
| Majestic Elytra (enigmatic_elytra) | ok | ok | ok (lang) | - (chest or back slot; glides; holding jump while flying boosts with the ender-eye sound and dragon-breath particles, seen by other players too; boosting wears durability faster; repair with etherium; hides the cape; renders on armor stands; right-click equips to chest) | - |
| The Judgement (the_judgement) | ok | ok | ok (lang) | - (creative-only; a hit kills every living entity within 64 blocks of the target; right-click toggles no-drops mode (glint, charge sound), which also deletes nearby items and XP orbs) | - |
| Narrator quotes (system) | - | - | - | - (needs the narrator unlocked by the Enigmatic Eye; voice line plus yellow subtitle box above the hotbar, also over open screens; quotes on respawn, first Nether/End arrival, completing an End portal, summoning the Wither/Ender Dragon, entering an End gateway, hitting the dragon; each "once" quote plays only once per player; Relic of Testing resets the unlock) | - |
| Inscrutable Eye (enigmatic_eye) | ok | ok | ok (lang) | auto ok (`enigmatic_eye`); rest - (first vanilla chest a player opens gives a dormant Eye, once per player; right-click wakes it and unlocks the narrator with an intro quote; awake Eye is a charm: +3 block reach; dormant Eye cannot be worn) | - |
| Charming Insignia (insignia) | ok | ok | ok (lang) | - (charm, adds a charm slot; rename it in an anvil and wear it: other players see that name over your head; right-click toggles showing your own nameplate in third person; with the Eye worn or in creative, players' real names show small under their nameplate) | - |
| Special loot (SpecialLootModifier) | - | - | - | - (Bottle of Ichor from chests only once per player; Celestial Fruit in the first End city chest for a cursed player; Heart of the Earth replaces suspicious-sand/gravel loot at 3%) | - |
| Quote Player (quote_player) | - | - | - | - (hidden, /give only; sneak-right-click cycles the selected quote (action bar shows its name, tooltip too); right-click plays it with subtitles) | - |
| Voracious Pan (eldritch_pan) | ok | ok | ok (lang) | - (hidden recipe; worthy-only: unworthy hits are cancelled; blocks like a shield and eats projectiles (food, burp, fire charge puff, large fireballs don't explode); 3 s cooldown when an axe disables it; holding it builds Growing Hunger/Bloodlust; lifesteal and hungersteal; kills of new mob types raise damage/armor (gold chat message); mob drops cooked; enchants as a diamond sword; check in-hand vs inventory model) | - |
| Lamps (massive_lamp, massive_shroomlamp, massive_redstonelamp, big_lamp, big_shroomlamp, big_redstonelamp) | ok | ok | ok (lang) | - (place each: lantern light (15), cutout model with no black corners, drops itself) | - |
| Block of Etherium (etherium_block) | ok | ok | ok (lang) | - (rare name color, animated texture, light 10, needs a diamond pickaxe, drops itself) | - |
| Block of Astral Dust (astral_block) | ok | ok | ok (lang) | - (epic name color, light 10, drops itself) | - |
| Star Fabric (star_fabric) | ok | ok | ok (lang) | - (epic name color, light 10, drops itself) | - |
| Cosmic Cake (cosmic_cake) | ok | ok | ok (lang) | - (eat slices: effects per upstream; tooltip; cutout model; a bitten cake regrows one slice per random tick) | - |
| End Anchor (end_anchor) | ok | ok | ok (lang) | - (charge with ender pearls up to 4, comparator output; light scales with charge; right-click with charge sets spawn (default: any dimension not blacklisted; EndExclusiveMode: End only), where it cannot it explodes; dispenser charges it; die: respawn on the anchor and use a charge; anchor gone or empty: normal respawn; end portal renderer on its faces; tooltip) | - |
| Commands (system) | - | - | - | - (op: `/haveadv <advancement>` tab-completes and says whether you have it; `/getringtime`, `/getnoringtime` report ticks and percent; `/setringtime`, `/setnoringtime <player> <ticks>` change them; level 4 needed for the ring-time ones) | - |
| Enchanting advancement (system) | - | - | - | - (opening an enchanting table grants `book/enchantments/generic`; the Acknowledgment entry unlocks) | - |
| Projectile deflection (fishing hook, llama spit, shulker bullet) | - | - | - | - (with the deflecting amulet worn, these are deflected like arrows; without it, they hit as normal; a fishing rod still hooks mobs) | - |

## Notes

### 2026-10-23 automated pass

- Run: `./gradlew runClient -Pplaytest=all,exit -Pdevcheck=true` (from `1.20.1/`). The client opens the world `playtest`, screenshots both creative tabs to `test-output/playtest/screenshots/` (`main_0..4`, `potions_0..4`), runs the scenarios in `DevPlaytestScenarios`, then quits. It starts muted, does not pause in the background and never moves the mouse.
- Creative tab and Texture columns: from the screenshots. Every item in both tabs has its texture (no missing-texture squares). Rows not marked: system rows and /give-only items.
- `auto ok (...)`: that scenario passed. Each scenario checks only the core effect named in its id, so the rest of the row still needs a manual pass.
- Desolation Ring: `desolation_advancement` passed (wearing it grants the advancement); no checklist row yet.
- Still manual for every item: name and tooltip, GUI screens, keybinds, HUD, particles, sounds, narrator, and a multiplayer check. (Superseded by the extended pass below.)

### 2026-10-23 extended pass

- Single player: `./gradlew runClient -Pplaytest=all,exit -Pdevcheck=true`, results in `test-output/playtest/results.md`.
- Multiplayer: start `./gradlew runServer -Pplaytest=all,exit -Pdevcheck=true --args=nogui`, then `./gradlew runClient -Pplaytest=remote,exit -Pdevcheck=true`. The client joins `localhost`, the server runs the same scenarios on the connected player and writes `results-server.md`; the client takes the `mp_*` screenshots.
- `ok (lang)` in Name + tooltip: the item's name and every tooltip line (plain, Shift, Ctrl) resolve to text (`tooltips.md`), and the lang files match upstream key for key (`lang.md`, made by `tools/compare_lang.py`). The four port-only keys are the Fabric attribute names and the charm modifier header.
- Keybinds and sounds: the scenarios press the real key mappings (J, I, spellstone key) and the Ageless Wisdom toggle checks that its `charged_on` sound played. Other sounds and all particles are not checked by the harness.
- The client always opens on the second monitor without focus (`DevMixinWindow`).
- Left for Dan (review sheet, look and feel only): GUI and HUD screenshots, item textures in both tabs, particle and sound feel. Rows still marked `rest -` describe behavior no scenario covers yet.
