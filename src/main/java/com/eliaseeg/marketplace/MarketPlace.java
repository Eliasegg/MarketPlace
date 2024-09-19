package com.eliaseeg.marketplace;

import com.eliaseeg.marketplace.managers.DatabaseManager;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUIListener;
import com.eliaseeg.marketplace.utils.DiscordWebhook;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import com.eliaseeg.marketplace.commands.*;
import com.eliaseeg.marketplace.managers.ItemMarketplaceManager;

public final class MarketPlace extends JavaPlugin {

    private static MarketPlace instance;
    private DatabaseManager databaseManager;
    private DiscordWebhook discordWebhook;
    private Economy economy;
    private ItemMarketplaceManager itemMarketplaceManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager();
        databaseManager.connect();
        databaseManager.createDatabase();

        String webhookUrl = getConfig().getString("discord.webhook_url");
        discordWebhook = new DiscordWebhook(webhookUrl);

        this.getCommand("sell").setExecutor(new SellCommand());
        this.getCommand("marketplace").setExecutor(new MarketplaceCommand());
        this.getCommand("blackmarket").setExecutor(new BlackMarketCommand());
        this.getCommand("transactions").setExecutor(new TransactionsCommand());

        getServer().getPluginManager().registerEvents(new InventoryGUIListener(), this);

        this.itemMarketplaceManager = new ItemMarketplaceManager();
        this.itemMarketplaceManager.loadItemListings();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    public static MarketPlace getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public DiscordWebhook getDiscordWebhook() {
        return discordWebhook;
    }

    public Economy getEconomy() {
        return economy;
    }

    public ItemMarketplaceManager getItemMarketplaceManager() {
        return itemMarketplaceManager;
    }
}
