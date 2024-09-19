package com.eliaseeg.marketplace;

import com.eliaseeg.marketplace.database.DatabaseManager;
import com.eliaseeg.marketplace.utils.inventorygui.InventoryGUIListener;
import org.bukkit.plugin.java.JavaPlugin;
import com.eliaseeg.marketplace.commands.SellCommand;
import com.eliaseeg.marketplace.commands.MarketplaceCommand;
import com.eliaseeg.marketplace.commands.BlackMarketCommand;

public final class MarketPlace extends JavaPlugin {

    private static MarketPlace instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        databaseManager = new DatabaseManager();
        databaseManager.connect();
        databaseManager.createDatabase();

        this.getCommand("sell").setExecutor(new SellCommand());
        this.getCommand("marketplace").setExecutor(new MarketplaceCommand());
        this.getCommand("blackmarket").setExecutor(new BlackMarketCommand());

        getServer().getPluginManager().registerEvents(new InventoryGUIListener(), this);
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
}
