package com.virtualcloset.app.service;

import com.virtualcloset.app.model.CategoryType;
import com.virtualcloset.app.model.ClothingItem;
import com.virtualcloset.app.model.ClosetData;
import com.virtualcloset.app.model.Outfit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClosetService {
    private final ClosetData closetData;
    private final OutfitStorage outfitStorage;
    private final Map<CategoryType, LinkedHashSet<String>> selectedItemIds = new EnumMap<>(CategoryType.class);
    private final List<Outfit> savedOutfits;

    public ClosetService(ClosetRepository closetRepository, OutfitStorage outfitStorage) {
        this.closetData = closetRepository.loadClosetData();
        this.outfitStorage = outfitStorage;
        this.savedOutfits = new ArrayList<>(outfitStorage.loadOutfits());
        for (CategoryType categoryType : CategoryType.values()) {
            selectedItemIds.put(categoryType, new LinkedHashSet<>());
        }
    }

    public ClosetData getClosetData() {
        return closetData;
    }

    public List<ClothingItem> getItemsForCategory(CategoryType categoryType) {
        return closetData.getItems().stream()
                .filter(item -> item.getCategory() == categoryType)
                .sorted(Comparator.comparing(ClothingItem::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<ClothingItem> searchItems(String query) {
        return closetData.getItems().stream()
                .filter(item -> item.matchesQuery(query))
                .sorted(Comparator.comparing(ClothingItem::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void toggleSelection(ClothingItem item) {
        LinkedHashSet<String> categorySelections = selectedItemIds.get(item.getCategory());
        if (item.getCategory().isMultiSelect()) {
            if (!categorySelections.remove(item.getId())) {
                categorySelections.add(item.getId());
            }
            return;
        }
        if (categorySelections.contains(item.getId())) {
            categorySelections.clear();
        } else {
            categorySelections.clear();
            categorySelections.add(item.getId());
        }
    }

    public boolean isSelected(ClothingItem item) {
        return selectedItemIds.get(item.getCategory()).contains(item.getId());
    }

    public List<ClothingItem> getSelectedItems() {
        return closetData.getItems().stream()
                .filter(item -> selectedItemIds.get(item.getCategory()).contains(item.getId()))
                .sorted(Comparator.comparingInt(ClothingItem::getLayerOrder))
                .toList();
    }

    public void clearSelections() {
        selectedItemIds.values().forEach(Collection::clear);
    }

    public List<Outfit> getSavedOutfits() {
        return savedOutfits.stream()
                .sorted(Comparator.comparing(Outfit::isFavorite).reversed().thenComparing(Outfit::getCreatedAt).reversed())
                .toList();
    }

    public Outfit saveCurrentOutfit(String name, boolean favorite) {
        Outfit outfit = new Outfit(
                UUID.randomUUID().toString(),
                name,
                getSelectedItems().stream().map(ClothingItem::getId).toList(),
                favorite,
                Instant.now()
        );
        savedOutfits.add(outfit);
        persistOutfits();
        return outfit;
    }

    public void loadOutfit(Outfit outfit) {
        clearSelections();
        for (String itemId : outfit.getItemIds()) {
            closetData.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> selectedItemIds.get(item.getCategory()).add(item.getId()));
        }
    }

    public void toggleFavorite(Outfit outfit) {
        outfit.setFavorite(!outfit.isFavorite());
        persistOutfits();
    }

    public void deleteOutfit(Outfit outfit) {
        savedOutfits.removeIf(existing -> existing.getId().equals(outfit.getId()));
        persistOutfits();
    }

    public Map<CategoryType, List<ClothingItem>> selectedItemsByCategory() {
        return getSelectedItems().stream().collect(Collectors.groupingBy(ClothingItem::getCategory, () -> new EnumMap<>(CategoryType.class), Collectors.toList()));
    }

    private void persistOutfits() {
        outfitStorage.saveOutfits(savedOutfits);
    }
}
