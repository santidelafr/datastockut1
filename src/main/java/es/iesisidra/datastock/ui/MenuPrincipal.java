package es.iesisidra.datastock.ui;

import java.io.File;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;

public class MenuPrincipal {
    private final Inventario inventario = new Inventario();

    public void iniciar() {
        int opcion = -1;
        do {
            System.out.println("\n=================================");
            System.out.println(" DATASTOCK GESTIÓN DE INVENTARIO ");
            System.out.println("=================================");
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(LectorConsola.leerTexto());
                switch (opcion) {
                    case 1:
                        explorarRuta();
                        break;
                    case 2:
                        importarJSON();
                        break;
                    case 3:
                        importarXML();
                        break;
                    case 4:
                        menuConsultar();
                        break;
                    case 5:
                        menuExportar();
                        break;
                    case 6:
                        menuSerializacion();
                        break;
                    case 0:
                        System.out.println("¡Hasta pronto!");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Introduce un número válido.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void explorarRuta() {
        System.out.print("Introduce la ruta: ");
        String entrada = LectorConsola.leerTexto();
        File file = new File(entrada);

        if (!file.exists()) {
            System.out.println("Error: La ruta especificada no existe.");
            return;
        }

        if (file.isFile()) {
            System.out.println("[FICHERO] " + file.getName());
            System.out.println("Ruta absoluta: " + file.getAbsolutePath());
            System.out.println("Tamaño: " + file.length() + " bytes");
        } else if (file.isDirectory()) {
            System.out.println("===== CONTENIDO DEL DIRECTORIO =====");
            File[] hijos = file.listFiles();
            if (hijos == null || hijos.length == 0) {
                System.out.println("El directorio está vacío.");
                return;
            }
            int totalFicheros = 0, totalDirs = 0;
            for (File f : hijos) {
                if (f.isDirectory()) {
                    System.out.printf("[DIR]     %-25s%n", f.getName());
                    totalDirs++;
                } else {
                    System.out.printf("[FICHERO] %-25s %d bytes%n", f.getName(), f.length());
                    totalFicheros++;
                }
            }
            System.out.println("------------------------------------");
            System.out.println("Total ficheros: " + totalFicheros + " | Directorios: " + totalDirs);
        }
    }

    private void importarJSON() {
        System.out.print("Ruta del fichero JSON: ");
        String ruta = LectorConsola.leerTexto();
        try {
            inventario.importarJSON(ruta);
        } catch (Exception e) {
            System.out.println("Error al importar JSON: " + e.getMessage());
        }
    }

    private void importarXML() {
        System.out.print("Ruta del fichero XML: ");
        String ruta = LectorConsola.leerTexto();
        try {
            inventario.importarXML(ruta);
        } catch (Exception e) {
            System.out.println("Error al importar XML: " + e.getMessage());
        }
    }

    private void menuConsultar() {
        int sub = -1;
        do {
            System.out.println("\n--- CONSULTA DE PRODUCTOS ---");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona opción: ");
            try {
                sub = Integer.parseInt(LectorConsola.leerTexto());
                switch (sub) {
                    case 1:
                        inventario.listarOrdenados();
                        break;
                    case 2:
                        System.out.print("Código: ");
                        String cod = LectorConsola.leerTexto();
                        inventario.buscarPorCodigo(cod);
                        break;
                    case 3:
                        System.out.print("Nombre a buscar: ");
                        String nom = LectorConsola.leerTexto();
                        inventario.buscarPorNombre(nom);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opción inválida.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida.");
            }
        } while (sub != 0);
    }

    private void menuExportar() {
        System.out.println("\n--- EXPORTAR PRODUCTOS ---");
        System.out.println("1. Exportar a JSON");
        System.out.println("2. Exportar a XML");
        System.out.println("0. Volver");
        System.out.print("Opción: ");
        try {
            int op = Integer.parseInt(LectorConsola.leerTexto());
            if (op == 1 || op == 2) {
                System.out.print("Ruta de destino: ");
                String ruta = LectorConsola.leerTexto();
                File f = new File(ruta);
                if (f.exists()) {
                    System.out.print("El fichero ya existe. ¿Sobrescribir? (S/N): ");
                    if (!LectorConsola.leerTexto().equalsIgnoreCase("S")) {
                        System.out.println("Operación cancelada.");
                        return;
                    }
                }
                if (op == 1) {
                    inventario.exportarJSON(ruta);
                } else {
                    inventario.exportarXML(ruta);
                }
                System.out.println("Exportación realizada con éxito.");
            }
        } catch (Exception e) {
            System.out.println("Error al exportar: " + e.getMessage());
        }
    }

    private void menuSerializacion() {
        System.out.println("\n--- GUARDAR Y RECUPERAR INVENTARIO ---");
        System.out.println("1. Guardar inventario en fichero.dat");
        System.out.println("2. Recuperar inventario desde fichero.dat");
        System.out.println("0. Volver");
        System.out.print("Opción: ");
        try {
            int op = Integer.parseInt(LectorConsola.leerTexto());
            if (op == 1) {
                System.out.print("Ruta de destino (.dat): ");
                String ruta = LectorConsola.leerTexto();
                File f = new File(ruta);
                if (f.exists()) {
                    System.out.print("Fichero existente. ¿Sobrescribir? (S/N): ");
                    if (!LectorConsola.leerTexto().equalsIgnoreCase("S")) return;
                }
                inventario.guardarSerializado(ruta);
                System.out.println("Inventario guardado correctamente.");
            } else if (op == 2) {
                System.out.print("Ruta del fichero .dat: ");
                String ruta = LectorConsola.leerTexto();
                inventario.recuperarSerializado(ruta);
                System.out.println("Inventario recuperado correctamente.");
            }
        } catch (Exception e) {
            System.out.println("Error en la operación: " + e.getMessage());
        }
    }
}