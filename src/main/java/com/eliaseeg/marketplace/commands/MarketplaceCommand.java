package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;
import com.eliaseeg.marketplace.models.Transaction;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUI;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUIListener;
import com.eliaseeg.marketplace.managers.CooldownManager;
import com.eliaseeg.marketplace.utils.MessageUtils;
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
import java.util.HashMap;
import java.util.List;

public class MarketplaceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.view")) {
            MessageUtils.sendMessage(player, "no_permission");
            return true;
        }

        openMarketplace(player);

        return true;
    }

    private void openMarketplace(Player player) {
        List<ItemListing> listings = MarketPlace.getInstance().getItemMarketplaceManager().getAllListings();
        InventoryGUI gui = new InventoryGUI(MessageUtils.getMessage("marketplace_title"), 6, false);
        InventoryGUIListener.registerGUI(MessageUtils.getMessage("marketplace_title"), gui);

        for (ItemListing listing : listings) {
            ItemStack displayItem = listing.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = MessageUtils.getMessageList("item_lore.marketplace",
                    "price", listing.getPrice(),
                    "seller", Bukkit.getOfflinePlayer(listing.getSellerUUID()).getName());
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.addItem(displayItem, p -> openConfirmationGUI(p, listing));
        }

        player.openInventory(gui.getInventory());
    }

    private void openConfirmationGUI(Player player, ItemListing listing) {
        if (player.getUniqueId().equals(listing.getSellerUUID())) {
            MessageUtils.sendMessage(player, "cannot_buy_own_item");
            player.closeInventory();
            return;
        }

        Economy economy = MarketPlace.getInstance().getEconomy();
        double price = listing.getPrice();

        if (!economy.has(player, price)) {
            MessageUtils.sendMessage(player, "not_enough_money");
            player.closeInventory();
            return;
        }

        InventoryGUI confirmGui = new InventoryGUI(MessageUtils.getMessage("confirm_purchase_title"), 3, true);
        InventoryGUIListener.registerGUI(MessageUtils.getMessage("confirm_purchase_title"), confirmGui);

        ItemStack confirmItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.setDisplayName(MessageUtils.getMessage("confirm_purchase"));
        confirmItem.setItemMeta(confirmMeta);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(MessageUtils.getMessage("cancel_purchase"));
        cancelItem.setItemMeta(cancelMeta);

        confirmGui.setItem(11, confirmItem, p -> purchaseItem(p, listing));
        confirmGui.setItem(15, cancelItem, p -> p.closeInventory());

        player.openInventory(confirmGui.getInventory());
    }

    private void purchaseItem(Player player, ItemListing listing) {
        if (!CooldownManager.checkCooldown(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "purchase_cooldown");
            return;
        }

        Economy economy = MarketPlace.getInstance().getEconomy();
        double price = listing.getPrice();

        if (!economy.has(player, price)) {
            MessageUtils.sendMessage(player, "not_enough_money");
            player.closeInventory();
            return;
        }

        MarketPlace.getInstance().getDatabaseManager().removeItemListing(listing.getItemId(), success -> {
            if (success) {
                MarketPlace.getInstance().getItemMarketplaceManager().removeItemListing(listing.getItemId());
                economy.withdrawPlayer(player, price);
                OfflinePlayer seller = Bukkit.getOfflinePlayer(listing.getSellerUUID());
                economy.depositPlayer(seller, price);

                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(listing.getItem());
                if (!leftover.isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
                    MessageUtils.sendMessage(player, "inventory_full_item_dropped");
                }

                Transaction transaction = new Transaction(player.getUniqueId(), listing.getSellerUUID(), listing.getItem(), price, false);
                MarketPlace.getInstance().getDatabaseManager().saveTransaction(transaction, transactionSuccess -> {
                    if (transactionSuccess) {
                        String itemName = listing.getItem().hasItemMeta() ? listing.getItem().getItemMeta().getDisplayName() : listing.getItem().getType().name();
                        MessageUtils.sendMessage(player, "purchase_success", "price", price);
                        MarketPlace.getInstance().getDiscordWebhook().sendMessage(MessageUtils.getMessage("discord_messages.purchase", "player", player.getName(), "item", itemName, "price", price));
                        
                        Player sellerPlayer = seller.getPlayer();
                        if (sellerPlayer != null && sellerPlayer.isOnline()) {
                            MessageUtils.sendMessage(sellerPlayer, "item_sold", "item", itemName, "price", price);
                        }
                    } else {
                        MessageUtils.sendMessage(player, "transaction_save_failed");
                    }
                });
            } else {
                MessageUtils.sendMessage(player, "purchase_failed");
            }
            openMarketplace(player);
        });
    }
}