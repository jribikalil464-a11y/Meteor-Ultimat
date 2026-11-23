# Meteor Ultimate 1.21.4 — v0.3.0 source update

## Added
- `Waypoint Beacon`: one manually configured X/Y/Z marker, rendered as a 3D outline with a distance readout.

## Fixed
- Auto Sell now stops and leaves the GUI open for manual submission when `close-to-sell` is disabled. It no longer repeatedly reaches the batch limit without progress.

## Documentation
- Updated the feature inventory for the new waypoint module.
- Corrected World Explorer's target list: the current filter does not include Ender Chests.
- Clarified which waypoint features are still planned.

## Verification limits
- Source and archive checks only. No Gradle executable or Gradle Wrapper is present in this environment, so no JAR build or Minecraft runtime test is claimed.
