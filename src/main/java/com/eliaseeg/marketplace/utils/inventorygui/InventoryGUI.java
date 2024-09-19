package com.eliaseeg.marketplace.utils.inventorygui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryGUI {

    private final String title;
    private final int rows;
    private final Inventory inventory;
    private final Map<Integer, ClickAction> actions;
    private final boolean isConfirmationGUI;
    private final List<ItemStack> items;
    private int currentPage;

    public InventoryGUI(String title, int rows, boolean isConfirmationGUI) {
        this.title = title;
        this.rows = Math.min(6, Math.max(1, rows));
        this.inventory = Bukkit.createInventory(null, this.rows * 9, title);
        this.actions = new HashMap<>();
        this.isConfirmationGUI = isConfirmationGUI;
        this.items = new ArrayList<>();
        this.currentPage = 0;
        
        if (!isConfirmationGUI) {
            fillBottomRow();
        }
    }

    public void addItem(ItemStack item, ClickAction action) {
        items.add(item);
        if (action != null) {
            actions.put(items.size() - 1, action);
        }
        updateInventory();
    }

    public void setItem(int slot, ItemStack item, ClickAction action) {
        while (items.size() <= slot) {
            items.add(null);
        }
        items.set(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
        updateInventory();
    }

    public void refresh(Player player) {
        updateInventory();
        player.getOpenInventory().getTopInventory().setContents(inventory.getContents());
    }

    public void handleClick(Player player, int slot) {
        if (!isConfirmationGUI && slot >= (rows - 1) * 9) {
            if (slot == rows * 9 - 9 && currentPage > 0) {
                currentPage--;
                updateInventory();
                player.getOpenInventory().getTopInventory().setContents(inventory.getContents());
            } else if (slot == rows * 9 - 1 && (currentPage + 1) * ((rows - 1) * 9) < items.size()) {
                currentPage++;
                updateInventory();
                player.getOpenInventory().getTopInventory().setContents(inventory.getContents());
            }
            return;
        }

        int globalSlot = currentPage * ((rows - 1) * 9) + slot;
        ClickAction action = actions.get(globalSlot);
        if (action != null) {
            action.onClick(player);
        }
    }

    private void updateInventory() {
        inventory.clear();
        int startIndex = currentPage * ((rows - 1) * 9);
        int endIndex = Math.min(startIndex + ((rows - 1) * 9), items.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(i - startIndex, items.get(i));
        }

        if (!isConfirmationGUI) {
            fillBottomRow();
            if (currentPage > 0) {
                inventory.setItem(rows * 9 - 9, createNavigationItem(Material.ARROW, "Previous Page"));
            }
            if ((currentPage + 1) * ((rows - 1) * 9) < items.size()) {
                inventory.setItem(rows * 9 - 1, createNavigationItem(Material.ARROW, "Next Page"));
            }
        }
    }

    private void fillBottomRow() {
        ItemStack filler = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        for (int i = (rows - 1) * 9; i < rows * 9; i++) {
            inventory.setItem(i, filler);
        }
    }

    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public interface ClickAction {
        void onClick(Player player);
    }
}