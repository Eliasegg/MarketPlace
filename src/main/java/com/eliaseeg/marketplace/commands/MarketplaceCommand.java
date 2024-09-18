package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        player.sendMessage("Fetching marketplace listings...");

        MarketPlace.getInstance().getDatabaseManager().getAllItemListings(listings -> {
            if (listings.isEmpty()) {
                player.sendMessage("There are no items currently for sale in the marketplace.");
            } else {
                player.sendMessage("Items for sale in the marketplace:");
                for (ItemListing listing : listings) {
                    String displayName = listing.getItem().getType().name();
                    if (listing.getItem().hasItemMeta()) {
                        displayName = listing.getItem().getItemMeta().getDisplayName();
                    }
                    player.sendMessage(String.format("- %s: %.2f (Seller: %s)",
                            displayName,
                            listing.getPrice(),
                            MarketPlace.getInstance().getServer().getOfflinePlayer(listing.getSellerUUID()).getName()));
                }
            }
        });

        return true;
    }
}