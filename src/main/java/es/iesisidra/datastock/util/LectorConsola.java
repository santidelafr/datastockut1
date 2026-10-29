package es.iesisidra.datastock.util;

import java.util.Scanner;

public class LectorConsola {
    private static final Scanner scanner = new Scanner(System.in);

    public static String leerTexto() {
        return scanner.nextLine().trim();
    }
}