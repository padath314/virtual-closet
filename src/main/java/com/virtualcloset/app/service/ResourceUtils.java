package com.virtualcloset.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public final class ResourceUtils {
    private ResourceUtils() {
    }

    public static InputStream openResource(String resourcePath) {
        InputStream stream = ResourceUtils.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        return stream;
    }

    public static Path getResourcePath(String resourceRoot) throws IOException, URISyntaxException {
        URL url = ResourceUtils.class.getResource(resourceRoot);
        if (url == null) {
            throw new IllegalArgumentException("Resource root not found: " + resourceRoot);
        }
        if (url.getProtocol().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap());
            return fileSystem.getPath(resourceRoot);
        }
        return Path.of(url.toURI());
    }

    public static boolean resourceExists(String resourcePath) {
        return ResourceUtils.class.getResource(resourcePath) != null;
    }

    public static String normalizeResourcePath(Path path, Path root) {
        return "/" + root.relativize(path).toString().replace('\\', '/');
    }

    public static boolean isPng(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".png");
    }
}
