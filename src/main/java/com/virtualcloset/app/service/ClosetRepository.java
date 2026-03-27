package com.virtualcloset.app.service;

import com.virtualcloset.app.model.CategoryType;
import com.virtualcloset.app.model.ClothingItem;
import com.virtualcloset.app.model.ClosetData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final String ROOT = "/closet-data";
    private static final String BASE = ROOT + "/base/base.png";
    private static final String WARDROBE_ROOT = ROOT + "/wardrobe";
    private static final String METADATA_FILE = ROOT + "/metadata/items.json";

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ClosetData loadClosetData() {
        Map<String, ItemMetadata> metadataById = loadMetadataById();
        List<ClothingItem> items = new ArrayList<>();
        try {
            Path wardrobePath = ResourceUtils.getResourcePath(WARDROBE_ROOT);
            Files.list(wardrobePath)
                    .filter(Files::isDirectory)
                    .sorted()
                    .forEach(categoryPath -> loadCategoryItems(categoryPath, metadataById, items));
        } catch (IOException | URISyntaxException exception) {
            throw new IllegalStateException("Unable to load closet data", exception);
        }
        items.sort(Comparator.comparingInt(ClothingItem::getLayerOrder).thenComparing(ClothingItem::getName, String.CASE_INSENSITIVE_ORDER));
        return new ClosetData(BASE, items);
    }

    private void loadCategoryItems(Path categoryPath,
                                   Map<String, ItemMetadata> metadataById,
                                   List<ClothingItem> items) {
        String categoryFolder = categoryPath.getFileName().toString();
        CategoryType categoryType = CategoryType.fromFolderName(categoryFolder);
        try {
            Files.list(categoryPath)
                    .filter(ResourceUtils::isPng)
                    .sorted()
                    .forEach(itemPath -> {
                        String fileName = itemPath.getFileName().toString();
                        String id = categoryFolder + "/" + fileName.substring(0, fileName.length() - 4);
                        ItemMetadata metadata = metadataById.getOrDefault(id, ItemMetadata.defaultFor(id, categoryType, fileName));
                        String imagePath = "/closet-data/wardrobe/" + categoryFolder + "/" + fileName;
                        String thumbnailPath = imagePath;
                        items.add(new ClothingItem(
                                id,
                                metadata.name(),
                                categoryType,
                                imagePath,
                                thumbnailPath,
                                metadata.colors(),
                                metadata.tags(),
                                metadata.layerOrder() == null ? categoryType.getLayerOrder() : metadata.layerOrder()
                        ));
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load items from category " + categoryFolder, exception);
        }
    }

    public void saveMetadataTemplate(Path outputFile) {
        ClosetData closetData = loadClosetData();
        List<ItemMetadata> metadata = closetData.getItems().stream()
                .map(item -> new ItemMetadata(item.getId(), item.getName(), item.getCategory().name(), item.getColors(), item.getTags(), item.getLayerOrder()))
                .collect(Collectors.toList());
        try {
            Files.createDirectories(outputFile.getParent());
            objectMapper.writeValue(outputFile.toFile(), metadata);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save metadata template", exception);
        }
    }

    private Map<String, ItemMetadata> loadMetadataById() {
        try (InputStream inputStream = ClosetRepository.class.getResourceAsStream(METADATA_FILE)) {
            if (inputStream == null) {
                return new HashMap<>();
            }
            List<ItemMetadata> metadata = objectMapper.readValue(inputStream, new TypeReference<>() { });
            return metadata.stream().collect(Collectors.toMap(ItemMetadata::id, value -> value, (left, right) -> right));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read metadata file", exception);
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
