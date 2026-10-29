package es.iesisidra.datastock.complementarias;

import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import es.iesisidra.datastock.modelo.Producto;

public class AccesoAleatorioActividad {
    // 12 chars código (24 bytes) + double precio (8 bytes) + int stock (4 bytes) = 36 bytes
    private static final int LONGITUD_REGISTRO = 36;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String rutaBin = "datos/stock.bin";

        // Simulamos copia de productos en memoria para la prueba
        List<Producto> listaMemoria = new ArrayList<>();
        listaMemoria.add(new Producto("TEC-001", "Teclado USB", new BigDecimal("19.95"), 12, 1, 1));
        listaMemoria.add(new Producto("RAT-002", "Ratón Inalámbrico", new BigDecimal("14.50"), 25, 1, 1));
        listaMemoria.add(new Producto("MON-003", "Monitor 24 Pulgadas", new BigDecimal("149.99"), 8, 2, 2));
        listaMemoria.add(new Producto("AUR-004", "Auriculares Gaming", new BigDecimal("45.00"), 10, 3, 1));

        try {
            // 1. Generar fichero de registros fijos
            try (RandomAccessFile raf = new RandomAccessFile(rutaBin, "rw")) {
                raf.setLength(0); // Truncar fichero
                for (Producto p : listaMemoria) {
                    StringBuffer sb = new StringBuffer(p.getCodigo());
                    sb.setLength(12); // Rellenar o truncar a 12 caracteres
                    raf.writeChars(sb.toString());
                    raf.writeDouble(p.getPrecio().doubleValue());
                    raf.writeInt(p.getStock());
                }
            }

            // 2. Operaciones con RandomAccessFile
            try (RandomAccessFile raf = new RandomAccessFile(rutaBin, "rw")) {
                long totalRegistros = raf.length() / LONGITUD_REGISTRO;
                System.out.println("===== ACCESO ALEATORIO =====");
                System.out.println("Fichero: " + rutaBin);
                System.out.println("Registros: " + totalRegistros + " | Tamaño: " + raf.length() + " bytes");

                if (raf.length() % LONGITUD_REGISTRO != 0) {
                    System.out.println("Error: Fichero binario corrupto o inválido.");
                    return;
                }

                System.out.print("Consultar posición (0-" + (totalRegistros - 1) + "): ");
                int pos = Integer.parseInt(sc.nextLine());

                if (pos < 0 || pos >= totalRegistros) {
                    System.out.println("Error: Posición fuera de rango.");
                    return;
                }

                // Desplazamiento directo al registro
                raf.seek((long) pos * LONGITUD_REGISTRO);
                StringBuilder cod = new StringBuilder();
                for (int i = 0; i < 12; i++) cod.append(raf.readChar());
                double precio = raf.readDouble();
                int stockActual = raf.readInt();

                System.out.printf("%s | %.2f € | stock %d%n", cod.toString().trim(), precio, stockActual);

                System.out.print("Nuevo stock: ");
                int nuevoStock = Integer.parseInt(sc.nextLine());

                // Desplazamiento exacto al campo stock (seek directo)
                raf.seek((long) pos * LONGITUD_REGISTRO + 32);
                raf.writeInt(nuevoStock);

                System.out.println("Actualización correcta: stock " + stockActual + " -> " + nuevoStock);
            }

        } catch (Exception e) {
            System.out.println("Error en RAF: " + e.getMessage());
        }
    }
}