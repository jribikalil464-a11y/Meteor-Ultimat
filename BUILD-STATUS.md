name: Build Meteor Ultimate

on:
  push:
    branches:
      - main
      - master
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build:
    name: Build Minecraft 1.21.4 addon
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout source
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Gradle 8.10.2
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.10.2'

      - name: Verify project structure
        run: python tools/verify_source.py

      - name: Build remapped mod JAR
        run: gradle --no-daemon clean build --stacktrace

      - name: Upload runtime JAR
        uses: actions/upload-artifact@v4
        with:
          name: Meteor-Ultimate-Minecraft-1.21.4
          path: build/libs/*-0.6.2-dev.jar
          if-no-files-found: error
          retention-days: 14
