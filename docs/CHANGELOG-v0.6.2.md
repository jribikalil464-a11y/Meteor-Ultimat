# Meteor Ultimate v0.6.2-dev

- Added a GitHub Actions build workflow for Minecraft 1.21.4 and Java 21.
- The workflow installs Gradle 8.10.2, compiles the project, stages the remapped addon JAR, and uploads it as a downloadable artifact.
- Added a SHA-256 checksum alongside the artifact.
- Kept the existing module scope and loaded-chunk-only exploration guard unchanged.

## Verification boundary

The workflow configuration and source structure can be checked locally, but the workflow has not run in this environment. No compiled JAR or in-game compatibility claim is included in this source package.
