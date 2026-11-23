# Build and test status

Meteor Ultimate is a **Fabric client-side Meteor addon** for Minecraft Java 1.21.4. Its final deliverable is a remapped addon `.jar`, installed with Fabric Loader and a compatible Meteor Client build.

## Verified in this environment
- Source ZIP integrity and static source verifier pass.
- Java 21 is installed.
- Project targets Minecraft 1.21.4, Yarn 1.21.4+build.8, Fabric Loader 0.16.10, and Meteor Client 1.21.4-SNAPSHOT.
- Gradle and dependency artifacts are not cached here; external DNS/network access is unavailable. Thus compilation and in-game testing cannot honestly be claimed.

## Build on Windows 11
1. Install a Java 21 JDK.
2. Extract the archive.
3. Double-click `build.bat` with internet access. It downloads Gradle 8.10.2, creates the Gradle Wrapper, and runs `clean build`.
4. On success, install `release/Meteor-Ultimate-1.21.4.jar` in the Fabric `mods` folder alongside the matching Meteor Client JAR.

If the build fails, retain the full terminal output. Any Meteor API incompatibility must be corrected against the exact dependency version.

## Source modules
AutoSellGui, WorldExplorer, SurvivalMonitor, WaypointBeacon, WaypointManager, SessionStats, InventoryMonitor, ResourceCounter, CoordinateHud.

## Known limits
- AutoSell depends on the target server's exact GUI title, row count, slot behavior, and sale confirmation. Empty allowlist means sell nothing. A closed GUI is not proof of a successful sale.
- WorldExplorer inspects only chunks already loaded by the client; it does not load chunks or reveal hidden/unloaded server data.
- Waypoints, resource counters, and HUD are client-side helpers.
- The full roadmap is not all implemented.
- No successful compilation or Minecraft runtime validation is claimed until the Windows build and smoke tests pass.
