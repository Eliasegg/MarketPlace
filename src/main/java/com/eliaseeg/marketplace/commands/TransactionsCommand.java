package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.Transaction;
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

public class TransactionsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.history")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        List<Transaction> transactions = MarketPlace.getInstance().getDatabaseManager().getPlayerTransactions(player.getUniqueId());
        displayTransactions(player, transactions);

        return true;
    }

    private void displayTransactions(Player player, List<Transaction> transactions) {
        InventoryGUI gui = new InventoryGUI("Transaction History", 6, false);
        InventoryGUIListener.registerGUI("Transaction History", gui);

        if (transactions.isEmpty()) {
            player.sendMessage("You have no transactions in your history.");
            return;
        }

        for (Transaction transaction : transactions) {
            ItemStack displayItem = transaction.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
                lore.add("");
                lore.add("----------------");
                lore.add("Price: " + transaction.getPrice());
                lore.add("Type: " + (transaction.isBuyerTransaction(player.getUniqueId()) ? "Bought" : "Sold"));
                lore.add("Date: " + transaction.getTransactionTime());
                if (transaction.isBlackMarket()) {
                    lore.add("Black Market Transaction");
                }
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.addItem(displayItem, null);
        }

        player.openInventory(gui.getInventory());
    }
}