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
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.10.2'

      - name: Build mod
        run: gradle --no-daemon clean build --stacktrace

      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: Meteor-Ultimate-Minecraft-1.21.4
          path: build/libs/*.jar
          if-no-files-found: error
