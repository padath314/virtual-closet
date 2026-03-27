package com.virtualcloset.app.service;

import com.virtualcloset.app.model.CategoryType;
import com.virtualcloset.app.model.ClothingItem;
import com.virtualcloset.app.model.ClosetData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClosetRepository {
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ClosetRepository() {
        // Initialize external data folder on startup
        try {
            DataFolder.initializeIfNeeded();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize data folder", e);
        }
    }

    public ClosetData loadClosetData() {
        Map<String, ItemMetadata> metadataById = loadMetadataById();
        List<ClothingItem> items = new ArrayList<>();
        try {
            Path wardrobePath = DataFolder.getWardrobePath();
            if (Files.exists(wardrobePath)) {
                Files.list(wardrobePath)
                        .filter(Files::isDirectory)
                        .sorted()
                        .forEach(categoryPath -> loadCategoryItems(categoryPath, metadataById, items));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load closet data", exception);
        }
        items.sort(Comparator.comparingInt(ClothingItem::getLayerOrder).thenComparing(ClothingItem::getName, String.CASE_INSENSITIVE_ORDER));
        String basePath = DataFolder.getBasePath().toString();
        return new ClosetData(basePath, items);
    }

    private void loadCategoryItems(Path categoryPath,
                                   Map<String, ItemMetadata> metadataById,
                                   List<ClothingItem> items) {
        String categoryFolder = categoryPath.getFileName().toString();
        CategoryType categoryType;
        try {
            categoryType = CategoryType.fromFolderName(categoryFolder);
        } catch (IllegalArgumentException e) {
            // Skip unknown category folders
            return;
        }
        try {
            Files.list(categoryPath)
                    .filter(this::isPng)
                    .sorted()
                    .forEach(itemPath -> {
                        String fileName = itemPath.getFileName().toString();
                        String id = categoryFolder + "/" + fileName.substring(0, fileName.length() - 4);
                        ItemMetadata metadata = metadataById.getOrDefault(id, ItemMetadata.defaultFor(id, categoryType, fileName));
                        String imagePath = itemPath.toAbsolutePath().toString();
                        items.add(new ClothingItem(
                                id,
                                metadata.name(),
                                categoryType,
                                imagePath,
                                imagePath,
                                metadata.colors(),
                                metadata.tags(),
                                metadata.layerOrder() == null ? categoryType.getLayerOrder() : metadata.layerOrder()
                        ));
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load items from category " + categoryFolder, exception);
        }
    }

    private boolean isPng(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".png");
    }

    /**
     * Import a new clothing item from an external PNG file.
     */
    public ClothingItem importItem(Path sourceFile, CategoryType category, String name) throws IOException {
        String categoryFolder = category.name().toLowerCase();
        Path targetDir = DataFolder.getCategoryPath(categoryFolder);
        Files.createDirectories(targetDir);

        // Generate a safe filename
        String safeName = name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (safeName.isEmpty()) safeName = "item";
        String fileName = safeName + ".png";
        Path targetPath = targetDir.resolve(fileName);

        // Avoid overwriting - add number suffix if needed
        int counter = 1;
        while (Files.exists(targetPath)) {
            fileName = safeName + "-" + counter + ".png";
            targetPath = targetDir.resolve(fileName);
            counter++;
        }

        // Copy the file
        Files.copy(sourceFile, targetPath, StandardCopyOption.COPY_ATTRIBUTES);

        // Create the item
        String id = categoryFolder + "/" + fileName.substring(0, fileName.length() - 4);
        ClothingItem item = new ClothingItem(
                id,
                name,
                category,
                targetPath.toAbsolutePath().toString(),
                targetPath.toAbsolutePath().toString(),
                new ArrayList<>(),
                new ArrayList<>(),
                category.getLayerOrder()
        );

        // Save to metadata
        addItemMetadata(item);

        return item;
    }

    /**
     * Rename an existing item.
     */
    public void renameItem(ClothingItem item, String newName) {
        Map<String, ItemMetadata> metadataById = loadMetadataById();
        ItemMetadata existing = metadataById.get(item.getId());
        ItemMetadata updated = new ItemMetadata(
                item.getId(),
                newName,
                item.getCategory().name(),
                existing != null ? existing.colors() : item.getColors(),
                existing != null ? existing.tags() : item.getTags(),
                item.getLayerOrder()
        );
        metadataById.put(item.getId(), updated);
        saveMetadata(metadataById);
    }

    /**
     * Update item tags and colors.
     */
    public void updateItemMetadata(ClothingItem item, List<String> colors, List<String> tags) {
        Map<String, ItemMetadata> metadataById = loadMetadataById();
        ItemMetadata updated = new ItemMetadata(
                item.getId(),
                item.getName(),
                item.getCategory().name(),
                colors,
                tags,
                item.getLayerOrder()
        );
        metadataById.put(item.getId(), updated);
        saveMetadata(metadataById);
    }

    /**
     * Delete an item (removes PNG file and metadata).
     */
    public void deleteItem(ClothingItem item) throws IOException {
        // Delete the PNG file
        Path pngPath = Path.of(item.getImagePath());
        Files.deleteIfExists(pngPath);

        // Remove from metadata
        Map<String, ItemMetadata> metadataById = loadMetadataById();
        metadataById.remove(item.getId());
        saveMetadata(metadataById);
    }

    private void addItemMetadata(ClothingItem item) {
        Map<String, ItemMetadata> metadataById = loadMetadataById();
        ItemMetadata metadata = new ItemMetadata(
                item.getId(),
                item.getName(),
                item.getCategory().name(),
                item.getColors(),
                item.getTags(),
                item.getLayerOrder()
        );
        metadataById.put(item.getId(), metadata);
        saveMetadata(metadataById);
    }

    private void saveMetadata(Map<String, ItemMetadata> metadataById) {
        try {
            Path metadataPath = DataFolder.getMetadataPath();
            Files.createDirectories(metadataPath.getParent());
            List<ItemMetadata> metadataList = new ArrayList<>(metadataById.values());
            objectMapper.writeValue(metadataPath.toFile(), metadataList);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save metadata", e);
        }
    }

    private Map<String, ItemMetadata> loadMetadataById() {
        try {
            Path metadataPath = DataFolder.getMetadataPath();
            if (!Files.exists(metadataPath)) {
                return new HashMap<>();
            }
            List<ItemMetadata> metadata = objectMapper.readValue(metadataPath.toFile(), new TypeReference<>() { });
            return metadata.stream().collect(Collectors.toMap(ItemMetadata::id, value -> value, (left, right) -> right));
        } catch (IOException exception) {
            return new HashMap<>();
        }
    }

    public record ItemMetadata(String id, String name, String category, List<String> colors, List<String> tags, Integer layerOrder) {
        public static ItemMetadata defaultFor(String id, CategoryType categoryType, String fileName) {
            String normalizedName = Optional.ofNullable(fileName)
                    .map(name -> name.substring(0, name.length() - 4))
                    .map(name -> name.replace('-', ' ').replace('_', ' '))
                    .map(name -> Arrays.stream(name.split(" "))
                            .filter(part -> !part.isBlank())
                            .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                            .collect(Collectors.joining(" ")))
                    .orElse(id);
            return new ItemMetadata(id, normalizedName, categoryType.name(), new ArrayList<>(), new ArrayList<>(), categoryType.getLayerOrder());
        }
    }
}
