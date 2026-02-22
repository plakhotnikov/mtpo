package com.subsetsum.ui;

import com.subsetsum.algorithm.SubsetSumSolver;
import com.subsetsum.io.JsonFileReader;
import com.subsetsum.io.JsonFileWriter;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsoleMenu Tests")
class ConsoleMenuTest {

    @Mock
    private SubsetSumSolver mockArraySolver;
    @Mock
    private SubsetSumSolver mockHashMapSolver;
    @Mock
    private JsonFileReader mockReader;
    @Mock
    private JsonFileWriter mockWriter;

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
    }

    private ConsoleMenu createMenu(String userInput) {
        Scanner scanner = new Scanner(userInput);
        PrintStream printStream = new PrintStream(outputStream);
        return new ConsoleMenu(scanner, printStream,
                mockArraySolver, mockHashMapSolver,
                mockReader, mockWriter);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    @Test
    @DisplayName("Команда '0' → выход")
    void exitCommand() {
        ConsoleMenu menu = createMenu("0\n");
        menu.run();

        assertFalse(menu.isRunning());
        assertThat(getOutput(), containsString("До свидания"));
    }

    @Test
    @DisplayName("Команда '3' → справка")
    void helpCommand() {
        ConsoleMenu menu = createMenu("3\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("Справка"));
        assertThat(output, containsString("Subset Sum"));
        assertThat(output, containsString("ArrayDP"));
        assertThat(output, containsString("HashMapDP"));
    }

    @Test
    @DisplayName("Неизвестная команда → сообщение об ошибке")
    void unknownCommand() {
        ConsoleMenu menu = createMenu("9\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Неизвестная команда"));
    }

    @Test
    @DisplayName("Ручной ввод → вызов обоих солверов, отказ от сохранения")
    void manualInput_bothSolversCalled() {
        SubsetSumResult resultArray = new SubsetSumResult(true, List.of(3, 8), "ArrayDP");
        SubsetSumResult resultHash = new SubsetSumResult(true, List.of(3, 8), "HashMapDP");

        when(mockArraySolver.solve(any())).thenReturn(resultArray);
        when(mockHashMapSolver.solve(any())).thenReturn(resultHash);

        ConsoleMenu menu = createMenu("1\n3 7 1 8 5\n11\nn\n0\n");
        menu.run();

        verify(mockArraySolver).solve(any(SubsetSumInput.class));
        verify(mockHashMapSolver).solve(any(SubsetSumInput.class));

        String output = getOutput();
        assertThat(output, containsString("Решение найдено: Да"));
    }

    @Test
    @DisplayName("Ручной ввод → некорректные числа")
    void manualInput_invalidNumbers() {
        ConsoleMenu menu = createMenu("1\nabc def\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("некорректный формат"));
    }

    @Test
    @DisplayName("Ручной ввод → пустая строка")
    void manualInput_emptyLine() {
        ConsoleMenu menu = createMenu("1\n\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("пустая строка"));
    }

    @Test
    @DisplayName("Ручной ввод → некорректная целевая сумма")
    void manualInput_invalidTarget() {
        ConsoleMenu menu = createMenu("1\n1 2 3\nabc\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("некорректный формат целевой суммы"));
    }

    @Test
    @DisplayName("Загрузка из файла → успех")
    void fileInput_success() throws IOException {
        SubsetSumInput fileInput = new SubsetSumInput(List.of(1, 2, 3), 5);
        when(mockReader.readFromFile(any())).thenReturn(fileInput);
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(2, 3), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(2, 3), "HashMapDP"));

        ConsoleMenu menu = createMenu("2\n/path/to/file.json\nn\n0\n");
        menu.run();

        verify(mockReader).readFromFile("/path/to/file.json");
        assertThat(getOutput(), containsString("Решение найдено: Да"));
    }

    @Test
    @DisplayName("Загрузка из файла → ошибка чтения")
    void fileInput_readError() throws IOException {
        when(mockReader.readFromFile(any())).thenThrow(new IOException("File not found"));

        ConsoleMenu menu = createMenu("2\n/bad/path.json\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Ошибка чтения файла"));
    }

    @Test
    @DisplayName("Загрузка из файла → пустой путь")
    void fileInput_emptyPath() {
        ConsoleMenu menu = createMenu("2\n\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("путь не указан"));
    }

    @Test
    @DisplayName("Сохранение результата в файл")
    void saveResultToFile() throws IOException {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(5), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(5), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n5\n5\ny\n/tmp/out.json\n0\n");
        menu.run();

        verify(mockWriter).writeToFile(any(SubsetSumResult.class), eq("/tmp/out.json"));
        assertThat(getOutput(), containsString("Результат сохранён"));
    }

    @Test
    @DisplayName("Солвер бросает исключение → сообщение об ошибке")
    void solverException_handledGracefully() {
        when(mockArraySolver.getName()).thenReturn("ArrayDP");
        when(mockArraySolver.solve(any()))
                .thenThrow(new RuntimeException("Out of memory"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(false, List.of(), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n1 2 3\n5\nn\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Ошибка"));
    }

    @Test
    @DisplayName("parseNumbers: корректный парсинг")
    void parseNumbers_valid() {
        ConsoleMenu menu = createMenu("0\n");
        List<Integer> nums = menu.parseNumbers("1 2 3 4 5");

        assertEquals(5, nums.size());
        assertEquals(List.of(1, 2, 3, 4, 5), nums);
    }

    @Test
    @DisplayName("parseNumbers: некорректный ввод → NumberFormatException")
    void parseNumbers_invalid() {
        ConsoleMenu menu = createMenu("0\n");
        assertThrows(NumberFormatException.class, () -> menu.parseNumbers("1 abc 3"));
    }

    @Test
    @DisplayName("printMenu отображает все пункты")
    void printMenu_showsAllOptions() {
        ConsoleMenu menu = createMenu("0\n");
        menu.printMenu();

        String output = getOutput();
        assertThat(output, containsString("1."));
        assertThat(output, containsString("2."));
        assertThat(output, containsString("3."));
        assertThat(output, containsString("0."));
    }
}
