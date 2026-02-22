package com.subsetsum;

import com.subsetsum.ui.ConsoleMenu;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConsoleMenu menu = new ConsoleMenu(scanner, System.out);
        menu.run();
        scanner.close();
    }
}
