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
import java.util.Collections;
import java.util.List;

public class BlackMarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.blackmarket")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        MarketPlace.getInstance().getDatabaseManager().getAllItemListings(allListings -> {
            InventoryGUI gui = new InventoryGUI("Black Market", 3);
            InventoryGUIListener.registerGUI("Black Market", gui);

            List<ItemListing> blackMarketListings = new ArrayList<>();

            // Randomly select up to 5 items for the black market
            Collections.shuffle(allListings);
            for (int i = 0; i < Math.min(5, allListings.size()); i++) {
                ItemListing original = allListings.get(i);
                ItemListing discounted = new ItemListing(original.getItem(), original.getPrice() * 0.5, original.getSellerUUID());
                blackMarketListings.add(discounted);
            }

            for (int i = 0; i < blackMarketListings.size(); i++) {
                ItemListing listing = blackMarketListings.get(i);
                ItemStack displayItem = listing.getItem().clone();
                ItemMeta meta = displayItem.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
                    lore.add("Discounted Price: " + listing.getPrice());
                    lore.add("Original Price: " + (listing.getPrice() * 2));
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