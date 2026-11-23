# Meteor Ultimate 1.21.4 — v0.5.0 Expansion Blueprint

This document expands the v0.4.0 source roadmap. It is a design/implementation plan, not a claim that these features are already implemented.

## Product goals

- Keep the addon lightweight and compatible with Minecraft 1.21.4, Java 21 and the matching Meteor Client artifact.
- Prefer Meteor's built-in modules when they already provide the feature.
- Keep all scans bounded, cached and restricted to information the client has legitimately received.
- Make destructive or server-interacting actions preview-first, opt-in, rate-limited and stoppable.
- Never claim a sale, transfer, discovery or server action succeeded without an observable confirmation.

## A. Ultimate Explorer

1. **Discovery Journal** — local records of locations the client has actually observed, grouped by world/server identity and dimension.
2. **New-Chunk Notes** — record chunk coordinates when the client first observes them; no chunk forcing or hidden terrain inspection.
3. **Portal Network** — user-confirmed portal waypoints, dimension labels and manually linked exits.
4. **Route Planner** — named ordered waypoint routes, distance estimates and route notes; navigation remains informational, not automated movement.
5. **Biome & Landmark Notebook** — save the player's current known biome and coordinates when the user creates a bookmark.
6. **Cave Entrance Bookmarks** — manually mark entrances the player has visited; no inference about unseen underground areas.
7. **Container Visit Log** — record containers the player opens, optionally with a user-entered label; do not read unopened or server-only contents.
8. **Exploration Export** — export/import waypoints and discovery notes as plain text or JSON with a preview and duplicate handling.
9. **Map Dashboard** — local map-like overview of saved waypoints and discovered points; no claim of a full-world map from unseen chunks.
10. **Nearest Point HUD** — nearest saved waypoint, dimension and distance with configurable update interval.

## B. Ultimate Sell

1. **Multiple Sell Profiles** — separate explicit allowlists/denylists for different server menus or sale categories.
2. **Sell Dry Run** — preview each candidate stack and explain why it qualifies or is protected.
3. **GUI Layout Inspector** — user-triggered diagnostic of visible menu slots, item names and slot positions; no automatic assumptions about decorative slots.
4. **Profile Validator** — detect empty allowlists, allow/deny conflicts, invalid item identifiers and unsafe settings.
5. **Batch Limits** — maximum stacks/items per cycle, click delay and a global stop control.
6. **Confirmation Watcher** — optional parsing of exact user-provided server confirmation messages; unknown messages must remain unconfirmed.
7. **Sale Audit Log** — timestamp, selected item counts, submitted actions and confirmation state (confirmed/unconfirmed/failed).
8. **Manual Review Mode** — prepare a sale batch but leave final submission to the player.
9. **Server Presets** — user-created presets; never ship fabricated server commands or guessed menu layouts.
10. **Emergency Stop** — one keybind to stop queued clicks and disable repeat scheduling.

## C. Ultimate Inventory & Storage

1. **Inventory Rule Engine** — explicit rules for keep, protect, mark and optionally move; preview before any movement.
2. **Protected Item Presets** — named, enchanted, damaged, user-pinned and configurable rare items.
3. **Sort Preview** — show proposed slot changes before applying them.
4. **Container Organizer** — organize only a container screen opened by the player, with a preview and bounded transfer rate.
5. **Storage Labels** — local labels and notes attached to observed container coordinates.
6. **Search Opened Container** — search item names/counts only in the currently open, client-visible container.
7. **Restock Checklist** — user-defined target quantities and a local list of missing supplies; no hidden storage scanning.
8. **Undo/Recovery Notes** — retain a local record of the last planned operation; do not promise rollback of server-confirmed transfers.
9. **Duplicate Rule Warning** — explain conflicting rules before an operation starts.
10. **Item Value Notes** — user-entered values for personal tracking only; do not invent server prices.

## D. Ultimate Survival & Travel HUD

1. **Armor Dashboard** — durability for each equipped armor piece.
2. **Effect Timers** — remaining duration for visible potion effects.
3. **Food & Totem Alerts** — configurable thresholds and per-alert cooldowns.
4. **Elytra Status** — durability and equipped-state warning.
5. **Fall/Water/Fire Context Alerts** — informational warnings based on the player's current client-known state.
6. **Death Bookmark** — save last known coordinates when a death event is observed; label uncertain cases clearly.
7. **Reconnect Reminder** — local reminder after disconnect; no repeated reconnect loops by default.
8. **Dimension Dashboard** — dimension, coordinates, biome (when available), nearest waypoint and session timer.
9. **Travel Statistics** — session distance, time by dimension and manually named journey segments.
10. **Restock & Repair Checklist** — user-defined reminders before a journey.

