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
import java.util.List;

public class BlackMarketCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.blackmarket")) {
            MessageUtils.sendMessage(player, "no_permission");
            return true;
        }

        openBlackMarket(player);

        return true;
    }

    private void openBlackMarket(Player player) {
        List<ItemListing> blackMarketListings = MarketPlace.getInstance().getItemMarketplaceManager().getBlackMarketListings(5);
        InventoryGUI gui = new InventoryGUI("Black Market", 3, false);
        InventoryGUIListener.registerGUI("Black Market", gui);

        for (ItemListing original : blackMarketListings) {
            ItemListing discounted = new ItemListing(original.getItem(), original.getPrice() * 0.5, original.getSellerUUID());
            ItemStack displayItem = discounted.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = MessageUtils.getMessageList("item_lore.black_market",
                    "discounted_price", discounted.getPrice(),
                    "original_price", original.getPrice());
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.addItem(displayItem, p -> openConfirmationGUI(p, discounted));
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
        double discountedPrice = listing.getPrice();

        if (!economy.has(player, discountedPrice)) {
            MessageUtils.sendMessage(player, "not_enough_money");
            player.closeInventory();
            return;
        }

        InventoryGUI confirmGui = new InventoryGUI("Confirm Black Market Purchase", 3, true);
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

        player.openInventory(confirmGui.getInventory());
    }

    private void purchaseItem(Player player, ItemListing listing) {
        if (!CooldownManager.checkCooldown(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "purchase_cooldown");
            return;
        }

        Economy economy = MarketPlace.getInstance().getEconomy();
        double discountedPrice = listing.getPrice();
        double originalPrice = discountedPrice * 2;

        if (!economy.has(player, discountedPrice)) {
            MessageUtils.sendMessage(player, "not_enough_money");
            player.closeInventory();
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
                        String itemName = listing.getItem().hasItemMeta() ? listing.getItem().getItemMeta().getDisplayName() : listing.getItem().getType().name();
                        MessageUtils.sendMessage(player, "purchase_success", "price", discountedPrice);
                        MarketPlace.getInstance().getDiscordWebhook().sendMessage(MessageUtils.getMessage("discord_messages.black_market_purchase", "player", player.getName(), "item", itemName, "price", discountedPrice));

                        Player sellerPlayer = seller.getPlayer();
                        if (sellerPlayer != null && sellerPlayer.isOnline()) {
                            MessageUtils.sendMessage(sellerPlayer, "black_market_item_sold", "item", itemName, "price", originalPrice);
                        }
                    } else {
                        MessageUtils.sendMessage(player, "transaction_save_failed");
                    }
                });
            } else {
                MessageUtils.sendMessage(player, "purchase_failed");
            }
            openBlackMarket(player);
        });
    }
}