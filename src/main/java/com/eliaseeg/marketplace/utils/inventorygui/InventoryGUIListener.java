package com.eliaseeg.marketplace.utils.inventorygui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryGUIListener implements Listener {
    private static final Map<String, InventoryGUI> activeGUIs = new HashMap<>();

    public static void registerGUI(String title, InventoryGUI gui) {
        activeGUIs.put(title, gui);
    }

    public static void unregisterGUI(String title) {
        activeGUIs.remove(title);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null) return;
        
        String inventoryTitle = event.getView().getTitle();
        InventoryGUI gui = activeGUIs.get(inventoryTitle.split(" - Page ")[0]);
        
        if (gui != null) {
            event.setCancelled(true);
            gui.handleClick(player, event.getSlot(), clickedInventory);
        }
    }
}