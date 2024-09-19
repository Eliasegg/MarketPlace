package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.sell")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /sell <price>");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid price. Please enter a valid number.");
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) {
            player.sendMessage("You must be holding an item to sell.");
            return true;
        }

        ItemListing listing = new ItemListing(itemInHand, price, player.getUniqueId());
        player.sendMessage("Listing your item for sale...");

        MarketPlace.getInstance().getDatabaseManager().saveItemListing(listing, success -> {
            if (success) {
                player.getInventory().setItemInMainHand(null);
                player.sendMessage("Your item has been listed in the marketplace for " + price + ".");
                MarketPlace.getInstance().getItemMarketplaceManager().addItemListing(listing);
            } else {
                player.sendMessage("Failed to list your item. Please try again later.");
            }
        });

        return true;
    }
}