package com.virtualcloset.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.virtualcloset.app.model.Outfit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OutfitStorage {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path storageFile;

    public OutfitStorage() {
        this(Path.of(System.getProperty("user.home"), ".virtual-closet", "saved-outfits.json"));
    }

    public OutfitStorage(Path storageFile) {
        this.storageFile = storageFile;
    }

    public List<Outfit> loadOutfits() {
        if (!Files.exists(storageFile)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(storageFile.toFile(), new TypeReference<>() { });
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load saved outfits", exception);
        }
    }

    public void saveOutfits(List<Outfit> outfits) {
        try {
            Files.createDirectories(storageFile.getParent());
            objectMapper.writeValue(storageFile.toFile(), outfits);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save outfits", exception);
        }
    }
}
