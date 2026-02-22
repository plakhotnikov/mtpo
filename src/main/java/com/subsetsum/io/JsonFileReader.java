package com.subsetsum.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsetsum.model.SubsetSumInput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class JsonFileReader {

    private final ObjectMapper objectMapper;

    public JsonFileReader() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonFileReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SubsetSumInput readFromFile(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read file: " + filePath);
        }
        return objectMapper.readValue(file, SubsetSumInput.class);
    }

    public SubsetSumInput readFromStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        return objectMapper.readValue(inputStream, SubsetSumInput.class);
    }
}
