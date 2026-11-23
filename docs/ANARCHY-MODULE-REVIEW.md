# Anarchy utility review — Minecraft 1.21.4

This document is a feature-selection review, not a claim that every listed module is implemented in this addon.

## Public clients reviewed

- **Meteor Client** — the best fit for this project because Meteor Ultimate is an addon for Meteor. Its module registry includes broad Player, Movement, Render, World and Misc groups.
- **Wurst7** — a long-running utility client; useful as a source of general feature ideas, but do not copy code without checking its license and the exact 1.21.4 source.
- **LiquidBounce** — another established client with a separate architecture; useful for comparing UI and movement/player utility ideas, not as a dependency to mix into Meteor.
- **AnarchyClient** — a newer server-oriented client whose currently advertised build targets a much newer Minecraft version, so it is a feature reference only, not a 1.21.4 dependency.

## Common module ideas worth considering

### Exploration and awareness — highest priority
- Storage ESP / Stash Finder
- Item ESP / Entity ESP
- Waypoints and Tracers
- Fullbright / Light Overlay
- Xray (only where allowed by server rules)
- New-chunk history and an exploration log (custom work; not assumed to exist in the current addon)
- Cave/ore hints restricted to chunks and block data already delivered to the client

### Travel and building
- Elytra Fly / Elytra Boost
- Auto Sprint, Safe Walk and Step
- Scaffold and Highway Builder
- Baritone-assisted pathfinding/mining where the matching Baritone integration is available

### Player and inventory
- Auto Eat, Auto Tool, Auto Replenish, Auto Armor, Auto Totem
- Auto Reconnect, Auto Respawn and Inventory Tweaks

### PvP-oriented modules commonly seen in anarchy clients
- KillAura, CrystalAura, Surround, AutoTrap, AutoCity, AutoWeb and AutoTotem
- These are feature names for comparison only. They are not implemented by Meteor Ultimate, and compatibility/settings need separate review.

## Recommended selection for Meteor Ultimate

1. Keep **World Explorer** as the custom exploration module and extend it with persistent discovery records, filters and export.
2. Reuse Meteor's own **Storage ESP, Stash Finder, Waypoints, Tracers, Fullbright, Auto Eat, Auto Tool, Auto Replenish, Auto Reconnect, Elytra Fly, Safe Walk, Step, Scaffold and Vein Miner** when present in the exact installed 1.21.4 build. Do not duplicate them in the addon.
3. Add custom **Sell Profiles**, **Inventory Cleaner with explicit allowlist**, and **Performance Guard** only after the core addon compiles.
4. Treat PvP modules as optional Meteor base-client functionality; do not copy third-party combat code into this addon.
5. Exclude anti-cheat bypasses, packet-fabrication tricks, dupe exploits and hidden-data extraction.

## Compatibility caveat

Public client feature lists often describe newer Minecraft versions. A feature name appearing in a current project does not prove it exists or compiles against Minecraft 1.21.4. Verify the exact source tag/artifact and license before integrating any code.


## Additions in the current source revision

- Survival Monitor is implemented as a warning-only module for hunger, Totem count and Elytra durability.
- World Explorer remains deliberately limited to already-loaded client chunks.
- Auto Sell remains allowlist-first and does not claim server-confirmed sale success.

Combat automation, anti-cheat evasion, packet manipulation, chunk forcing and hidden-data extraction are intentionally excluded.
