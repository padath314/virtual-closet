package com.virtualcloset.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClothingItem {
    private String id;
    private String name;
    private CategoryType category;
    private String imagePath;
    private String thumbnailPath;
    private List<String> colors = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private int layerOrder;

    public ClothingItem() {
    }

    public ClothingItem(String id, String name, CategoryType category, String imagePath, String thumbnailPath,
                        List<String> colors, List<String> tags, int layerOrder) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
        this.thumbnailPath = thumbnailPath;
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.layerOrder = layerOrder;
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

    public CategoryType getCategory() {
        return category;
    }

    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public int getLayerOrder() {
        return layerOrder;
    }

    public void setLayerOrder(int layerOrder) {
        this.layerOrder = layerOrder;
    }

    public boolean matchesQuery(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.toLowerCase();
        return contains(name, normalized) ||
                colors.stream().filter(Objects::nonNull).map(String::toLowerCase).anyMatch(value -> value.contains(normalized)) ||
                tags.stream().filter(Objects::nonNull).map(String::toLowerCase).anyMatch(value -> value.contains(normalized)) ||
                category.getDisplayName().toLowerCase().contains(normalized);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }
}
