# Playtest: 1.20.1 Fabric

A version counts as done only after a manual dev-client playtest of every item.
The automatic check below is a smoke test only.

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

## Manual checklist

Status: `-` not tested, `ok`, `bug` (put details in the notes).

| Item | Creative tab | Texture | Name + tooltip | Recipe / use | Status |
|---|---|---|---|---|---|
| Mending Mixture | - | - | - | - (repair recipe loads; in-game repair not tested yet) | - |
| Magnetic Ring (`magnet_ring`) | - | - | - | - (pulls items in range 8, not while sneaking; can't equip with Dislocation Ring) | - |
| Extradimensional Eye (`extradimensional_eye`) | - | - | - | - (sneak + use binds the position once; hitting a mob in the bound dimension teleports it there with portal/dragon-breath particles and uses up the Eye, unless creative) | - |
| Potion of Recall (`recall_potion`) | - | - | - | - (drink only in Overworld/Nether/End; returns you to your bed/anchor or world spawn, across dimensions; glass bottle back unless creative) | - |
| Dislocation Ring (`super_magnet_ring`) | - | - | - | - (teleports items straight into inventory, server side; can't equip with Magnetic Ring) | - |
| Blank Scroll (`thicc_scroll`) | - | - | - | - | - |
| Iron Ring (ring slot, +1 armor, right-click equips, Curse of Binding in anvil) | - | - | - | - | - |
| Etherium Ingot (fire resistant) | - | - | - | - | - |
| Etherium Nugget | - | - | - | - | - |
| Etherium Scraps | - | - | - | - | - |
| Ender Rod | - | - | - | - | - |
| Astral Dust | - | - | - | - | - |
| Lore Fragment | - | - | - | - | - |
| Heart of the Earth | - | - | - | - | - |
| Twisted Heart | - | - | - | - | - |
| Exquisite Ring (golden_ring; ring slot, +1 luck, piglins neutral, barter currency) | - | - | - | - | - |
| Nefarious Essence | - | - | - | - | - |
| Nefarious Ingot | - | - | - | - | - |
| Unholy Stone | - | - | - | - | - |
| Darkest Scroll | - | - | - | - | - |
| Tome of Hungering Knowledge | - | - | - | - (enchantment transposing) | - |
| Heart of the Cosmos | - | - | - | - | - |
| Heart of the Abyss (abyssal_heart) | - | - | - | - | - |
| Soul Dust | - | - | - | - | - |
| Emblem of Monster Slayer (monster_charm) | - | - | - | - (undead +25%, hostile +10%, +1 looting, double XP from monsters) | - |
| Scroll of Ageless Wisdom (xp_scroll) | - | - | - | - (use/J toggles mode; absorb, extract, pull orbs in range; write/learn sounds) | - |
| Ring of Ender (ender_ring) | - | - | - | - (ring slot; I opens ender chest with open/close sounds; does nothing unequipped) | - |
| Essence of Raging Life (infinimeal) | - | - | - | - (never consumed; grows crops, cactus, sugar cane, vines, nether wart, chorus; works from a dispenser) | - |
| Astral Potato (astral_potato) | - | - | - | - (eat: food 8, small random teleport with chorus sound) | - |
| Charm of Treasure Hunter (mining_charm) | - | - | - | - (charm slot; night vision below Y and toggle; faster mining; +Fortune on ores, gravel, leaves; +block reach for breaking and placing, also on a server) | - |
| Axe of Executioner (forbidden_axe) | - | - | - | - (10% + 5%/looting head drop from skeleton, wither skeleton, zombie, creeper, dragon when killed with it; tooltip chance includes Trinkets looting; no head duplicated if one already dropped; beheading advancement; cannot harvest blocks) | - |
| Heart of Creation (enigmatic_item) | - | - | - | - (spellstone slot; creative-style flight; no fall, fire, lava, drowning, wall, void, starve damage; harmful effects blocked; never dies while worn or in inventory; ×5 mining speed in the air; K fires Ultimate Wither Skulls, hold K for rapid fire; explosion and particles) | - |
| Blazing Core (blazing_core) | - | - | - | - (spellstone slot; fire, burning and hot floor immunity; ~30 s lava immunity, then the heat bar fills and lava hurts; extinguish sound and particles on first lava touch; double damage from water mobs and drowned; attackers burn and take damage; status effects tick twice as fast; clear lava fog; found in nether chests) | - |
| Scroll of Postmortal Recall (escape_scroll) | - | - | - | - (scroll slot; on death all drops, trinkets included, become permanent items at the respawn point; only the owner picks them up; pickup particles and sound; immune to lava, fire and other damage except /kill) | - |
| Enigmatic Amulet (enigmatic_amulet) | - | - | - | - (new world: gift in hotbar slot 9 with the inscription and a seeded color, not given again on rejoin; color model; charm slot; RED/AQUA/BLUE/MAGENTA/GREEN/VIOLET/BLACK attribute bonuses; MAGENTA lower gravity and fall damage -2 (2 or less cancelled); AQUA faster swimming; GREEN mining speed; BLACK 10% lifesteal; VIOLET 15% projectile deflection with sound; kept on death via postmortal possessions) | - |
| Ultimate Potions (ultimate_potion, _splash, _lingering) | - | - | - | - (brewing from vanilla potions with Ender Eye/Nether Star chain; long effects in tooltip; splash hits area; lingering leaves a cloud; colors match effects; Potions tab) | - |
| Common Potions (common_potion, _splash, _lingering; e.g. Haste, Molten Heart) | - | - | - | - (brew; Molten Heart: lava immunity and clear lava fog; common lingering leaves a cloud, not a splash) | - |
| Potion of Twisted Mercy (twisted_potion) | - | - | - | - (cursed-only, needs a last death location; drink: teleports to where you last died, across dimensions) | - |
| Bottle of Ichor (ichor_bottle) | - | - | - | - (drink: Regeneration III, Absorption V, Resistance III, Fire Resistance; sets ConsumedIchorBottle) | - |
| Unholy Grail (unholy_grail) | - | - | - | - (drink: Wither, Poison, Nausea, Weakness, Hunger, Slowness + advancement; cursed player who ate the Forbidden Fruit gets Regeneration, Absorption, Resistance, Strength instead) | - |
| Forbidden Fruit (forbidden_fruit) | - | - | - | - (cursed-only eat; hunger bar replaced by fruit icons; food stays full, no saturation; small heals reduced; flag survives death and relog; advancement on join) | - |
| Redemption Potion (redemption_potion) | - | - | - | - (drinkable only after the Forbidden Fruit; drink: normal hunger bar and food return, also on the client) | - |
| Celestial Fruit (astral_fruit) | - | - | - | - (cursed-only eat; beacon sound; effects; unlocks an extra ring slot) | - |

| Angel's Blessing (angel_blessing) | - | - | - | - (spellstone key or jump mid-air: dash where you look; no fall/collision damage; deflects some projectiles; own arrows faster) | - |
| Heart of the Golem (golem_heart) | - | - | - | - (+armor; with no armor worn: more armor, toughness, explosion resistance; melee and knockback resistance; extra magic damage taken) | - |
| Will of the Ocean (ocean_stone) | - | - | - | - (key: thunderstorm for XP; breathe underwater, air bar hidden, night vision, fast swim, no attribute lines in tooltip; extra fire damage taken) | - |
| Eye of the Nebula (eye_of_nebula) | - | - | - | - (key: teleport behind target, next hit empowered; magic resist; chance to phase away from hits; no ender pearl damage) | - |
| Pearl of the Void (void_pearl) | - | - | - | - (no hunger, no air, effect immunity; hits wither; mobs in darkness nearby take damage; chance to survive lethal hit) | - |
| The Cube (the_cube) | - | - | - | - (key: random teleport across dimensions; all passive buffs in tooltip; lethal hit leaves 1 HP or revives via active) | - |
| Gift of the Heaven (heaven_scroll) | - | - | - | - (flight in survival, drains XP) | - |
| Fabulous Scroll (fabulous_scroll) | - | - | - | - (flight drains XP, free within an active beacon's range; no mining penalty while flying) | - |
| Scroll of a Thousand Curses (cursed_scroll) | - | - | - | - (cursed-only; inactive until the Cursed Ring) | - |
| Scroll of Avarice (avarice_scroll) | - | - | - | - (cursed-only; inactive until the Cursed Ring) | - |
| Cosmic Scroll (cosmic_scroll, not in creative tab: /give) | - | - | - | - (blessed-only; +1 scroll slot, revive at 50% on cooldown, extra tooltip lines) | - |
| Astral Breaker (astral_breaker) | - | - | - | - (mines 3x3 on pickaxe, axe and shovel blocks; sneak mines one block; flame particles on other players' clients; fireproof item) | - |
| Keystone of the Oblivion (void_stone) | - | - | - | - (use toggles voiding; picked-up items on its list are voided; Ctrl tooltip shows the list; combine recipe adds an item to the list; in Nether fortress/bastion chests, weight 8) | - |
| Curse Transposer (curse_transposer) | - | - | - | - (craft with an item and a book: moves only curses to the book; enchant-table sound on crafting; Cursed Stone crafting plays the beacon sound) | - |
| The Architect's Inkwell (lore_inscriber) | - | - | - | - (use opens the inkwell GUI; a Lore Fragment in the left slot gets the typed name or lore commands; taking the result plays the write sound) | - |
| Extradimensional Vessel (storage_crystal) | - | - | - | - (die wearing the Enigmatic Amulet: drops and XP go into a floating vessel at the death point; only the owner can pick it up unless the config allows others; pickup returns items and XP, plus the soul if one was embedded) | - |
| Soul Crystal (soul_crystal) | - | - | - | - (with soul loss on, death drops a crystal and lowers max health 10% per lost crystal; picking it up or using it restores the health; rejoining and respawning keep the penalty; `soul_archive.json` appears in the world folder) | - |

Cursed-only tooltip lines stay inactive until the Cursed Ring is ported
(`isTheCursedOne` always returns false for now).

## Notes
