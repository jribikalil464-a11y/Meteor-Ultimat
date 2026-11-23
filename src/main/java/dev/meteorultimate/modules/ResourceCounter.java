package dev.meteorultimate.modules;

import dev.meteorultimate.MeteorUltimate;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;

/** Counts only user-selected item types in the player's local inventory. */
public class ResourceCounter extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<List<Item>> trackedItems = sg.add(new ItemListSetting.Builder()
        .name("tracked-items").description("Items to count in your inventory. Select them explicitly.")
        .defaultValue().build());

    public ResourceCounter() {
        super(MeteorUltimate.CATEGORY, "resource-counter", "Counts selected resources in the local player inventory.");
    }

    @Override public String getInfoString() {
        if (mc.player == null) return "No player";
        if (trackedItems.get().isEmpty()) return "Select items";
        int total = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (!stack.isEmpty() && trackedItems.get().contains(stack.getItem())) total += stack.getCount();
        }
        return total + " tracked";
    }
}
