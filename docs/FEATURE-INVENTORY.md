# Meteor Ultimate — Feature Inventory (Minecraft 1.21.4)

Status labels:
- **Source implemented:** Java source exists and is registered; not necessarily compiled or runtime-tested.
- **Prototype:** source exists but depends on live server/client behavior that has not been verified.
- **Planned:** documentation only; no implementation claim.

## Source implemented in v0.6.0-dev

| Module | Status | Scope |
|---|---|---|
| Auto Sell GUI | Prototype | Explicit allowlist/denylist, item protections, preview-only mode, click pacing and batch limit. Requires the exact server GUI and rules to be validated. |
| World Explorer | Source implemented | Bounded markers for storage blocks, shulkers and spawners in already-loaded chunks only. |
| Survival Monitor | Source implemented | Warning-only hunger, Totem and Elytra durability alerts. |
| Waypoint Beacon | Source implemented | One manually configured coordinate marker and distance. |
| Waypoint Manager | Source implemented | Multiple named waypoints stored in Meteor settings and rendered locally. |
| Session Stats | Source implemented | Local session timer and approximate distance travelled. |
| Inventory Monitor | Source implemented | Warning-only low-free-slot and equipped-armor durability alerts. |
| Resource Counter | Source implemented | Counts explicitly selected items in the player's 36-slot inventory. |
| Coordinate HUD | Source implemented | XYZ and dimension in the Meteor module-list info string. |

## Planned — not implemented

- Discovery Journal, new-chunk history, portal network, route planner, map dashboard and waypoint import/export.
- Sell profiles, detailed dry-run reasons, GUI layout inspector, audit log and server-confirmation parser.
- Inventory rule engine, sort preview, open-container search, storage labels and restock checklist.
- Effect timers, death bookmarks, time-by-dimension and travel segments.
- Shared scan budget, adaptive scan rate, profiler, cache inspector and diagnostics export.
- Control Center, profiles, profile diff, setup wizard, config backup/restore and localization polish.
- Base Operations Board, Farm Session Tracker, Crafting Planner, Build Material Planner and Session Timeline.

## Existing Meteor features — reuse rather than duplicate

Meteor already supplies overlapping modules in many builds, such as Storage ESP, Stash Finder, Waypoints, Auto Eat, Auto Tool, Auto Replenish and Safe Walk. Availability must be checked against the exact installed Meteor build before implementing a duplicate.

## Compatibility and verification

- Target: Minecraft Java 1.21.4, Java 21, Fabric Loader 0.16.10 and a matching Meteor Client 1.21.4 artifact.
- The offline structural check is `python tools/verify_source.py`.
- Java compilation and in-game tests are not claimed. The environment had Java 21 but no Gradle executable/wrapper and could not resolve external dependencies because network DNS was unavailable.
- Auto Sell is disabled unless the user enables it, sells nothing with an empty allowlist, and must only be used where server rules permit. A closed GUI is not proof of a successful sale.
- World Explorer reads client-known data in loaded chunks only. It does not expose hidden data, force chunks to load, or inspect server-only inventories.
