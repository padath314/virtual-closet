package com.virtualcloset.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Manages the external data folder at ~/.virtual-closet/closet-data/
 * This allows users to add/modify clothing items after the app is packaged.
 */
public final class DataFolder {
    private static final Path DATA_ROOT = Path.of(System.getProperty("user.home"), ".virtual-closet", "closet-data");
    private static final String BUNDLED_ROOT = "/closet-data";

    private DataFolder() {
    }

    public static Path getDataRoot() {
        return DATA_ROOT;
    }

    public static Path getBasePath() {
        return DATA_ROOT.resolve("base/base.png");
    }

    public static Path getWardrobePath() {
        return DATA_ROOT.resolve("wardrobe");
    }

    public static Path getMetadataPath() {
        return DATA_ROOT.resolve("metadata/items.json");
    }

    public static Path getCategoryPath(String categoryFolder) {
        return getWardrobePath().resolve(categoryFolder);
    }

    /**
     * Initialize the external data folder by copying bundled resources if needed.
     * Only copies if the external folder doesn't exist yet.
     */
    public static void initializeIfNeeded() throws IOException {
        if (Files.exists(DATA_ROOT)) {
            // Already initialized, ensure category folders exist
            ensureCategoryFolders();
            return;
        }

        // Create the data folder structure
        Files.createDirectories(DATA_ROOT.resolve("base"));
        Files.createDirectories(DATA_ROOT.resolve("metadata"));
        ensureCategoryFolders();

        // Copy base.png if bundled
        copyBundledResource("/closet-data/base/base.png", DATA_ROOT.resolve("base/base.png"));

        // Copy metadata if bundled
        copyBundledResource("/closet-data/metadata/items.json", DATA_ROOT.resolve("metadata/items.json"));

        // Copy wardrobe items from bundled resources
        copyBundledWardrobe();
    }

    private static void ensureCategoryFolders() throws IOException {
        String[] categories = {"tops", "bottoms", "skirts", "dresses", "jackets", "tights", "socks", "shoes", "accessories"};
        for (String category : categories) {
            Files.createDirectories(getWardrobePath().resolve(category));
        }
    }

    private static void copyBundledResource(String resourcePath, Path targetPath) throws IOException {
        try (InputStream is = DataFolder.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                Files.createDirectories(targetPath.getParent());
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void copyBundledWardrobe() {
        // This copies bundled wardrobe items on first run
        // For a packaged app, you might pre-populate or leave empty
        String[] categories = {"tops", "bottoms", "skirts", "dresses", "jackets", "tights", "socks", "shoes", "accessories"};
        for (String category : categories) {
            try {
                Path bundledPath = ResourceUtils.getResourcePath("/closet-data/wardrobe/" + category);
                if (Files.exists(bundledPath)) {
                    Path targetDir = getWardrobePath().resolve(category);
                    Files.list(bundledPath)
                            .filter(ResourceUtils::isPng)
                            .forEach(src -> {
                                try {
                                    Path target = targetDir.resolve(src.getFileName().toString());
                                    if (!Files.exists(target)) {
                                        Files.copy(src, target);
                                    }
                                } catch (IOException e) {
                                    // Skip this file
                                }
                            });
                }
            } catch (Exception e) {
                // Category doesn't exist in bundled resources, skip
            }
        }
    }

    /**
     * Get the file path for an item image (external folder version).
     */
    public static String toExternalPath(String resourcePath) {
        if (resourcePath.startsWith("/closet-data/")) {
            return DATA_ROOT.resolve(resourcePath.substring("/closet-data/".length())).toString();
        }
        return resourcePath;
    }
}
