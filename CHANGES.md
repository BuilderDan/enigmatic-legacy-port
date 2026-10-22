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
