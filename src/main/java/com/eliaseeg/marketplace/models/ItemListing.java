package com.eliaseeg.marketplace.models;

import com.eliaseeg.marketplace.utils.ItemStackSerializer;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import java.util.Date;
import java.util.UUID;

public class ItemListing {

    private UUID itemId;
    private ItemStack item;
    private double price;
    private UUID sellerUUID;
    private Date listingTime;

    public ItemListing(ItemStack item, double price, UUID sellerUUID) {
        this.itemId = UUID.randomUUID();
        this.item = item;
        this.price = price;
        this.sellerUUID = sellerUUID;
        this.listingTime = new Date();
    }

    public UUID getItemId() {
        return itemId;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public Date getListingTime() {
        return listingTime;
    }

    public Document toDocument() {
        return new Document("itemId", itemId.toString())
                .append("item", ItemStackSerializer.serialize(item))
                .append("price", price)
                .append("sellerUUID", sellerUUID.toString())
                .append("listingTime", listingTime);
    }

    public static ItemListing fromDocument(Document doc) {
        ItemListing listing = new ItemListing(
            ItemStackSerializer.deserialize(doc.get("item", Document.class)),
            doc.getDouble("price"),
            UUID.fromString(doc.getString("sellerUUID"))
        );
        listing.itemId = UUID.fromString(doc.getString("itemId"));
        listing.listingTime = doc.getDate("listingTime");
        return listing;
    }
}