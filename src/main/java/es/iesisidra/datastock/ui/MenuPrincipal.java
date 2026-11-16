package es.iesisidra.datastock.ui;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.componente.LectorProductosJson;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Menú principal de DataStock (Opciones 1 a 6 de UT1 y Opción 7 de UT2/UT3).
 * Integra el componente reutilizable {@link LectorProductosJson} en la opción 2 (UT3).
 */
public class MenuPrincipal {
    private final Inventario inventario = new Inventario();
    private final LectorProductosJson lectorProductosJson = new LectorProductosJson();
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
                    case 1 -> explorarRuta();
                    case 2 -> importarJSON();
                    case 3 -> importarXML();
                    case 4 -> menuConsultar();
                    case 5 -> menuExportar();
                    case 6 -> menuSerializacion();
                    case 7 -> menuMySQL.iniciar();
                    case 0 -> System.out.println("¡Hasta pronto!");
                    default -> ConsolaColor.aviso("Opción no válida.");
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
        File file = new File(LectorConsola.leerTexto());
        if (!file.exists()) {
            ConsolaColor.error("La ruta especificada no existe.");
            return;
        }
        if (file.isFile()) {
            System.out.printf("[FICHERO] %s | Ruta: %s | %d bytes%n", file.getName(), file.getAbsolutePath(), file.length());
        } else if (file.isDirectory()) {
            File[] hijos = file.listFiles();
            if (hijos == null || hijos.length == 0) {
                System.out.println("El directorio está vacío.");
                return;
            }
            int ficheros = 0, dirs = 0;
            for (File f : hijos) {
                if (f.isDirectory()) { System.out.printf("[DIR]     %-25s%n", f.getName()); dirs++; }
                else { System.out.printf("[FICHERO] %-25s %d bytes%n", f.getName(), f.length()); ficheros++; }
            }
            System.out.println("Total ficheros: " + ficheros + " | Directorios: " + dirs);
        }
    }

    // Opción 2: Integrada con el componente reutilizable LectorProductosJson (UT3)
    private void importarJSON() {
        System.out.print("Ruta del fichero JSON: ");
        String ruta = LectorConsola.leerTexto();
        try {
            LectorProductosJson.ResultadoLectura res = lectorProductosJson.leerConDetalle(Paths.get(ruta));
            int importados = 0;
            List<String> incidencias = new ArrayList<>(res.getIncidencias());

            for (Producto p : res.getProductosValidos()) {
                if (inventario.agregarProducto(p)) importados++;
                else incidencias.add("Producto (" + p.getCodigo() + "): código duplicado en memoria.");
            }

            int encontrados = res.getTotalEncontrados();
            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + (encontrados - importados));
            if (!incidencias.isEmpty()) {
                ConsolaColor.aviso("Incidencias detectadas:");
                incidencias.forEach(inc -> System.out.println(" - " + inc));
            }
            ConsolaColor.ok("Fichero JSON procesado. Productos disponibles en memoria: " + inventario.getProductos().size());
        } catch (Exception e) {
            ConsolaColor.error("Error al importar JSON: " + e.getMessage());
        }
    }

    private void importarXML() {
        System.out.print("Ruta del fichero XML: ");
        try {
            inventario.importarXML(LectorConsola.leerTexto());
            ConsolaColor.ok("Fichero XML procesado. Productos disponibles en memoria: " + inventario.getProductos().size());
        } catch (Exception e) {
            ConsolaColor.error("Error al importar XML: " + e.getMessage());
        }
    }

    private void menuConsultar() {
        int sub = -1;
        do {
            System.out.println("\n--- CONSULTA EN MEMORIA (UT1) ---");
            System.out.println("1. Listar todos | 2. Buscar por código | 3. Buscar por nombre | 0. Volver");
            System.out.print("Selecciona opción: ");
            try {
                sub = Integer.parseInt(LectorConsola.leerTexto());
                switch (sub) {
                    case 1 -> {
                        List<Producto> todos = inventario.listarOrdenados();
                        if (todos.isEmpty()) System.out.println("No hay productos en memoria.");
                        else { todos.forEach(System.out::println); System.out.println("Total: " + todos.size()); }
                    }
                    case 2 -> {
                        System.out.print("Código: ");
                        Producto p = inventario.buscarPorCodigo(LectorConsola.leerTexto());
                        System.out.println(p != null ? p : "No se encontró ningún producto con ese código.");
                    }
                    case 3 -> {
                        System.out.print("Nombre a buscar: ");
                        List<Producto> lista = inventario.buscarPorNombre(LectorConsola.leerTexto());
                        if (lista.isEmpty()) System.out.println("No se encontraron coincidencias.");
                        else lista.forEach(System.out::println);
                    }
                    case 0 -> {}
                    default -> ConsolaColor.aviso("Opción inválida.");
                }
            } catch (Exception e) {
                ConsolaColor.error("Entrada inválida.");
            }
        } while (sub != 0);
    }

    private void menuExportar() {
        System.out.println("\n--- EXPORTAR PRODUCTOS (1. JSON | 2. XML | 0. Volver) ---");
        System.out.print("Opción: ");
        try {
            int op = Integer.parseInt(LectorConsola.leerTexto());
            if (op == 1 || op == 2) {
                System.out.print("Ruta de destino: ");
                String ruta = LectorConsola.leerTexto();
                if (new File(ruta).exists() && !ConsolaColor.confirmar("El fichero ya existe. ¿Sobrescribir?")) return;
                if (op == 1) inventario.exportarJSON(ruta);
                else inventario.exportarXML(ruta);
                ConsolaColor.ok("Exportación realizada con éxito.");
            }
        } catch (Exception e) {
            ConsolaColor.error("Error al exportar: " + e.getMessage());
        }
    }

    private void menuSerializacion() {
        System.out.println("\n--- SERIALIZACIÓN (1. Guardar .dat | 2. Recuperar .dat | 0. Volver) ---");
        System.out.print("Opción: ");
        try {
            int op = Integer.parseInt(LectorConsola.leerTexto());
            if (op == 1) {
                System.out.print("Ruta de destino (.dat): ");
                String ruta = LectorConsola.leerTexto();
                if (new File(ruta).exists() && !ConsolaColor.confirmar("Fichero existente. ¿Sobrescribir?")) return;
                inventario.guardarSerializado(ruta);
                ConsolaColor.ok("Inventario guardado correctamente.");
            } else if (op == 2) {
                System.out.print("Ruta del fichero .dat: ");
                String ruta = LectorConsola.leerTexto();
                if (!inventario.getProductos().isEmpty()
                        && !ConsolaColor.confirmar("La memoria no está vacía. ¿Sustituirla por el .dat?")) return;
                inventario.recuperarSerializado(ruta);
                ConsolaColor.ok("Inventario recuperado correctamente.");
            }
        } catch (Exception e) {
            ConsolaColor.error("Error en la operación: " + e.getMessage());
        }
    }
}
