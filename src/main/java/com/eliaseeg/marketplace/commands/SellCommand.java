package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.eliaseeg.marketplace.utils.MessageUtils;

public class SellCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.sell")) {
            MessageUtils.sendMessage(player, "no_permission");
            return true;
        }

        if (args.length != 1) {
            MessageUtils.sendMessage(player, "usage_sell");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            MessageUtils.sendMessage(player, "invalid_price");
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) {
            MessageUtils.sendMessage(player, "must_hold_item");
            return true;
        }

        ItemListing listing = new ItemListing(itemInHand, price, player.getUniqueId());
        player.getInventory().setItemInMainHand(null);

        MessageUtils.sendMessage(player, "listing_item");

        MarketPlace.getInstance().getDatabaseManager().saveItemListing(listing, success -> {
            if (success) {
                MessageUtils.sendMessage(player, "item_listed", "price", price);
                MarketPlace.getInstance().getItemMarketplaceManager().addItemListing(listing);
            } else {
                MessageUtils.sendMessage(player, "listing_failed");
            }
        });

        return true;
    }
}