# Build Meteor Ultimate in GitHub Actions

1. Create a GitHub repository for this source and upload the project files.
2. Open the repository's **Actions** tab.
3. Select **Build Meteor Ultimate**.
4. Choose **Run workflow** and run it on the default branch.
5. Wait for the job to finish. A green check means Gradle completed and the remapped JAR passed the workflow's file checks.
6. Open the completed run and download the artifact named `Meteor-Ultimate-Minecraft-1.21.4`.
7. Extract the ZIP. Place `Meteor-Ultimate-1.21.4.jar` in `.minecraft/mods` beside Fabric Loader and the matching Meteor Client build.

If the workflow fails, open the failed job and copy the complete Gradle error section. A failed compilation must be fixed before the JAR can be treated as usable.

The artifact is retained by GitHub Actions for 30 days. Re-run the workflow to create a fresh artifact later.
