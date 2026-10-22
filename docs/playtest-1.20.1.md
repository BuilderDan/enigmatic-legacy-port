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

## Manual checklist

Status: `-` not tested, `ok`, `bug` (put details in the notes).

| Item | Creative tab | Texture | Name + tooltip | Recipe / use | Status |
|---|---|---|---|---|---|
| Mending Mixture | - | - | - | - (repair recipe needs EnigmaticRecipes, not ported yet) | - |
| Magnetic Ring (`magnet_ring`) | - | - | - | - (pulls items in range 8, not while sneaking; can't equip with Dislocation Ring) | - |
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
| Nefarious Essence | - | - | - | - | - |
| Nefarious Ingot | - | - | - | - | - |
| Unholy Stone | - | - | - | - | - |
| Darkest Scroll | - | - | - | - | - |
| Soul Dust | - | - | - | - | - |

Cursed-only tooltip lines stay inactive until the Cursed Ring is ported
(`isTheCursedOne` always returns false for now).

## Notes
