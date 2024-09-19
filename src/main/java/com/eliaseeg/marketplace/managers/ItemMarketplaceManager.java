package com.eliaseeg.marketplace.managers;

import com.eliaseeg.marketplace.MarketPlace;
import com.eliaseeg.marketplace.models.ItemListing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemMarketplaceManager {

    private final List<ItemListing> itemListings;

    public ItemMarketplaceManager() {
        this.itemListings = new CopyOnWriteArrayList<>();
    }

    public void loadItemListings() {
        MarketPlace.getInstance().getDatabaseManager().getAllItemListings(listings -> {
            itemListings.clear();
            itemListings.addAll(listings);
            MarketPlace.getInstance().getLogger().info("Loaded " + itemListings.size() + " item listings.");
        });
    }

    public void addItemListing(ItemListing listing) {
        itemListings.add(listing);
    }

    public void removeItemListing(UUID itemId) {
        itemListings.removeIf(listing -> listing.getItemId().equals(itemId));
    }

    public List<ItemListing> getAllListings() {
        return new ArrayList<>(itemListings);
    }

    public List<ItemListing> getBlackMarketListings(int count) {
        List<ItemListing> shuffledListings = new ArrayList<>(itemListings);
        Collections.shuffle(shuffledListings);
        return shuffledListings.subList(0, Math.min(count, shuffledListings.size()));
    }
}