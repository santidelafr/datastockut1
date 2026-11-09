package es.iesisidra.datastock.util;

/**
 * Constantes ANSI y métodos auxiliares para mostrar mensajes diferenciados
 * por consola manteniendo prefijos legibles ([OK], [ERROR], [AVISO]) aunque
 * la terminal no soporte colores.
 */
public class ConsolaColor {
    public static final String RESET = "\u001B[0m";
    public static final String ROJO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARILLO = "\u001B[33m";

    private ConsolaColor() {}

    public static void ok(String mensaje) {
        System.out.println(VERDE + "[OK] " + mensaje + RESET);
    }

    public static void error(String mensaje) {
        System.out.println(ROJO + "[ERROR] " + mensaje + RESET);
    }

    public static void aviso(String mensaje) {
        System.out.println(AMARILLO + "[AVISO] " + mensaje + RESET);
    }

    /**
     * Solicita confirmación S/N al usuario repitiendo la pregunta si introduce
     * un valor distinto de S o N.
     */
    public static boolean confirmar(String pregunta) {
        while (true) {
            System.out.print(AMARILLO + "[AVISO] " + pregunta + " (S/N): " + RESET);
            String respuesta = LectorConsola.leerTexto();
            if ("S".equalsIgnoreCase(respuesta)) {
                return true;
            }
            if ("N".equalsIgnoreCase(respuesta)) {
                return false;
            }
            aviso("Respuesta no válida. Introduce 'S' para confirmar o 'N' para cancelar.");
        }
    }
}
