package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import com.eliaseeg.marketplace.models.Transaction;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUI;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUIListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

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

        openMarketplace(player);

        return true;
    }

    private void openMarketplace(Player player) {
        List<ItemListing> listings = MarketPlace.getInstance().getItemMarketplaceManager().getAllListings();
        InventoryGUI gui = new InventoryGUI("Marketplace", 6);
        InventoryGUIListener.registerGUI("Marketplace", gui);

        for (ItemListing listing : listings) {
            ItemStack displayItem = listing.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
                lore.add("");
                lore.add("----------------");
                lore.add("Price: " + listing.getPrice());
                lore.add("Seller: " + Bukkit.getOfflinePlayer(listing.getSellerUUID()).getName());
                lore.add("----------------");
                lore.add("Click to purchase");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.addItem(displayItem, p -> openConfirmationGUI(p, listing));
        }

        gui.open(player);
    }

    private void openConfirmationGUI(Player player, ItemListing listing) {
        Economy economy = MarketPlace.getInstance().getEconomy();
        double price = listing.getPrice();

        if (!economy.has(player, price)) {
            player.sendMessage("You don't have enough money to purchase this item.");
            return;
        }

        InventoryGUI confirmGui = new InventoryGUI("Confirm Purchase", 3);
        InventoryGUIListener.registerGUI("Confirm Purchase", confirmGui);

        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName("Confirm Purchase");
        confirmItem.setItemMeta(confirmMeta);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("Cancel Purchase");
        cancelItem.setItemMeta(cancelMeta);

        confirmGui.setItem(11, confirmItem, p -> purchaseItem(p, listing));
        confirmGui.setItem(15, cancelItem, p -> p.closeInventory());

        confirmGui.open(player);
    }

    private void purchaseItem(Player player, ItemListing listing) {
        Economy economy = MarketPlace.getInstance().getEconomy();
        double price = listing.getPrice();

        if (!economy.has(player, price)) {
            player.sendMessage("You don't have enough money to purchase this item.");
            return;
        }

        MarketPlace.getInstance().getDatabaseManager().removeItemListing(listing.getItemId(), success -> {
            if (success) {
                MarketPlace.getInstance().getItemMarketplaceManager().removeItemListing(listing.getItemId());
                economy.withdrawPlayer(player, price);
                OfflinePlayer seller = Bukkit.getOfflinePlayer(listing.getSellerUUID());
                economy.depositPlayer(seller, price);

                player.getInventory().addItem(listing.getItem());
                Transaction transaction = new Transaction(player.getUniqueId(), listing.getSellerUUID(), listing.getItem(), price, false);
                MarketPlace.getInstance().getDatabaseManager().saveTransaction(transaction, transactionSuccess -> {
                    if (transactionSuccess) {
                        player.sendMessage("You have successfully purchased the item for " + price);
                        MarketPlace.getInstance().getDiscordWebhook().sendMessage("Player " + player.getName() + " purchased " + listing.getItem().getType() + " for " + price);
                        
                        Player sellerPlayer = seller.getPlayer();
                        if (sellerPlayer != null && sellerPlayer.isOnline()) {
                            sellerPlayer.sendMessage("Your item " + listing.getItem().getType() + " has been sold for " + price);
                        }
                    } else {
                        player.sendMessage("Purchase successful, but failed to save transaction history.");
                    }
                });
            } else {
                player.sendMessage("Failed to purchase the item. It may have been already sold.");
            }
            openMarketplace(player);
        });
    }
}