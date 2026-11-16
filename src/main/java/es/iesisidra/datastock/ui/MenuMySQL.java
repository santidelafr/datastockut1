package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ServicioTransaccionesBD;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Menú principal de gestión MySQL (opción 7).
 * Coordina las 9 opciones delegando en submenús especializados para facilitar su localización.
 */
public class MenuMySQL {
    private final Inventario inventarioMemoria;
    private final MenuCategoriasProveedoresUI menuCatProv = new MenuCategoriasProveedoresUI();
    private final MenuProductosUI menuProductos = new MenuProductosUI();
    private final MenuMovimientosUI menuMovimientos = new MenuMovimientosUI();
    private final ServicioTransaccionesBD servicioTransacciones = new ServicioTransaccionesBD();

    public MenuMySQL(Inventario inventarioMemoria) {
        this.inventarioMemoria = inventarioMemoria;
    }

    public void iniciar() {
        int opcion = -1;
        do {
            System.out.println("\n================================================");
            System.out.println("              GESTIÓN MYSQL - UT2               ");
            System.out.println("================================================");
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona una opción: ");

            opcion = leerOpcionMenu();
            switch (opcion) {
                case 1 -> servicioTransacciones.comprobarConexionYEsquema();
                case 2 -> menuCatProv.menuCategorias();
                case 3 -> menuCatProv.menuProveedores();
                case 4 -> menuProductos.menuProductosCRUD();
                case 5 -> menuProductos.menuConsultasProductos();
                case 6 -> menuMovimientos.menuMovimientosStock();
                case 7 -> menuMovimientos.menuHistorial();
                case 8 -> servicioTransacciones.importarProductosDeMemoria(inventarioMemoria.getProductos());
                case 9 -> servicioTransacciones.ejecutarDemostracionTransacciones();
                case 0 -> {}
                default -> ConsolaColor.aviso("Opción no válida. Selecciona una opción entre 0 y 9.");
            }
        } while (opcion != 0);
    }

    public static int leerOpcionMenu() {
        try {
            return Integer.parseInt(LectorConsola.leerTexto());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Integer pedirEnteroPositivo(String etiqueta) {
        System.out.print(etiqueta);
        try {
            int valor = Integer.parseInt(LectorConsola.leerTexto());
            if (valor <= 0) {
                ConsolaColor.error("El valor debe ser un número entero positivo (> 0).");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            ConsolaColor.error("Debes introducir un número entero válido.");
            return null;
        }
    }

    public static Integer pedirEnteroNoNegativo(String etiqueta) {
        System.out.print(etiqueta);
        try {
            int valor = Integer.parseInt(LectorConsola.leerTexto());
            if (valor < 0) {
                ConsolaColor.error("El valor no puede ser negativo (>= 0).");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            ConsolaColor.error("Debes introducir un número entero válido.");
            return null;
        }
    }
}
