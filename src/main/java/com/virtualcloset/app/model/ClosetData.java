package com.virtualcloset.app.model;

import java.util.ArrayList;
import java.util.List;

public class ClosetData {
    private String baseImagePath;
    private List<ClothingItem> items = new ArrayList<>();

    public ClosetData() {
    }

    public ClosetData(String baseImagePath, List<ClothingItem> items) {
        this.baseImagePath = baseImagePath;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    public String getBaseImagePath() {
        return baseImagePath;
    }

    public void setBaseImagePath(String baseImagePath) {
        this.baseImagePath = baseImagePath;
    }

    public List<ClothingItem> getItems() {
        return items;
    }

    public void setItems(List<ClothingItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }
}
