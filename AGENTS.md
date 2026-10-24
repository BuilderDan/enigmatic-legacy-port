# Enigmatic Legacy Port

Port the Forge mod Enigmatic Legacy (https://modrinth.com/mod/enigmatic-legacy,
source https://github.com/Aizistral-Studios/Enigmatic-Legacy, branch `1.19.X`, last
supported 1.20.1) to Fabric and NeoForge, for a public Modrinth/CurseForge release.

## Version set (Dan, 2026-10-21)

| MC version | Loaders |
|---|---|
| 1.20.1 | Fabric |
| 1.21.1 | Fabric only (NeoForge skipped 2026-10-21: Enigmatic Legacy Plus covers it) |
| Latest (26.3 per registry; verify) | NeoForge, Fabric |
| Mid-point (e.g. 1.21.4) | NeoForge, Fabric (optional, only if cheap after 1.21.1) |

Order: 1.20.1 Fabric -> 1.21.1 Fabric -> latest on both -> mid-point.
NeoForge 1.21.1 players are pointed to https://modrinth.com/mod/enigmaticlegacy-plus.
Structure: one multiloader project (common + fabric + neoforge) per Minecraft version.

## Hard rules (license: Enigmatic Legacy License II Edition, 08.11.2022)

Read the full text in the upstream repo's `LICENSE.md` before publishing. Summary:

- Credit Aizistral Studios as owner. Never claim ownership of any part.
- Mark every modification clearly (CHANGES file listing what differs from upstream).
- The whole port must be open source (source visible, free to redistribute and modify)
  under a similar license. Keep it in a public git repo.
- No monetization at all: no paid downloads, paywalls, or ad-gated links.
- The license never mentions ports. **Ask the creator for written permission before the
  first public release** (waiver clause). Draft in `IDEAS.md`. Keep their reply in the repo.
- Never publish before permission is recorded or Dan says to go ahead anyway.

## Working notes

- Upstream clone goes in `upstream/` (gitignored, read-only reference).
- Check dependencies first (e.g. Curios for trinkets/relics). Fabric needs an equivalent
  (Trinkets or similar); decide per version.
- Every version needs a real dev-client playtest of every item before it counts as done.
- Game/test instances are large: put them on B:, not C:. Test output stays in `test-output/`.
- Close any Minecraft client/IDE you launched when done.
- Open dev/test clients on Dan's 2nd monitor, never the main one (Dan, 2026-10-21).

## Session / rules / working style

- Read `FEEDBACK.md` open items and `IDEAS.md` at session start.
- Keep replies short. Opus 5.5 by default; Fable only for a port that is stuck.
- Use `explorer` for upstream searches, `worker` for mechanical renames/registry edits,
  `reviewer` before reporting a multi-file change done.
- Feedback from Dan goes into `FEEDBACK.md` before acting on it.

## Ideas

Task Master adds ideas to `IDEAS.md`. Check it for new items.
