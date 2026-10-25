# Port plan

Written 2026-10-21. Living document: update it as decisions are made.

## 1. Upstream base

- Repo: https://github.com/Aizistral-Studios/Enigmatic-Legacy
- **Base branch: `1.20.X`** (commit `a0294f1`, mod 2.30.1, Forge 1.20.1-47.1.43, last
  commit 2026-08-18). It is the active branch; `1.19.X` is older (1.19.4, 2.28.0).
- Also useful as reference: `neoforge_1.20.1` (upstream's own NeoForge 1.20.1 build, 2.30.2).
- Related project: [Enigmatic Legacy Plus](https://modrinth.com/mod/enigmaticlegacy-plus)
  by Auviotre ([source](https://github.com/Auviotre/Enigmatic-Legacy-Plus)), an active
  1.21.1 NeoForge port under the same license. **We skip 1.21.1 NeoForge** (Dan,
  2026-10-21) and point NeoForge players there.

## 2. Size of the job

| Area | Upstream (1.20.1 Forge) |
|---|---|
| Java | 345 files, ~41,800 lines |
| Items / blocks / block entities / entities | 105 / 13 / 1 / 3 |
| Enchantments / effects / potions | 8 / 4 / 23 |
| Recipe serializers / loot modifiers / loot functions | 8 / 1 / 1 |
| Advancements / criteria triggers | 196 JSON / 7 |
| Menus / screens / keybinds | 1 / 2 / 2 handlers |
| Assets | 289 textures, 237 models, 45 sounds, 7 languages |
| Guide book (Patchouli) | 10 categories, 100 entries |

## 3. Dependencies and their replacements

| Upstream (Forge) | Used for | Fabric | NeoForge |
|---|---|---|---|
| Curios (38 files; slots: charm 11, ring 7, scroll 8, spellstone 8, back 1) | Rings, charms, scrolls, spellstones | **Trinkets** (1.20.1, 1.21.1); **Trinkets Updated** 4.2.1+26.3 (Patbox fork, same API) on 26.3 | Curios (available up to 26.3) |
| Patchouli (9 files + 2 GUI mixins) | The Acknowledgment guide book | Patchouli (1.20.1, 1.21.1). **No 26.3 build** (newest 26.1-94-beta): optional on 26.3, compiled against 26.1, guarded by isModLoaded + mixin plugin | Patchouli |
| Caelus (2 files) | Enigmatic Elytra (back slot) | Fabric API elytra events (1.20.1); vanilla glider component (1.21.2+) | Caelus (1.21.1); vanilla glider component later |
| Omniconfig (bundled, by Aizistral) | All config | Keep the library; write a Fabric backend for its one Forge wrapper | Keep; port the wrapper |
| Quark, Dark Utilities, CSG, Blue Skies, JEI (soft compat) | Small compat fixes | Drop at first; re-add per mod if the mod exists there | Same |

Design rule: game code talks to a small `common` interface (`SlotAccess`, `Network`,
`PlayerData`, `Platform`), never to Trinkets/Curios directly. Each loader implements it.

## 4. Loader-specific systems to rewrite

| System | Upstream | Plan |
|---|---|---|
| Events | 73 `@SubscribeEvent`, 56 of them in `EnigmaticEventHandler` (3,428 lines) | Split into loader-neutral handler methods; Fabric calls them from Fabric API events or mixins, NeoForge from its event bus |
| Networking | 1 SimpleChannel, 31 packets | Common packet classes + per-loader send/receive (Fabric networking API; NeoForge payloads). 1.20.5+ uses custom payload codecs |
| Player data | 1 capability (playtime counter) | Fabric: Cardinal Components (1.20.1) or Fabric data attachments (1.21+); NeoForge: data attachments |
| Mixins | 33 (Player, LivingEntity, ItemStack, Villager, Piglin/Hoglin AI, EnchantmentHelper, PlayerList, Forge hooks, Patchouli GUI, 3rd-party) | Keep vanilla ones; replace Forge-hook targets; drop 3rd-party ones until needed |
| Access transformers | ~76 lines, SRG names | Access widener (Fabric) / AT with Mojang names (NeoForge) |
| Forge item hooks | `canApplyAtEnchantingTable`, `getEnchantmentValue`, `isEnchantable`, `canElytraFly`, `elytraFlightTick`, `getEquipmentSlot` | Fabric API equivalents where they exist, mixins otherwise |
| Loot | GlobalLootModifier + reflection into LootTable | Fabric `LootTableEvents.MODIFY`; NeoForge GLM |
| Reflection | 3 spots (screen fields, LootTable/LootPool internals) | Access widener / accessor mixins |
| Rendering | ~28 files (elytra layer, dark armor model, renderers) | Port per loader render API; test visually |

## 5. Big version jumps (beyond the loader port)

- **1.20.1 -> 1.21.1:** item NBT becomes data components; enchantments become
  data-driven (all 8 must be rewritten as JSON + effect components); attribute and
  damage API changes; networking moves to payload codecs; Java 17 -> 21.
- **1.21.1 -> 26.3:** check when we get there (new versioning scheme; expect rendering,
  item model and registry changes). Trinkets Updated covers Fabric slots
  (2026-10-24); Patchouli is optional there.

## 6. Repository layout

One Gradle project per Minecraft version, side by side in this repo:

```
1.20.1/   single Fabric Loom project
1.21.1/   single Fabric Loom project
26.3/     common/ fabric/ neoforge/   (multiloader)
1.21.11/  common/ fabric/ neoforge/   (multiloader)
```

Fabric-only versions skip the common/fabric split: loader-specific code lives in a
`platform` package behind the interfaces from section 3, so it can move into a `common`
module later. Client code stays in the main source set at first (upstream mixes it in);
split it out when the port is stable.

Tooling: Fabric Loom with Mojang mappings + Parchment (keeps names close to upstream, so
diffs stay readable), ModDevGradle for NeoForge. Mod id stays `enigmaticlegacy` unless
decided otherwise.

## 7. Order and milestones

1. **1.20.1 Fabric**
   1. Gradle project builds and launches a dev client with Fabric API, Trinkets, Patchouli.
   2. Copy upstream content: registries, assets, data, lang (CHANGES.md notes every edit).
      Assets, data and Trinkets slots done 2026-10-21 (`tools/sync_resources.py`).
   3. Common abstraction layer (slots, network, player data, platform hooks).
      Done 2026-10-21 (compiles; untested in game): `platform/` interfaces with Fabric
      backends in `fabric/platform/` (Trinkets, Fabric networking, Cardinal Components).
   4. Port game logic and the event handler; port mixins and access widener.
   5. Config (Omniconfig Fabric backend), guide book, client rendering.
      Guide book (Patchouli) integration done 2026-10-23 (devcheck ok; in-world test pending).
   6. Playtest every item in the dev client (checklist in `docs/playtest-1.20.1.md`).
2. **1.21.1 Fabric** (NeoForge skipped: Enigmatic Legacy Plus covers it).
3. **26.3** Fabric (Trinkets Updated) and NeoForge (Curios), one multiloader project (started 2026-10-24).
   Dev-client playtest 39/39 on both loaders (2026-10-24).
4. Mid-point version: **1.21.11** Fabric and NeoForge (1.21.4 has no Fabric Trinkets build), started 2026-10-25.
   Dev-client playtest 40/40 on both loaders (2026-10-25).

All four versions ported (2026-10-25). Upstream `1.20.X` had no new commits as of
2026-10-25, and 26.3 is still the latest stable release (26.4 in snapshots).
Next: release jars, then the permission request (section 8) before any upload.

A version counts as done only after a full dev-client playtest of every item.

## 8. Release rules

- License: same Enigmatic Legacy License, II Edition (copied verbatim in `LICENSE.md`).
- Credit Aizistral as the owner on every page, jar metadata and README.
- Every change from upstream listed in `CHANGES.md`.
- Free only: no paid downloads, no ad-gated or shortened links.
- No public release before the creator has been asked for permission and their answer
  is recorded in `docs/permission/`.

## 9. Open questions

- Mod name on the mod sites (e.g. "Enigmatic Legacy (Fabric)")?
- Upstream has no `package-info.java`, so the license's API exemption covers nothing.
  Ask the creator about it in the permission request.
- ~~26.3 Fabric slot API~~ resolved 2026-10-24: Trinkets Updated.
