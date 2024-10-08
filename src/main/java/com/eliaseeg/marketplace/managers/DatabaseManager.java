package com.eliaseeg.marketplace.managers;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import org.bson.Document;
import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.PlayerData;
import com.eliaseeg.marketplace.models.ItemListing;
import com.eliaseeg.marketplace.models.Transaction;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DatabaseManager {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> playerDataCollection;
    private MongoCollection<Document> itemListingsCollection;

    public boolean connect() {
        try {
            String uri = MarketPlace.getInstance().getConfig().getString("database.uri", "mongodb://localhost:27017");
            String databaseName = MarketPlace.getInstance().getConfig().getString("database.name", "marketplace");

            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .serverApi(ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build())
                    .retryWrites(true)
                    .retryReads(true)
                    .build();
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            
            // Test the connection
            database.runCommand(new Document("ping", 1));
            
            return true;
        } catch (Exception e) {
            MarketPlace.getInstance().getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
            return false;
        }
    }

    public void createDatabase() {
        try {
            // Create collections if they don't exist
            database.createCollection("playerData");
            database.createCollection("itemListings");

            // Initialize collection references
            playerDataCollection = database.getCollection("playerData");
            itemListingsCollection = database.getCollection("itemListings");

            MarketPlace.getInstance().getLogger().info("Database and collections created successfully!");
        } catch (Exception e) {
            MarketPlace.getInstance().getLogger().severe("Failed to create database or collections: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            MarketPlace.getInstance().getLogger().info("Disconnected from MongoDB.");
        }
    }

    // PlayerData operations
    public PlayerData getPlayerData(UUID playerUUID) {
        Document doc = playerDataCollection.find(Filters.eq("playerUUID", playerUUID.toString())).first();
        return doc != null ? PlayerData.fromDocument(doc) : new PlayerData(playerUUID);
    }

    public void savePlayerData(PlayerData playerData) {
        playerDataCollection.replaceOne(
            Filters.eq("playerUUID", playerData.getPlayerUUID().toString()),
            playerData.toDocument(),
            new ReplaceOptions().upsert(true)
        );
    }

    // ItemListing operations
    public void saveItemListing(ItemListing itemListing, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketPlace.getInstance(), () -> {
            try {
                itemListingsCollection.replaceOne(
                    Filters.eq("itemId", itemListing.getItemId().toString()),
                    itemListing.toDocument(),
                    new ReplaceOptions().upsert(true)
                );
                Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(true));
            } catch (Exception e) {
                MarketPlace.getInstance().getLogger().severe("Failed to save item listing: " + e.getMessage());
                Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(false));
            }
        });
    }

    public void getItemListing(UUID itemId, Consumer<ItemListing> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketPlace.getInstance(), () -> {
            Document doc = itemListingsCollection.find(Filters.eq("itemId", itemId.toString())).first();
            ItemListing listing = doc != null ? ItemListing.fromDocument(doc) : null;
            Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(listing));
        });

    }

    public void getAllItemListings(Consumer<List<ItemListing>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketPlace.getInstance(), () -> {
            List<ItemListing> listings = new ArrayList<>();
            itemListingsCollection.find().forEach(doc -> listings.add(ItemListing.fromDocument(doc)));
            Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(listings));
        });
    }

    public void removeItemListing(UUID itemId, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketPlace.getInstance(), () -> {
            try {
                itemListingsCollection.deleteOne(Filters.eq("itemId", itemId.toString()));
                Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(true));
            } catch (Exception e) {
                MarketPlace.getInstance().getLogger().severe("Failed to remove item listing: " + e.getMessage());
                Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(false));
            }
        });
    }

    // Transaction operations
    public void saveTransaction(Transaction transaction, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketPlace.getInstance(), () -> {
            try {
                PlayerData buyerData = getPlayerData(transaction.getBuyerUUID());
                PlayerData sellerData = getPlayerData(transaction.getSellerUUID());

                buyerData.addTransaction(transaction);
                sellerData.addTransaction(transaction);

                savePlayerData(buyerData);
                savePlayerData(sellerData);

                Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(true));
            } catch (Exception e) {
                MarketPlace.getInstance().getLogger().severe("Failed to save transaction: " + e.getMessage());
                Bukkit.getScheduler().runTask(MarketPlace.getInstance(), () -> callback.accept(false));
            }
        });
    }

    public List<Transaction> getPlayerTransactions(UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);
        return playerData.getTransactions();
    }
}