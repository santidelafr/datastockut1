package es.iesisidra.datastock.ui;

import java.io.File;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;

public class MenuPrincipal {
    private final Inventario inventario = new Inventario();
    private final MenuMySQL menuMySQL = new MenuMySQL(inventario);

    public void iniciar() {
        int opcion = -1;
        do {
            System.out.println("\n================================================");
            System.out.println("        DATASTOCK - GESTIÓN DE INVENTARIO       ");
            System.out.println("================================================");
            System.out.println("1. Explorar ficheros y directorios (UT1)");
            System.out.println("2. Importar productos desde JSON (UT1)");
            System.out.println("3. Importar productos desde XML (UT1)");
            System.out.println("4. Consultar productos en memoria (UT1)");
            System.out.println("5. Exportar productos (UT1)");
            System.out.println("6. Guardar y recuperar inventario (UT1)");
            System.out.println("7. Gestionar base de datos MySQL (UT2)");
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
                    case 7:
                        menuMySQL.iniciar();
                        break;
                    case 0:
                        System.out.println("¡Hasta pronto!");
                        break;
                    default:
                        ConsolaColor.aviso("Opción no válida.");
                        break;
                }
            } catch (NumberFormatException e) {
                ConsolaColor.error("Introduce un número válido.");
            } catch (Exception e) {
                ConsolaColor.error(e.getMessage());
            }
        } while (opcion != 0);
    }

    private void explorarRuta() {
        System.out.print("Introduce la ruta: ");
        String entrada = LectorConsola.leerTexto();
        File file = new File(entrada);

        if (!file.exists()) {
            ConsolaColor.error("La ruta especificada no existe.");
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
            ConsolaColor.ok("Fichero JSON procesado. Productos disponibles en memoria: " +
                    inventario.getProductos().size());
        } catch (Exception e) {
            ConsolaColor.error("Error al importar JSON: " + e.getMessage());
        }
    }

    private void importarXML() {
        System.out.print("Ruta del fichero XML: ");
        String ruta = LectorConsola.leerTexto();
        try {
            inventario.importarXML(ruta);
            ConsolaColor.ok("Fichero XML procesado. Productos disponibles en memoria: " +
                    inventario.getProductos().size());
        } catch (Exception e) {
            ConsolaColor.error("Error al importar XML: " + e.getMessage());
        }
    }

    private void menuConsultar() {
        int sub = -1;
        do {
            System.out.println("\n--- CONSULTA DEL INVENTARIO EN MEMORIA (UT1) ---");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona opción: ");
            try {
                sub = Integer.parseInt(LectorConsola.leerTexto());
                switch (sub) {
                    case 1:
                        List<Producto> todos = inventario.listarOrdenados();
                        if (todos.isEmpty()) {
                            System.out.println("No hay productos cargados en memoria.");
                        } else {
                            todos.forEach(System.out::println);
                            System.out.println("Total en memoria: " + todos.size());
                        }
                        break;
                    case 2:
                        System.out.print("Código: ");
                        String cod = LectorConsola.leerTexto();
                        Producto encontrado = inventario.buscarPorCodigo(cod);
                        if (encontrado == null) {
                            System.out.println("No se encontró ningún producto en memoria con ese código.");
                        } else {
                            System.out.println(encontrado);
                        }
                        break;
                    case 3:
                        System.out.print("Nombre a buscar: ");
                        String nom = LectorConsola.leerTexto();
                        List<Producto> coincidencias = inventario.buscarPorNombre(nom);
                        if (coincidencias.isEmpty()) {
                            System.out.println("No se encontraron coincidencias en memoria.");
                        } else {
                            coincidencias.forEach(System.out::println);
                        }
                        break;
                    case 0:
                        break;
                    default:
                        ConsolaColor.aviso("Opción inválida.");
                        break;
                }
            } catch (Exception e) {
                ConsolaColor.error("Entrada inválida.");
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
                    if (!ConsolaColor.confirmar("El fichero ya existe. ¿Sobrescribir?")) {
                        System.out.println("Operación cancelada.");
                        return;
                    }
                }
                if (op == 1) {
                    inventario.exportarJSON(ruta);
                } else {
                    inventario.exportarXML(ruta);
                }
                ConsolaColor.ok("Exportación realizada con éxito.");
            }
        } catch (Exception e) {
            ConsolaColor.error("Error al exportar: " + e.getMessage());
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
                    if (!ConsolaColor.confirmar("Fichero existente. ¿Sobrescribir?")) return;
                }
                inventario.guardarSerializado(ruta);
                ConsolaColor.ok("Inventario guardado correctamente.");
            } else if (op == 2) {
                System.out.print("Ruta del fichero .dat: ");
                String ruta = LectorConsola.leerTexto();
                inventario.recuperarSerializado(ruta);
                ConsolaColor.ok("Inventario recuperado correctamente.");
            }
        } catch (Exception e) {
            ConsolaColor.error("Error en la operación: " + e.getMessage());
        }
    }
}