## E. Performance & Diagnostics

1. **Global Scan Budget** — shared per-tick work budget for custom scanners.
2. **Adaptive Scan Rate** — reduce scan frequency when FPS or client load drops, if reliable metrics are available.
3. **Render Distance Culling** — hide distant markers and cap visible marker count.
4. **Screen-Aware Pause** — pause nonessential scanning while menus, chat or loading screens are open.
5. **Cache Inspector** — show cached marker counts and last scan time.
6. **Module Timing Diagnostics** — optional profiling of custom module work with minimal overhead.
7. **Low-Resource Preset** — conservative intervals, marker caps and HUD refresh rates.
8. **Diagnostics Export** — export addon version, Minecraft version, Java version and sanitized settings; exclude account identifiers and chat contents.
9. **Compatibility Check** — warn when Minecraft/Meteor versions do not match the declared target.
10. **Safe Defaults Reset** — restore conservative defaults without wiping saved waypoints unless separately confirmed.

## F. Control Center & Quality of Life

1. **Ultimate Control Center** — searchable custom-module overview with status and keybind hints.
2. **Profiles** — named configurations for exploration, selling, building and low-resource play.
3. **Profile Diff** — preview which settings change before switching profiles.
4. **Quick Actions** — safe toggles for pausing scanners, enabling HUDs and stopping sale queues.
5. **Setup Wizard** — explain required configuration and highlight unconfigured commands/allowlists.
6. **Localization** — Arabic and English labels where the Meteor API permits; avoid hard-coded UI assumptions.
7. **Settings Groups** — consistent General, Display, Safety and Performance sections.
8. **Config Backup** — export/import addon settings with validation and confirmation.
9. **In-Game Help** — concise descriptions and examples for every custom setting.
10. **Module Conflict Hints** — warn when two custom modules are configured to perform overlapping work.

## G. Additional ideas for later releases

- **Base Operations Board:** a user-maintained checklist for farms, storage, repairs and supplies at a saved base waypoint.
- **Farm Session Tracker:** manually start/stop a farm session and record elapsed time and user-entered output totals.
- **Resource Ledger:** record gathered/crafted/sold totals entered by the player or observed in permitted client-visible screens.
- **Crafting Planner:** calculate ingredient requirements from user-selected recipes using client-available recipe data.
- **Build Material Planner:** maintain a target block list and compare it with the player's inventory.
- **Waypoint Sharing Code:** export a waypoint bundle as text for friends; importing always previews coordinates first.
- **Personal Notes & Tags:** attach notes/tags to waypoints, routes and container labels.
- **Session Timeline:** local timeline of dimension changes, death bookmarks, manual waypoints and user-triggered events.
- **Server Rules Reminder:** user-written reminders per server profile; no automatic rule scraping or enforcement bypass.
- **Accessibility HUD:** scalable text, high-contrast marker options and reduced visual clutter.

## Suggested delivery phases

### Phase 1 — reliability foundation
- Verify and compile the existing v0.4 source against the exact Meteor 1.21.4 artifact.
- Fix API/compiler issues before expanding automation.
- Add compatibility diagnostics, scan budget, emergency stop and consistent settings groups.

### Phase 2 — Explorer Plus
- Discovery Journal, portal/route notes, nearest waypoint HUD, import/export and container visit labels.

### Phase 3 — Sell Plus
- Multiple profiles, dry run, GUI inspector, validator, audit log and confirmation watcher.

### Phase 4 — Inventory Plus
- Protected-item presets, sort preview, open-container search, labels and restock checklist.

### Phase 5 — Survival/HUD Plus
- Armor/effect dashboard, dimension dashboard, death bookmark and travel statistics.

### Phase 6 — Control Center & polish
- Profiles, profile diff, setup wizard, localization, backup/restore and accessibility options.

## Explicit exclusions

- Anti-cheat bypasses, packet spoofing, dupe exploits, server-side or hidden-data extraction, forced chunk loading, and tools intended to evade server rules.
- Combat automation copied from other clients.
- Automatic movement or irreversible inventory/sale actions without clear user initiation and a stop mechanism.

## Status convention

A feature is **implemented** only when source code exists, is registered, compiles against the target dependency and passes relevant tests. A source-only prototype is not build-verified. A planned item in this document is not implemented.
