package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        player.sendMessage("Generating black market listings...");

        MarketPlace.getInstance().getDatabaseManager().getAllItemListings(allListings -> {
            List<ItemListing> blackMarketListings = new ArrayList<>();

            // Randomly select up to 5 items for the black market
            Collections.shuffle(allListings);
            for (int i = 0; i < Math.min(5, allListings.size()); i++) {
                ItemListing original = allListings.get(i);
                ItemListing discounted = new ItemListing(original.getItem(), original.getPrice() * 0.5, original.getSellerUUID());
                blackMarketListings.add(discounted);
            }

            if (blackMarketListings.isEmpty()) {
                player.sendMessage("There are no items available in the black market at the moment.");
            } else {
                player.sendMessage("Black Market Items (50% off):");
                for (ItemListing listing : blackMarketListings) {
                    String displayName = listing.getItem().getType().name();
                    if (listing.getItem().hasItemMeta()) {
                        displayName = listing.getItem().getItemMeta().getDisplayName();
                    }
                    player.sendMessage(String.format("- %s: %.2f (Original: %.2f)",
                            displayName,
                            listing.getPrice(),
                            listing.getPrice() * 2));
                }
            }
        });

        return true;
    }
}