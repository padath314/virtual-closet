package com.virtualcloset.app.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Outfit {
    private String id;
    private String name;
    private List<String> itemIds = new ArrayList<>();
    private boolean favorite;
    private Instant createdAt = Instant.now();

    public Outfit() {
    }

    public Outfit(String id, String name, List<String> itemIds, boolean favorite, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.itemIds = itemIds == null ? new ArrayList<>() : new ArrayList<>(itemIds);
        this.favorite = favorite;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds == null ? new ArrayList<>() : new ArrayList<>(itemIds);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
