package dev.meteorultimate;

import dev.meteorultimate.modules.AutoSellGui;
import dev.meteorultimate.modules.WorldExplorer;
import dev.meteorultimate.modules.SurvivalMonitor;
import dev.meteorultimate.modules.WaypointBeacon;
import dev.meteorultimate.modules.WaypointManager;
import dev.meteorultimate.modules.SessionStats;
import dev.meteorultimate.modules.InventoryMonitor;
import dev.meteorultimate.modules.ResourceCounter;
import dev.meteorultimate.modules.CoordinateHud;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class MeteorUltimate extends MeteorAddon {
    public static final Category CATEGORY = new Category("Meteor Ultimate");

    @Override
    public void onInitialize() {
        Modules.get().add(new AutoSellGui());
        Modules.get().add(new WorldExplorer());
        Modules.get().add(new SurvivalMonitor());
        Modules.get().add(new WaypointBeacon());
        Modules.get().add(new WaypointManager());
        Modules.get().add(new SessionStats());
        Modules.get().add(new InventoryMonitor());
        Modules.get().add(new ResourceCounter());
        Modules.get().add(new CoordinateHud());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "dev.meteorultimate";
    }
}
