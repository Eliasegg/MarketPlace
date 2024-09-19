package com.eliaseeg.marketplace.models;

import com.eliaseeg.marketplace.utils.ItemStackSerializer;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import java.util.Date;
import java.util.UUID;

public class Transaction {

    private UUID transactionId;
    private UUID buyerUUID;
    private UUID sellerUUID;
    private ItemStack item;
    private double price;
    private Date transactionTime;
    private boolean isBlackMarket;

    public Transaction(UUID buyerUUID, UUID sellerUUID, ItemStack item, double price, boolean isBlackMarket) {
        this.transactionId = UUID.randomUUID();
        this.buyerUUID = buyerUUID;
        this.sellerUUID = sellerUUID;
        this.item = item;
        this.price = price;
        this.transactionTime = new Date();
        this.isBlackMarket = isBlackMarket;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getBuyerUUID() {
        return buyerUUID;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public boolean isBlackMarket() {
        return isBlackMarket;
    }

    public boolean isBuyerTransaction(UUID playerUUID) {
        return buyerUUID.equals(playerUUID);
    }

    public Document toDocument() {
        return new Document("transactionId", transactionId.toString())
                .append("buyerUUID", buyerUUID.toString())
                .append("sellerUUID", sellerUUID.toString())
                .append("item", ItemStackSerializer.serialize(item))
                .append("price", price)
                .append("transactionTime", transactionTime)
                .append("isBlackMarket", isBlackMarket);
    }

    public static Transaction fromDocument(Document doc) {
        Transaction transaction = new Transaction(
            UUID.fromString(doc.getString("buyerUUID")),
            UUID.fromString(doc.getString("sellerUUID")),
            ItemStackSerializer.deserialize(doc.get("item", Document.class)),
            doc.getDouble("price"),
            doc.getBoolean("isBlackMarket")
        );
        transaction.transactionId = UUID.fromString(doc.getString("transactionId"));
        transaction.transactionTime = doc.getDate("transactionTime");
        return transaction;
    }

}