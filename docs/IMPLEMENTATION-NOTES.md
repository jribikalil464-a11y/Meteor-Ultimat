# Implementation notes

## Auto Sell GUI

State machine: `IDLE -> PICKED_UP -> PLACE_PENDING -> IDLE` for each eligible stack.

Safety properties:
- Exact title and row-count validation.
- Empty whitelist means sell nothing.
- Only explicitly selected item types are moved.
- One stack is moved at a time with a configurable delay.
- Hotbar, custom-named and damaged-item protections are available.
- Unexpected screens stop the module.
- The module does not use OCR, fabricated packets, anti-cheat bypasses or guessed server commands.

A source-order bug was fixed in this revision: the candidate stack is now read before testing destination-slot insertion compatibility.

Remaining verification:
1. Compile against the exact Meteor 1.21.4 artifact.
2. Confirm which Sell GUI slots accept items on the target server.
3. Verify the actual menu-opening command before enabling repeat.
4. Test with a low-value item only where server rules permit automation.
5. Confirm success from an actual server response or balance change; menu closure alone is not confirmation.

## World Explorer

- Scans a bounded square around the player every 10 client ticks.
- Reads only loaded `WorldChunk` instances using `getChunk(..., ChunkStatus.FULL, false)`.
- Reads each chunk's existing block-entity map; no chunk loading or packet manipulation.
- Limits the cached marker count and render work.
- Targets chests, trapped chests, barrels, shulker boxes and spawners. Ender chests are not included by the current block-entity filter.

This does not detect hidden/unloaded data, ores, cave networks, generated structures in unexplored chunks, or bases with certainty. Those require separate, carefully bounded implementations and cannot be inferred from data the client does not have.

## Waypoint Beacon

- Displays one manually configured X/Y/Z waypoint as a small 3D outline.
- Shows approximate distance in the module info string.
- Does not scan chunks, discover locations, or save multiple waypoint profiles.

## Auto Sell GUI follow-up

- If automatic closing is disabled, the module now stops and leaves the sell menu open for manual submission instead of repeatedly reaching the batch limit without progress.
- Automatic repeat remains dependent on a user-verified server command and a server that permits the workflow.


## v0.6.0-dev additions

- Inventory Monitor: warning-only free-slot and equipped-armor durability checks with a cooldown.
- Resource Counter: counts only item types explicitly selected by the user in the 36 main inventory slots.
- Coordinate HUD: reports XYZ and dimension through the module info string.
- Waypoint Manager refreshes parsed settings before reporting its information string.
- `tools/verify_source.py` provides offline structural checks; it does not replace compilation or in-game tests.
