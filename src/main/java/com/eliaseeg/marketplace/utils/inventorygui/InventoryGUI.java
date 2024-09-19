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
    private final List<Inventory> pages;
    private final Map<Integer, ClickAction> actions;

    public InventoryGUI(String title, int rows) {
        this.title = title;
        this.rows = Math.min(6, Math.max(1, rows));
        this.pages = new ArrayList<>();
        this.actions = new HashMap<>();
        addPage();
    }

    public void addItem(ItemStack item) {
        Inventory currentPage = getCurrentPage();
        if (currentPage.firstEmpty() == -1) {
            addPage();
            currentPage = getCurrentPage();
        }
        currentPage.addItem(item);
    }

    public void setItem(int slot, ItemStack item, ClickAction action) {
        int page = slot / (rows * 9);
        int pageSlot = slot % (rows * 9);
        while (pages.size() <= page) {
            addPage();
        }
        pages.get(page).setItem(pageSlot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    public void open(Player player) {
        player.openInventory(pages.get(0));
    }

    public void handleClick(Player player, int slot, Inventory clickedInventory) {
        int page = pages.indexOf(clickedInventory);
        if (page == -1) return;

        int globalSlot = page * (rows * 9) + slot;
        ClickAction action = actions.get(globalSlot);
        if (action != null) {
            action.onClick(player);
        }

        if (slot == rows * 9 - 9 && page > 0) {
            player.openInventory(pages.get(page - 1));
        } else if (slot == rows * 9 - 1 && page < pages.size() - 1) {
            player.openInventory(pages.get(page + 1));
        }
    }

    private void addPage() {
        Inventory page = Bukkit.createInventory(null, rows * 9, title + " - Page " + (pages.size() + 1));
        if (!pages.isEmpty()) {
            page.setItem(rows * 9 - 9, createNavigationItem(Material.ARROW, "Previous Page"));
        }
        if (!title.startsWith("Confirm")) {
            page.setItem(rows * 9 - 1, createNavigationItem(Material.ARROW, "Next Page"));
        }
        pages.add(page);
        
        if (pages.size() > 1 && !title.startsWith("Confirm")) {
            Inventory previousPage = pages.get(pages.size() - 2);
            previousPage.setItem(rows * 9 - 1, createNavigationItem(Material.ARROW, "Next Page"));
        }
    }

    private Inventory getCurrentPage() {
        return pages.get(pages.size() - 1);
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

    public interface ClickAction {
        void onClick(Player player);
    }
}