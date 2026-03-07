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
import static org.hamcrest.Matchers.*;
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
        assertThat(output, containsString("Главное меню"));
        assertThat(output, containsString("Выберите действие:"));
    }

    @Test
    @DisplayName("isRunning: true до вызова run, false после выхода")
    void isRunning_trueBeforeExit() {
        ConsoleMenu menu = createMenu("0\n");
        assertTrue(menu.isRunning());
        menu.run();
        assertFalse(menu.isRunning());
    }

    @Test
    @DisplayName("Отрицательные числа → ArrayDP пропущен")
    void negativeNumbers_arrayDPSkipped() {
        when(mockArraySolver.getName()).thenReturn("ArrayDP");
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(-1, 5), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n-1 5 3\n4\nn\n0\n");
        menu.run();

        verify(mockArraySolver, never()).solve(any());
        String output = getOutput();
        assertThat(output, containsString("Пропущен"));
        assertThat(output, containsString("не поддерживает отрицательные числа"));
    }

    @Test
    @DisplayName("Отрицательный target → ArrayDP пропущен")
    void negativeTarget_arrayDPSkipped() {
        when(mockArraySolver.getName()).thenReturn("ArrayDP");
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n1 2 3\n-5\nn\n0\n");
        menu.run();

        verify(mockArraySolver, never()).solve(any());
        assertThat(getOutput(), containsString("Пропущен"));
    }

    @Test
    @DisplayName("Сравнение: r1 быстрее r2")
    void comparison_r1Faster() {
        SubsetSumResult r1 = new SubsetSumResult(true, List.of(1), "ArrayDP");
        r1.setExecutionTimeNs(100);
        SubsetSumResult r2 = new SubsetSumResult(true, List.of(1), "HashMapDP");
        r2.setExecutionTimeNs(500);

        when(mockArraySolver.solve(any())).thenReturn(r1);
        when(mockHashMapSolver.solve(any())).thenReturn(r2);

        ConsoleMenu menu = createMenu("1\n1\n1\nn\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("Сравнение алгоритмов"));
        assertThat(output, containsString("быстрее: ArrayDP"));
    }

    @Test
    @DisplayName("Сравнение: r2 быстрее r1")
    void comparison_r2Faster() {
        SubsetSumResult r1 = new SubsetSumResult(true, List.of(1), "ArrayDP");
        r1.setExecutionTimeNs(500);
        SubsetSumResult r2 = new SubsetSumResult(true, List.of(1), "HashMapDP");
        r2.setExecutionTimeNs(100);

        when(mockArraySolver.solve(any())).thenReturn(r1);
        when(mockHashMapSolver.solve(any())).thenReturn(r2);

        ConsoleMenu menu = createMenu("1\n1\n1\nn\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("быстрее: HashMapDP"));
    }

    @Test
    @DisplayName("Сравнение: одинаковое время")
    void comparison_equalTimes() {
        SubsetSumResult r1 = new SubsetSumResult(true, List.of(1), "ArrayDP");
        r1.setExecutionTimeNs(200);
        SubsetSumResult r2 = new SubsetSumResult(true, List.of(1), "HashMapDP");
        r2.setExecutionTimeNs(200);

        when(mockArraySolver.solve(any())).thenReturn(r1);
        when(mockHashMapSolver.solve(any())).thenReturn(r2);

        ConsoleMenu menu = createMenu("1\n1\n1\nn\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("одинаково"));
    }

    @Test
    @DisplayName("HashMapDP бросает исключение")
    void hashMapSolverException() {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "ArrayDP"));
        when(mockHashMapSolver.getName()).thenReturn("HashMapDP");
        when(mockHashMapSolver.solve(any()))
                .thenThrow(new RuntimeException("HashMap error"));

        ConsoleMenu menu = createMenu("1\n1\n1\nn\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("[HashMapDP] Ошибка"));
        assertThat(output, containsString("HashMap error"));
    }

    @Test
    @DisplayName("Ошибка записи в файл → сообщение об ошибке")
    void saveResult_ioException() throws IOException {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "HashMapDP"));
        doThrow(new IOException("disk full")).when(mockWriter).writeToFile(any(), any());

        ConsoleMenu menu = createMenu("1\n1\n1\ny\n/tmp/out.json\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Ошибка записи"));
    }

    @Test
    @DisplayName("Сохранение по 'yes' (полное слово)")
    void saveResultWithYes() throws IOException {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n1\n1\nyes\n/tmp/yes.json\n0\n");
        menu.run();

        verify(mockWriter).writeToFile(any(SubsetSumResult.class), eq("/tmp/yes.json"));
        assertThat(getOutput(), containsString("Результат сохранён"));
    }

    @Test
    @DisplayName("Оба солвера бросают исключения → сохранение не вызывается")
    void bothSolversFail_noSave() {
        when(mockArraySolver.getName()).thenReturn("ArrayDP");
        when(mockArraySolver.solve(any())).thenThrow(new RuntimeException("err1"));
        when(mockHashMapSolver.getName()).thenReturn("HashMapDP");
        when(mockHashMapSolver.solve(any())).thenThrow(new RuntimeException("err2"));

        ConsoleMenu menu = createMenu("1\n1 2 3\n5\ny\n/tmp/out.json\n0\n");
        menu.run();

        verifyNoInteractions(mockWriter);
        String output = getOutput();
        assertThat(output, containsString("[ArrayDP] Ошибка"));
        assertThat(output, containsString("[HashMapDP] Ошибка"));
    }

    @Test
    @DisplayName("printResult: результат найден — полный вывод")
    void printResult_found_fullOutput() {
        SubsetSumResult result = new SubsetSumResult(true, List.of(2, 3), "ArrayDP");
        result.setExecutionTimeNs(1_500_000);
        result.setMemoryUsedBytes(1024);

        when(mockArraySolver.solve(any())).thenReturn(result);
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(2, 3), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n2 3 5\n5\nn\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("--- Результат: ArrayDP ---"));
        assertThat(output, containsString("Решение найдено: Да"));
        assertThat(output, containsString("Подмножество:"));
        assertThat(output, containsString("Сумма подмножества:"));
        assertThat(output, containsString("Время выполнения:"));
        assertThat(output, containsString("Использовано памяти:"));
        assertThat(output, containsString("1024 байт"));
    }

    @Test
    @DisplayName("printResult: результат не найден — без подмножества")
    void printResult_notFound_noSubset() {
        SubsetSumResult result = new SubsetSumResult(false, List.of(), "ArrayDP");

        when(mockArraySolver.solve(any())).thenReturn(result);
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(false, List.of(), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n1 2 3\n99\nn\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("Решение найдено: Нет"));
        assertThat(output, not(containsString("Подмножество:")));
        assertThat(output, not(containsString("Сумма подмножества:")));
    }

    @Test
    @DisplayName("run() выводит заголовок приложения")
    void run_displaysHeader() {
        ConsoleMenu menu = createMenu("0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("=== Subset Sum Solver ==="));
        assertThat(output, containsString("NP-полной задачи Subset Sum"));
    }

    @Test
    @DisplayName("Справка: подробная проверка содержимого")
    void helpCommand_detailedOutput() {
        ConsoleMenu menu = createMenu("3\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("=== Справка ==="));
        assertThat(output, containsString("NP-полная задача"));
        assertThat(output, containsString("подмножество"));
        assertThat(output, containsString("два алгоритма"));
        assertThat(output, containsString("ArrayDP"));
        assertThat(output, containsString("boolean[][]"));
        assertThat(output, containsString("HashMapDP"));
        assertThat(output, containsString("HashMap"));
        assertThat(output, containsString("JSON-файл"));
        assertThat(output, containsString("Форматы ввода/вывода"));
    }

    @Test
    @DisplayName("solveAndDisplay: отображает входные данные")
    void solveAndDisplay_displaysInput() {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(5), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(5), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n5\n5\nn\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Входные данные:"));
    }

    @Test
    @DisplayName("solveAndDisplay: отображает запрос на сохранение")
    void solveAndDisplay_displaysPrompt() {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(5), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(5), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n5\n5\nn\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Сохранить результат в JSON-файл?"));
    }

    @Test
    @DisplayName("handleManualInput: отображает подсказки ввода")
    void handleManualInput_displaysPrompts() {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n1\n1\nn\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("Введите числа через пробел:"));
        assertThat(output, containsString("Введите целевую сумму:"));
    }

    @Test
    @DisplayName("handleFileInput: отображает подсказку пути")
    void handleFileInput_displaysPrompt() throws IOException {
        SubsetSumInput fileInput = new SubsetSumInput(List.of(1), 1);
        when(mockReader.readFromFile(any())).thenReturn(fileInput);
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "HashMapDP"));

        ConsoleMenu menu = createMenu("2\n/path/f.json\nn\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Введите путь к JSON-файлу:"));
    }

    @Test
    @DisplayName("Сохранение: запрашивает путь при согласии")
    void saveResult_asksForPath() throws IOException {
        when(mockArraySolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "ArrayDP"));
        when(mockHashMapSolver.solve(any()))
                .thenReturn(new SubsetSumResult(true, List.of(1), "HashMapDP"));

        ConsoleMenu menu = createMenu("1\n1\n1\ny\n/tmp/x.json\n0\n");
        menu.run();

        assertThat(getOutput(), containsString("Введите путь для сохранения:"));
    }

    @Test
    @DisplayName("printComparison: отображает время и память обоих")
    void comparison_displaysTimingsAndMemory() {
        SubsetSumResult r1 = new SubsetSumResult(true, List.of(1), "ArrayDP");
        r1.setExecutionTimeNs(1_000_000);
        r1.setMemoryUsedBytes(2048);
        SubsetSumResult r2 = new SubsetSumResult(true, List.of(1), "HashMapDP");
        r2.setExecutionTimeNs(2_000_000);
        r2.setMemoryUsedBytes(4096);

        when(mockArraySolver.solve(any())).thenReturn(r1);
        when(mockHashMapSolver.solve(any())).thenReturn(r2);

        ConsoleMenu menu = createMenu("1\n1\n1\nn\n0\n");
        menu.run();

        String output = getOutput();
        assertThat(output, containsString("Сравнение алгоритмов"));
        assertThat(output, containsString("ArrayDP"));
        assertThat(output, containsString("HashMapDP"));
        assertThat(output, containsString("2048"));
        assertThat(output, containsString("4096"));
    }

    @Test
    @DisplayName("Ввод вручную → только цифра 0 → нет решения без подмножества в выводе")
    void handleChoice_menuOptions() {
        ConsoleMenu menu = createMenu("0\n");
        menu.handleChoice("0");

        assertFalse(menu.isRunning());
        assertThat(getOutput(), containsString("До свидания!"));
    }
}
