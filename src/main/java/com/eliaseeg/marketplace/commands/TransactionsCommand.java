package com.eliaseeg.marketplace.commands;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.Transaction;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUI;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUIListener;
import com.eliaseeg.marketplace.utils.MessageUtils;
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
            sender.sendMessage(MessageUtils.getMessage("player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("marketplace.history")) {
            MessageUtils.sendMessage(player, "no_permission");
            return true;
        }

        List<Transaction> transactions = MarketPlace.getInstance().getDatabaseManager().getPlayerTransactions(player.getUniqueId());
        displayTransactions(player, transactions);

        return true;
    }

    private void displayTransactions(Player player, List<Transaction> transactions) {
        InventoryGUI gui = new InventoryGUI(MessageUtils.getMessage("transaction_history_title"), 6, false);
        InventoryGUIListener.registerGUI(MessageUtils.getMessage("transaction_history_title"), gui);

        if (transactions.isEmpty()) {
            MessageUtils.sendMessage(player, "no_transactions");
            return;
        }

        for (Transaction transaction : transactions) {
            ItemStack displayItem = transaction.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = MessageUtils.getMessageList("item_lore.transaction",
                    "price", transaction.getPrice(),
                    "type", (transaction.isBuyerTransaction(player.getUniqueId()) ? "Bought" : "Sold"),
                    "date", transaction.getTransactionTime(),
                    "black_market", transaction.isBlackMarket() ? "Black Market Transaction" : "");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.addItem(displayItem, null);
        }

        player.openInventory(gui.getInventory());
    }
}