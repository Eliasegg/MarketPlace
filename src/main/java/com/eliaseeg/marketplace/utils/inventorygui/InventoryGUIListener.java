package com.eliaseeg.marketplace.utils.inventorygui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
        
        String inventoryTitle = event.getView().getTitle();
        InventoryGUI gui = activeGUIs.get(inventoryTitle);
        
        if (gui != null) {
            event.setCancelled(true);
            if (clickedInventory != null && clickedInventory.equals(event.getView().getTopInventory())) {
                gui.handleClick(player, event.getSlot());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String inventoryTitle = event.getView().getTitle();
        if (activeGUIs.containsKey(inventoryTitle)) {
            event.setCancelled(true);
        }
    }
}