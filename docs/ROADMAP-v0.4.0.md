# Meteor Ultimate 1.21.4 — v0.4 development roadmap

This is a source-level development plan. A listed item is not implemented unless it appears in the implemented section and has source code in this archive.

## Added in this source revision

- **Waypoint Manager:** multiple named coordinates in one setting, rendered as markers. Entries use `name,x,y,z;name2,x,y,z`. Meteor's settings persistence stores the value with the module configuration.
- **Session Stats:** local elapsed time and approximate travelled distance. Large position jumps are excluded from distance totals to reduce teleport inflation.

## Existing custom modules retained

- Auto Sell GUI — explicit allowlist, denylist, inventory protections, pacing, bounded batch and optional user-supplied reopen command. Still requires real server GUI validation.
- World Explorer — bounded markers for block entities in already-loaded chunks only.
- Survival Monitor — warning-only food, Totem and Elytra durability checks.
- Waypoint Beacon — single-coordinate compatibility module; Waypoint Manager supersedes it for multiple destinations.

## Planned implementation order

### Explorer Plus
1. Scan scheduler settings and incremental scan budget.
2. Persistent discovery history with world/server separation and an explicit clear button.
3. Waypoint import/export using a plain text format.
4. Portal markers based only on client-known portal blocks and loaded chunks.
5. Searchable local waypoint list and nearest-waypoint HUD.

### Sell Plus
1. Multiple named sale profiles (each with explicit allowlist and denylist).
2. Preview-only mode that lists candidate stacks without moving them.
3. Slot-rule diagnostics for server-provided sell menus.
4. Optional server confirmation parsing after the user supplies exact message examples.
5. Per-cycle local audit log; never label a batch as successful without server confirmation.

### Inventory Plus
1. Preview-first inventory rules.
2. Protected-item presets for named, enchanted, damaged, rare and user-pinned items.
3. Sorting suggestions that do not move items until the user confirms.
4. Container transfer helpers restricted to screens the player opened.

### Survival Plus
1. Configurable alert groups and per-alert cooldowns.
2. Armor durability summary and potion-effect timer HUD.
3. Optional low-resource warnings and reconnect reminder.
4. No automatic movement or risky inventory actions by default.

### Performance Plus
1. Configurable scan intervals and maximum work per tick.
2. Pause scanning while chat, menus or loading screens are open.
3. Marker distance culling and render-count caps.
4. Diagnostics for module scan time and cached marker count.
5. Low-resource preset that reduces scan frequency and render density.

### Interface Plus
1. Consistent module naming, descriptions and setting groups.
2. Built-in quick-start guide and safe defaults.
3. Exportable configuration notes and profile documentation.

## Additional ideas worth considering

- **Route Notes:** save a sequence of waypoints for a journey, without automated movement.
- **Base Visit Checklist:** user-defined checklist attached to a waypoint (doors, storage, supplies), stored locally.
- **Resource Ledger:** manually record gathered item totals per session; no hidden server-data reading.
- **Container Labeler:** locally tag containers the player has opened, with user-entered labels.
- **Death/Disconnect Bookmark:** save the last known player coordinates locally when the client receives the relevant event.
- **Dimension Dashboard:** show current dimension, coordinates, nearest saved waypoint and session timer in one compact HUD.
- **Sell Dry Run:** show exactly which stacks would qualify under the current sale rules, without clicking anything.
- **Configuration Validator:** warn about conflicting rules, such as an item appearing in both allowlist and denylist.

## Performance and compatibility rules

- Target Java 21, Minecraft 1.21.4 and the exact matching Meteor Client artifact.
- Do not force-load chunks, fabricate packets, read server-only state or bypass anti-cheat protections.
- Keep scanning bounded, cached and configurable; never scan the entire world every tick.
- Prefer reusing base Meteor modules where the installed build already provides the same function.
- Every automation feature must have a preview, clear stop condition and conservative default.
