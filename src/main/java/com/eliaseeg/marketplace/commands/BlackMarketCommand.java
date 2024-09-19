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

        openBlackMarket(player);

        return true;
    }

    private void openBlackMarket(Player player) {
        List<ItemListing> blackMarketListings = MarketPlace.getInstance().getItemMarketplaceManager().getBlackMarketListings(5);
        InventoryGUI gui = new InventoryGUI("Black Market", 3);
        InventoryGUIListener.registerGUI("Black Market", gui);

        for (ItemListing original : blackMarketListings) {
            ItemListing discounted = new ItemListing(original.getItem(), original.getPrice() * 0.5, original.getSellerUUID());
            ItemStack displayItem = discounted.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
                lore.add("Discounted Price: " + discounted.getPrice());
                lore.add("Original Price: " + original.getPrice());
                lore.add("Click to purchase");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.addItem(displayItem, p -> openConfirmationGUI(p, discounted));
        }

        gui.open(player);
    }

    private void openConfirmationGUI(Player player, ItemListing listing) {
        Economy economy = MarketPlace.getInstance().getEconomy();
        double discountedPrice = listing.getPrice();

        if (!economy.has(player, discountedPrice)) {
            player.sendMessage("You don't have enough money to purchase this item.");
            return;
        }

        InventoryGUI confirmGui = new InventoryGUI("Confirm Black Market Purchase", 3);
        InventoryGUIListener.registerGUI("Confirm Black Market Purchase", confirmGui);

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
        double discountedPrice = listing.getPrice();
        double originalPrice = discountedPrice * 2;

        if (!economy.has(player, discountedPrice)) {
            player.sendMessage("You don't have enough money to purchase this item.");
            return;
        }

        MarketPlace.getInstance().getDatabaseManager().removeItemListing(listing.getItemId(), success -> {
            if (success) {
                MarketPlace.getInstance().getItemMarketplaceManager().removeItemListing(listing.getItemId());
                economy.withdrawPlayer(player, discountedPrice);
                OfflinePlayer seller = Bukkit.getOfflinePlayer(listing.getSellerUUID());
                economy.depositPlayer(seller, originalPrice); // 2x reimbursement

                player.getInventory().addItem(listing.getItem());
                Transaction transaction = new Transaction(player.getUniqueId(), listing.getSellerUUID(), listing.getItem(), discountedPrice, true);
                MarketPlace.getInstance().getDatabaseManager().saveTransaction(transaction, transactionSuccess -> {
                    if (transactionSuccess) {
                        player.sendMessage("You have successfully purchased the item for " + discountedPrice);
                        MarketPlace.getInstance().getDiscordWebhook().sendMessage("Player " + player.getName() + " purchased " + listing.getItem().getType() + " from the Black Market for " + discountedPrice);

                        Player sellerPlayer = seller.getPlayer();
                        if (sellerPlayer != null && sellerPlayer.isOnline()) {
                            sellerPlayer.sendMessage("Your item " + listing.getItem().getType() + " has been sold on the Black Market. You've been reimbursed " + originalPrice);
                        }
                    } else {
                        player.sendMessage("Purchase successful, but failed to save transaction history.");
                    }
                });
            } else {
                player.sendMessage("Failed to purchase the item. It may have been already sold.");
            }
            openBlackMarket(player);
        });
    }
}