package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUI;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUIListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.view")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        MarketPlace.getInstance().getDatabaseManager().getAllItemListings(listings -> {
            InventoryGUI gui = new InventoryGUI("Marketplace", 6); // 6 rows
            InventoryGUIListener.registerGUI("Marketplace", gui);

            for (int i = 0; i < listings.size(); i++) {
                ItemListing listing = listings.get(i);
                ItemStack displayItem = listing.getItem().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
                    lore.add("");
                    lore.add("----------------");
                    lore.add("Price: " + listing.getPrice());
                    lore.add("Seller: " + MarketPlace.getInstance().getServer().getOfflinePlayer(listing.getSellerUUID()).getName());
                    lore.add("----------------");
                    meta.setLore(lore);
                    displayItem.setItemMeta(meta);
                }
                gui.setItem(i, displayItem, null);
            }

            gui.open(player);
        });

        return true;
    }
}