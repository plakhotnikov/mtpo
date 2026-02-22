package com.subsetsum.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.subsetsum.model.SubsetSumResult;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class JsonFileWriter {

    private final ObjectMapper objectMapper;

    public JsonFileWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public JsonFileWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeToFile(SubsetSumResult result, String filePath) throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        objectMapper.writeValue(file, result);
    }

    public void writeToStream(SubsetSumResult result, OutputStream outputStream) throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream must not be null");
        }
        objectMapper.writeValue(outputStream, result);
    }

    public String writeToString(SubsetSumResult result) throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("result must not be null");
        }
        return objectMapper.writeValueAsString(result);
    }
}
