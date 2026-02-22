package com.subsetsum.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsetsum.model.SubsetSumResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("JsonFileWriter Tests")
class JsonFileWriterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Запись результата в файл")
    void writeToFile_success() throws IOException {
        SubsetSumResult result = new SubsetSumResult(true, List.of(3, 8), "TestAlgo");
        result.setExecutionTimeNs(1_500_000);
        result.setMemoryUsedBytes(2048);

        Path outFile = tempDir.resolve("output.json");
        JsonFileWriter writer = new JsonFileWriter();
        writer.writeToFile(result, outFile.toString());

        assertTrue(Files.exists(outFile));
        String content = Files.readString(outFile);
        assertThat(content, containsString("\"found\""));
        assertThat(content, containsString("true"));
        assertThat(content, containsString("\"subset\""));
    }

    @Test
    @DisplayName("Запись в OutputStream")
    void writeToStream_success() throws IOException {
        SubsetSumResult result = new SubsetSumResult(false, List.of(), "TestAlgo");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JsonFileWriter writer = new JsonFileWriter();
        writer.writeToStream(result, baos);

        String json = baos.toString();
        assertThat(json, containsString("\"found\""));
        assertThat(json, containsString("false"));
    }

    @Test
    @DisplayName("Запись в строку")
    void writeToString_success() throws IOException {
        SubsetSumResult result = new SubsetSumResult(true, List.of(5, 10), "Algo");
        JsonFileWriter writer = new JsonFileWriter();
        String json = writer.writeToString(result);

        assertThat(json, containsString("\"found\" : true"));
        assertThat(json, containsString("\"algorithmName\" : \"Algo\""));
    }

    @Test
    @DisplayName("null result → IllegalArgumentException")
    void nullResult_throwsException() {
        JsonFileWriter writer = new JsonFileWriter();
        assertThrows(IllegalArgumentException.class, () -> writer.writeToFile(null, "out.json"));
        assertThrows(IllegalArgumentException.class, () -> writer.writeToStream(null, new ByteArrayOutputStream()));
        assertThrows(IllegalArgumentException.class, () -> writer.writeToString(null));
    }

    @Test
    @DisplayName("null/blank filePath → IllegalArgumentException")
    void nullOrBlankPath_throwsException() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(1), "A");
        JsonFileWriter writer = new JsonFileWriter();

        assertThrows(IllegalArgumentException.class, () -> writer.writeToFile(result, null));
        assertThrows(IllegalArgumentException.class, () -> writer.writeToFile(result, "  "));
    }

    @Test
    @DisplayName("null outputStream → IllegalArgumentException")
    void nullOutputStream_throwsException() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(1), "A");
        JsonFileWriter writer = new JsonFileWriter();
        assertThrows(IllegalArgumentException.class, () -> writer.writeToStream(result, null));
    }

    @Test
    @DisplayName("Запись в несуществующий подкаталог — создаёт каталог")
    void writeToNonExistentSubdir_createsDir() throws IOException {
        SubsetSumResult result = new SubsetSumResult(true, List.of(1, 2), "A");
        Path subDir = tempDir.resolve("sub/dir/output.json");

        JsonFileWriter writer = new JsonFileWriter();
        writer.writeToFile(result, subDir.toString());

        assertTrue(Files.exists(subDir));
    }

    @Test
    @DisplayName("Mock: verify ObjectMapper.writeValue вызывается")
    void mock_objectMapperWriteValueCalled() throws IOException {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        JsonFileWriter writer = new JsonFileWriter(mockMapper);

        SubsetSumResult result = new SubsetSumResult(true, List.of(1), "A");
        Path outFile = tempDir.resolve("mock_out.json");

        writer.writeToFile(result, outFile.toString());

        verify(mockMapper).writeValue(any(File.class), eq(result));
    }
}
