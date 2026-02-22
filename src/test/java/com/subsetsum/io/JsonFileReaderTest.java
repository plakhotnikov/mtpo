package com.subsetsum.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subsetsum.model.SubsetSumInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Тесты для JsonFileReader.
 * Демонстрация мокирования: @Mock, @Spy, mock()
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonFileReader Tests")
class JsonFileReaderTest {

    @TempDir
    Path tempDir;

    // Мокирование тип 1: @Mock аннотация
    @Mock
    private ObjectMapper mockObjectMapper;

    // Мокирование тип 2: @Spy аннотация
    @Spy
    private ObjectMapper spyObjectMapper;

    @BeforeEach
    void setUp() {
    }

    // ==================== Тесты с реальным чтением ====================

    @Test
    @DisplayName("Чтение корректного JSON-файла")
    void readValidJsonFile() throws IOException {
        Path jsonFile = tempDir.resolve("input.json");
        Files.writeString(jsonFile, """
                {"numbers": [1, 2, 3, 4, 5], "targetSum": 9}
                """);

        JsonFileReader reader = new JsonFileReader();
        SubsetSumInput input = reader.readFromFile(jsonFile.toString());

        assertNotNull(input);
        assertThat(input.getNumbers(), hasSize(5));
        assertThat(input.getNumbers(), contains(1, 2, 3, 4, 5));
        assertEquals(9, input.getTargetSum());
    }

    @Test
    @DisplayName("Чтение из InputStream")
    void readFromStream() throws IOException {
        String json = """
                {"numbers": [10, 20, 30], "targetSum": 50}
                """;
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        JsonFileReader reader = new JsonFileReader();
        SubsetSumInput input = reader.readFromStream(stream);

        assertNotNull(input);
        assertThat(input.getNumbers(), contains(10, 20, 30));
        assertEquals(50, input.getTargetSum());
    }

    @Test
    @DisplayName("Файл не существует → IOException")
    void fileNotFound_throwsIOException() {
        JsonFileReader reader = new JsonFileReader();
        assertThrows(IOException.class, () -> reader.readFromFile("/nonexistent/path.json"));
    }

    @Test
    @DisplayName("null filePath → IllegalArgumentException")
    void nullFilePath_throwsException() {
        JsonFileReader reader = new JsonFileReader();
        assertThrows(IllegalArgumentException.class, () -> reader.readFromFile(null));
    }

    @Test
    @DisplayName("Пустой filePath → IllegalArgumentException")
    void blankFilePath_throwsException() {
        JsonFileReader reader = new JsonFileReader();
        assertThrows(IllegalArgumentException.class, () -> reader.readFromFile("   "));
    }

    @Test
    @DisplayName("null inputStream → IllegalArgumentException")
    void nullInputStream_throwsException() {
        JsonFileReader reader = new JsonFileReader();
        assertThrows(IllegalArgumentException.class, () -> reader.readFromStream(null));
    }

    // ==================== Тесты с мокированием ====================

    @Test
    @DisplayName("Mock: ObjectMapper вызывается с правильным файлом")
    void mock_objectMapperCalledWithCorrectFile() throws IOException {
        // Мокирование тип 1: @Mock с when/thenReturn
        SubsetSumInput expected = new SubsetSumInput(java.util.List.of(1, 2), 3);
        when(mockObjectMapper.readValue(any(File.class), eq(SubsetSumInput.class)))
                .thenReturn(expected);

        Path jsonFile = tempDir.resolve("mock_input.json");
        Files.writeString(jsonFile, "{}");

        JsonFileReader reader = new JsonFileReader(mockObjectMapper);
        SubsetSumInput result = reader.readFromFile(jsonFile.toString());

        assertEquals(expected, result);
        verify(mockObjectMapper).readValue(any(File.class), eq(SubsetSumInput.class));
    }

    @Test
    @DisplayName("Mock: ObjectMapper бросает IOException → пробрасывается")
    void mock_objectMapperThrows_propagated() throws IOException {
        when(mockObjectMapper.readValue(any(File.class), eq(SubsetSumInput.class)))
                .thenThrow(new IOException("Simulated parse error"));

        Path jsonFile = tempDir.resolve("bad.json");
        Files.writeString(jsonFile, "not-json");

        JsonFileReader reader = new JsonFileReader(mockObjectMapper);
        IOException ex = assertThrows(IOException.class,
                () -> reader.readFromFile(jsonFile.toString()));
        assertThat(ex.getMessage(), containsString("Simulated"));
    }

    @Test
    @DisplayName("Spy: ObjectMapper Spy перехватывает реальный вызов")
    void spy_objectMapperInterception() throws IOException {
        // Мокирование тип 2: @Spy — частичное мокирование
        Path jsonFile = tempDir.resolve("spy_input.json");
        Files.writeString(jsonFile, """
                {"numbers": [7, 8, 9], "targetSum": 15}
                """);

        JsonFileReader reader = new JsonFileReader(spyObjectMapper);
        SubsetSumInput input = reader.readFromFile(jsonFile.toString());

        assertNotNull(input);
        assertEquals(15, input.getTargetSum());
        // Верифицируем, что реальный метод был вызван
        verify(spyObjectMapper).readValue(any(File.class), eq(SubsetSumInput.class));
    }

    @Test
    @DisplayName("Mock (inline): создание mock() вручную")
    void inlineMock_objectMapper() throws IOException {
        // Мокирование тип 3: mock() вручную (inline)
        ObjectMapper inlineMock = mock(ObjectMapper.class);
        SubsetSumInput expected = new SubsetSumInput(java.util.List.of(99), 99);
        when(inlineMock.readValue(any(InputStream.class), eq(SubsetSumInput.class)))
                .thenReturn(expected);

        JsonFileReader reader = new JsonFileReader(inlineMock);
        InputStream stream = new ByteArrayInputStream("{}".getBytes());
        SubsetSumInput result = reader.readFromStream(stream);

        assertEquals(expected, result);
        verify(inlineMock, times(1)).readValue(any(InputStream.class), eq(SubsetSumInput.class));
    }
}
