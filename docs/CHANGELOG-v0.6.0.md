# Changelog — v0.6.0-dev

## Added
- **Inventory Monitor:** warning-only low-free-slot and equipped-armor durability alerts with configurable thresholds and cooldown.
- **Resource Counter:** counts user-selected item types in the player's 36-slot inventory; no container scanning.
- **Coordinate HUD:** compact XYZ and dimension readout in Meteor's module list.
- Offline source verification script for metadata, module registration, AutoSell safety guards and loaded-chunk-only exploration.

## Improved
- Waypoint Manager refreshes its parsed waypoint list before reporting its module-list information, including when rendering is disabled.
- Project metadata and Gradle project name updated for Meteor Ultimate v0.6.0 development.
- Documentation now separates implemented source, source prototypes, and planned work.

## Not claimed
- No compiled JAR or in-game compatibility is claimed. Gradle and the Meteor dependency could not be downloaded in the offline build environment.
- Sell GUI behavior and the target server's confirmation messages still require manual verification.
