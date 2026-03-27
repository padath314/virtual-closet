package com.virtualcloset.app.model;

public enum CategoryType {
    TOPS(false, 10, "Tops"),
    BOTTOMS(false, 20, "Bottoms"),
    SKIRTS(false, 25, "Skirts"),
    DRESSES(false, 30, "Dresses"),
    JACKETS(false, 35, "Jackets"),
    TIGHTS(false, 36, "Tights"),
    SOCKS(false, 38, "Socks"),
    SHOES(false, 40, "Shoes"),
    ACCESSORIES(true, 50, "Accessories");

    private final boolean multiSelect;
    private final int layerOrder;
    private final String displayName;

    CategoryType(boolean multiSelect, int layerOrder, String displayName) {
        this.multiSelect = multiSelect;
        this.layerOrder = layerOrder;
        this.displayName = displayName;
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

    public static CategoryType fromFolderName(String folderName) {
        return switch (folderName.toLowerCase()) {
            case "tops" -> TOPS;
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
