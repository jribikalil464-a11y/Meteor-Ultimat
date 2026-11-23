# Meteor Ultimate — Minecraft 1.21.4

A lightweight Fabric client addon for Meteor Client. Target runtime: Java 21, Minecraft 1.21.4, Fabric Loader 0.16.10 and the matching Meteor Client artifact.

## Current source modules (v0.6.2-dev)

- **Auto Sell GUI (prototype):** explicit item allowlist/denylist, protected hotbar/named/damaged items, preview-only mode, click pacing and bounded batches. The target server's GUI layout, close-to-submit behavior and rules must be verified before use.
- **World Explorer:** bounded markers for chests, trapped chests, barrels, shulkers and spawners in chunks already loaded by the client. It never requests unloaded chunks.
- **Survival Monitor:** warning-only food, Totem and Elytra durability alerts.
- **Waypoint Beacon:** one manually configured waypoint.
- **Waypoint Manager:** multiple named waypoints persisted in Meteor settings.
- **Session Stats:** local elapsed time and approximate distance travelled.
- **Inventory Monitor:** warning-only low-space and armor-durability alerts.
- **Resource Counter:** counts explicitly selected item types in the player's inventory.
- **Coordinate HUD:** XYZ and dimension readout in the module list.

See `docs/FEATURE-INVENTORY.md` for the implemented/planned distinction and `docs/CHANGELOG-v0.6.2.md` for this revision.

## Build

Requirements: Java 21 and Gradle 8.x with network access to Fabric and Meteor Maven repositories.

```powershell
gradle build
```

The project intentionally does not bundle a Gradle wrapper because the wrapper distribution could not be downloaded in the offline authoring environment. If Gradle is installed, generate a wrapper locally with `gradle wrapper --gradle-version 8.10.2`, then build with `./gradlew.bat build`.

The expected output is under `build/libs/`. Use the JAR whose filename does not end in `-sources.jar` or `-javadoc.jar`.

## Offline source check

```powershell
python tools/verify_source.py
```

This verifies metadata, module source/registration, key Auto Sell safety guards and the loaded-chunk-only Explorer guard. It is not a Java compiler or in-game test.

## Performance and safety design

- Custom scanners are bounded by radius, interval and marker caps.
- World Explorer reads only block entities in already-loaded chunks (`create=false`).
- Inventory and survival monitors only read local player state and issue warnings.
- Resource Counter scans only the 36 player inventory slots.
- Auto Sell uses an explicit allowlist; an empty allowlist means sell nothing. Preview mode is available. Never use it where server rules disallow automation.
- No anti-cheat bypass, packet spoofing, forced chunk loading, dupe exploit or hidden server-data extraction is included.

## Verification status

Source archive integrity and the offline structural check pass. **The Java project has not been compiled and has not been launched in Minecraft in this environment.** Gradle and external Maven dependencies were unavailable because network DNS resolution failed. Build and test against the exact Meteor 1.21.4 artifact installed by the user before treating this as a working JAR.


## Build a mod JAR on Windows

Run `build.bat` with Java 21 installed and internet access. It downloads Gradle 8.10.2, creates the Gradle Wrapper, builds the remapped addon, and copies the resulting JAR to `release/Meteor-Ultimate-1.21.4.jar`. See `BUILD-STATUS.md` for compatibility targets and limitations. A JAR is not included unless a successful build has produced it.


## Cloud build (recommended)

The repository includes `.github/workflows/build.yml`. After uploading this project to a GitHub repository, open **Actions → Build Meteor Ultimate → Run workflow**. The workflow installs Java 21 and Gradle 8.10.2, runs `clean build`, checks that exactly one remapped addon JAR was produced, and uploads it as the `Meteor-Ultimate-Minecraft-1.21.4` artifact. Download that artifact from the completed workflow run. This avoids installing Gradle locally, but GitHub still needs to download the Fabric and Meteor dependencies.

A successful workflow run is the first compilation check; it is not a substitute for launching Minecraft with the matching Fabric Loader and Meteor Client and testing the modules in-game.
