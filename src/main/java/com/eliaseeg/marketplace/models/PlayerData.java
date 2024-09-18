package com.eliaseeg.marketplace.models;

import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerData {

    private UUID playerUUID;
    private List<Transaction> transactions;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.transactions = new ArrayList<>();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public Document toDocument() {
        return new Document("playerUUID", playerUUID.toString())
                .append("transactions", transactions.stream()
                        .map(Transaction::toDocument)
                        .collect(Collectors.toList()));
    }

    public static PlayerData fromDocument(Document doc) {
        PlayerData playerData = new PlayerData(UUID.fromString(doc.getString("playerUUID")));
        List<Document> transactionDocs = doc.getList("transactions", Document.class);
        playerData.transactions = transactionDocs.stream()
                .map(Transaction::fromDocument)
                .collect(Collectors.toList());
        return playerData;
    }
}