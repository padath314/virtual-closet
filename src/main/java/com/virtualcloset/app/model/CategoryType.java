package com.virtualcloset.app.model;

public enum CategoryType {
    // Layer order: base → tights → socks → shoes → tops1 → bottoms → dresses → skirts → tops2 → jackets → accessories
    TIGHTS(false, 10, "Tights", "tights"),
    SOCKS(false, 20, "Socks", "socks"),
    SHOES(false, 30, "Shoes", "shoes"),
    TOPS1(false, 40, "Top 1", "tops"),       // Lower layer top (under bottoms/dresses/skirts)
    BOTTOMS(false, 50, "Bottoms", "bottoms"),
    DRESSES(false, 60, "Dresses", "dresses"),
    SKIRTS(false, 70, "Skirts", "skirts"),
    TOPS2(false, 80, "Top 2", "tops"),       // Upper layer top (over dresses/skirts)
    JACKETS(false, 90, "Jackets", "jackets"),
    ACCESSORIES(true, 100, "Accessories", "accessories");

    private final boolean multiSelect;
    private final int layerOrder;
    private final String displayName;
    private final String folderName;

    CategoryType(boolean multiSelect, int layerOrder, String displayName, String folderName) {
        this.multiSelect = multiSelect;
        this.layerOrder = layerOrder;
        this.displayName = displayName;
        this.folderName = folderName;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public int getLayerOrder() {
        return layerOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFolderName() {
        return folderName;
    }

    public static CategoryType fromFolderName(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "tops" -> TOPS1;  // Default to TOPS1 when loading from folder
            case "bottoms" -> BOTTOMS;
            case "skirt", "skirts" -> SKIRTS;
            case "dresses" -> DRESSES;
            case "jacket", "jackets" -> JACKETS;
            case "tights" -> TIGHTS;
            case "socks" -> SOCKS;
            case "shoes" -> SHOES;
            case "accessories" -> ACCESSORIES;
            default -> throw new IllegalArgumentException("Unknown category folder: " + folderName);
        };
    }
}
