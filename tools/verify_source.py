#!/usr/bin/env python3
"""Offline structural checks for the Meteor Ultimate source archive."""
from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java/dev/meteorultimate"
META = ROOT / "src/main/resources/fabric.mod.json"

errors = []
metadata = json.loads(META.read_text(encoding="utf-8"))
if metadata.get("id") != "meteor_ultimate":
    errors.append("Unexpected Fabric mod id")
if metadata.get("depends", {}).get("minecraft") != "1.21.4":
    errors.append("Minecraft dependency is not pinned to 1.21.4")

main = (SRC / "MeteorUltimate.java").read_text(encoding="utf-8")
modules = [
    "AutoSellGui", "WorldExplorer", "SurvivalMonitor", "WaypointBeacon",
    "WaypointManager", "SessionStats", "InventoryMonitor", "ResourceCounter", "CoordinateHud",
]
for name in modules:
    path = SRC / "modules" / f"{name}.java"
    if not path.is_file():
        errors.append(f"Missing module source: {path.name}")
    if f"new {name}()" not in main:
        errors.append(f"Module is not registered: {name}")

sell = (SRC / "modules/AutoSellGui.java").read_text(encoding="utf-8")
for required in ["allowedItems.get().isEmpty()", "blockedItems.get().contains", "previewOnly.get()", "protectHotbar.get()"]:
    if required not in sell:
        errors.append(f"AutoSell safety guard missing: {required}")
explorer = (SRC / "modules/WorldExplorer.java").read_text(encoding="utf-8")
if "ChunkStatus.FULL, false" not in explorer:
    errors.append("WorldExplorer must only inspect already-loaded chunks")

if errors:
    print("FAIL")
    for error in errors:
        print(" -", error)
    sys.exit(1)
print(f"PASS: metadata, {len(modules)} module sources/registrations, AutoSell guards, loaded-chunk guard")
