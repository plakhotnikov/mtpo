package com.subsetsum.ui;

import com.subsetsum.algorithm.ArrayDPSolver;
import com.subsetsum.algorithm.HashMapDPSolver;
import com.subsetsum.algorithm.SubsetSumSolver;
import com.subsetsum.io.JsonFileReader;
import com.subsetsum.io.JsonFileWriter;
import com.subsetsum.model.SubsetSumInput;
import com.subsetsum.model.SubsetSumResult;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner scanner;
    private final PrintStream out;
    private final SubsetSumSolver arraySolver;
    private final SubsetSumSolver hashMapSolver;
    private final JsonFileReader jsonReader;
    private final JsonFileWriter jsonWriter;

    private boolean running;

    public ConsoleMenu(Scanner scanner, PrintStream out) {
        this(scanner, out, new ArrayDPSolver(), new HashMapDPSolver(),
                new JsonFileReader(), new JsonFileWriter());
    }

    public ConsoleMenu(Scanner scanner, PrintStream out,
                       SubsetSumSolver arraySolver, SubsetSumSolver hashMapSolver,
                       JsonFileReader jsonReader, JsonFileWriter jsonWriter) {
        this.scanner = scanner;
        this.out = out;
        this.arraySolver = arraySolver;
        this.hashMapSolver = hashMapSolver;
        this.jsonReader = jsonReader;
        this.jsonWriter = jsonWriter;
        this.running = true;
    }

    public void run() {
        out.println("=== Subset Sum Solver ===");
        out.println("Решение NP-полной задачи Subset Sum двумя алгоритмами");
        out.println();

        while (running) {
            printMenu();
            String choice = readLine().trim();
            handleChoice(choice);
        }
    }

    void printMenu() {
        out.println("--- Главное меню ---");
        out.println("1. Ввести данные вручную");
        out.println("2. Загрузить данные из JSON-файла");
        out.println("3. Справка");
        out.println("0. Выход");
        out.print("Выберите действие: ");
    }

    void handleChoice(String choice) {
        switch (choice) {
            case "1" -> handleManualInput();
            case "2" -> handleFileInput();
            case "3" -> printHelp();
            case "0" -> {
                running = false;
                out.println("До свидания!");
            }
            default -> out.println("Неизвестная команда. Введите 0-3.");
        }
    }

    void handleManualInput() {
        out.print("Введите числа через пробел: ");
        String numbersLine = readLine().trim();
        if (numbersLine.isEmpty()) {
            out.println("Ошибка: введена пустая строка.");
            return;
        }

        List<Integer> numbers;
        try {
            numbers = parseNumbers(numbersLine);
        } catch (NumberFormatException e) {
            out.println("Ошибка: некорректный формат чисел.");
            return;
        }

        out.print("Введите целевую сумму: ");
        String targetLine = readLine().trim();
        int target;
        try {
            target = Integer.parseInt(targetLine);
        } catch (NumberFormatException e) {
            out.println("Ошибка: некорректный формат целевой суммы.");
            return;
        }

        SubsetSumInput input = new SubsetSumInput(numbers, target);
        solveAndDisplay(input);
    }

    void handleFileInput() {
        out.print("Введите путь к JSON-файлу: ");
        String filePath = readLine().trim();
        if (filePath.isEmpty()) {
            out.println("Ошибка: путь не указан.");
            return;
        }

        SubsetSumInput input;
        try {
            input = jsonReader.readFromFile(filePath);
        } catch (IOException e) {
            out.println("Ошибка чтения файла: " + e.getMessage());
            return;
        }

        solveAndDisplay(input);
    }

    void solveAndDisplay(SubsetSumInput input) {
        out.println("\nВходные данные: " + input);
        out.println();

        SubsetSumResult arrayResult = null;
        boolean hasNegative = input.getNumbers().stream().anyMatch(n -> n < 0);
        boolean negativeTarget = input.getTargetSum() < 0;

        if (!hasNegative && !negativeTarget) {
            try {
                arrayResult = arraySolver.solve(input);
                printResult(arrayResult);
            } catch (Exception e) {
                out.println("[" + arraySolver.getName() + "] Ошибка: " + e.getMessage());
            }
        } else {
            out.println("[" + arraySolver.getName() + "] Пропущен: не поддерживает отрицательные числа.");
        }

        SubsetSumResult hashResult = null;
        try {
            hashResult = hashMapSolver.solve(input);
            printResult(hashResult);
        } catch (Exception e) {
            out.println("[" + hashMapSolver.getName() + "] Ошибка: " + e.getMessage());
        }

        if (arrayResult != null && hashResult != null) {
            printComparison(arrayResult, hashResult);
        }

        out.print("\nСохранить результат в JSON-файл? (y/n): ");
        String save = readLine().trim().toLowerCase();
        if ("y".equals(save) || "yes".equals(save)) {
            out.print("Введите путь для сохранения: ");
            String outPath = readLine().trim();
            SubsetSumResult resultToSave = hashResult != null ? hashResult : arrayResult;
            if (resultToSave != null) {
                try {
                    jsonWriter.writeToFile(resultToSave, outPath);
                    out.println("Результат сохранён в " + outPath);
                } catch (IOException e) {
                    out.println("Ошибка записи: " + e.getMessage());
                }
            }
        }
        out.println();
    }

    void printResult(SubsetSumResult result) {
        out.println("--- Результат: " + result.getAlgorithmName() + " ---");
        out.println("  Решение найдено: " + (result.isFound() ? "Да" : "Нет"));
        if (result.isFound()) {
            out.println("  Подмножество: " + result.getSubset());
            out.println("  Сумма подмножества: " + result.getSubsetSum());
        }
        out.printf("  Время выполнения: %.3f мс%n", result.getExecutionTimeMs());
        out.println("  Использовано памяти: " + result.getMemoryUsedBytes() + " байт");
    }

    void printComparison(SubsetSumResult r1, SubsetSumResult r2) {
        out.println("\n--- Сравнение алгоритмов ---");
        out.printf("  %-35s  Время: %10.3f мс | Память: %d байт%n",
                r1.getAlgorithmName(), r1.getExecutionTimeMs(), r1.getMemoryUsedBytes());
        out.printf("  %-35s  Время: %10.3f мс | Память: %d байт%n",
                r2.getAlgorithmName(), r2.getExecutionTimeMs(), r2.getMemoryUsedBytes());

        if (r1.getExecutionTimeNs() < r2.getExecutionTimeNs()) {
            out.println("  -> По времени быстрее: " + r1.getAlgorithmName());
        } else if (r2.getExecutionTimeNs() < r1.getExecutionTimeNs()) {
            out.println("  -> По времени быстрее: " + r2.getAlgorithmName());
        } else {
            out.println("  -> По времени одинаково.");
        }
    }

    void printHelp() {
        out.println();
        out.println("=== Справка ===");
        out.println("Subset Sum — NP-полная задача: определить, существует ли");
        out.println("подмножество заданного набора целых чисел, сумма которых");
        out.println("равна заданному целевому значению.");
        out.println();
        out.println("Программа использует два алгоритма:");
        out.println("  1. ArrayDP — динамическое программирование на двумерном");
        out.println("     массиве boolean[][]. Работает только с числами >= 0.");
        out.println("     Сложность: O(n * target) по времени и памяти.");
        out.println("  2. HashMapDP — ДП на HashMap, хранящей достижимые суммы.");
        out.println("     Поддерживает любые целые числа (включая отрицательные).");
        out.println("     Эффективнее по памяти для разреженных данных.");
        out.println();
        out.println("Форматы ввода/вывода:");
        out.println("  Ввод: вручную или JSON-файл {\"numbers\":[...], \"targetSum\":N}");
        out.println("  Вывод: в консоль и/или JSON-файл.");
        out.println();
    }

    List<Integer> parseNumbers(String line) {
        String[] parts = line.split("\\s+");
        List<Integer> numbers = new ArrayList<>();
        for (String part : parts) {
            numbers.add(Integer.parseInt(part));
        }
        return numbers;
    }

    private String readLine() {
        return scanner.nextLine();
    }

    public boolean isRunning() {
        return running;
    }
}
