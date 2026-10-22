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

Cursed-only tooltip lines stay inactive until the Cursed Ring is ported
(`isTheCursedOne` always returns false for now).

## Notes
