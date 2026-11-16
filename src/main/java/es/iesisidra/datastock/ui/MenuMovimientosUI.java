package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.MovimientoDAO;
import es.iesisidra.datastock.dao.ProductoDAO;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.ValidadorBD;

/** Submenús resumidos de Movimientos de Stock (7.6) e Historial (7.7). */
public class MenuMovimientosUI {
    private final ProductoDAO prodDAO = new ProductoDAO();
    private final MovimientoDAO movDAO = new MovimientoDAO();

    public void menuMovimientosStock() {
        int op;
        do {
            System.out.print("\n--- MOVIMIENTOS (1. ENTRADA | 2. SALIDA | 0. Volver): ");
            op = MenuMySQL.leerOpcionMenu();
            switch (op) {
                case 1 -> registrarMovimiento("ENTRADA");
                case 2 -> registrarMovimiento("SALIDA");
                case 0 -> {}
                default -> ConsolaColor.aviso("Opción no válida.");
            }
        } while (op != 0);
    }

    private void registrarMovimiento(String tipo) {
        System.out.print("Código del producto: ");
        String cod = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        try {
            ProductoDetalle act = prodDAO.buscarPorCodigo(cod);
            if (act == null) { ConsolaColor.error("El producto " + cod + " no existe en MySQL."); return; }
            System.out.println("Producto: " + act.getNombre() + " | Stock actual: " + act.getStock());

            Integer cant = MenuMySQL.pedirEnteroPositivo("Cantidad (> 0): ");
            if (cant == null || !ConsolaColor.confirmar("¿Registrar " + tipo + " de " + cant + " uds en " + cod + "?")) return;

            movDAO.registrarMovimiento(cod, tipo, cant);
            MovimientoStock ult = movDAO.obtenerUltimoMovimientoProducto(cod);
            ConsolaColor.ok("Movimiento registrado.");
            if (ult != null) {
                System.out.printf("Stock anterior: %d -> Stock posterior: %d%n", ult.getStockAnterior(), ult.getStockPosterior());
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    public void menuHistorial() {
        int op;
        do {
            System.out.print("\n--- HISTORIAL (1. Todos | 2. Por producto | 0. Volver): ");
            op = MenuMySQL.leerOpcionMenu();
            try {
                switch (op) {
                    case 1 -> imprimir(movDAO.listarTodos());
                    case 2 -> {
                        System.out.print("Código del producto: ");
                        String cod = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
                        if (!prodDAO.existeCodigo(cod)) ConsolaColor.error("El producto " + cod + " no existe.");
                        else imprimir(movDAO.listarPorProducto(cod));
                    }
                    case 0 -> {}
                    default -> ConsolaColor.aviso("Opción no válida.");
                }
            } catch (SQLException e) {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        } while (op != 0);
    }

    private void imprimir(List<MovimientoStock> lista) {
        if (lista.isEmpty()) ConsolaColor.aviso("Sin movimientos registrados.");
        else { lista.forEach(System.out::println); System.out.println("Total: " + lista.size()); }
    }
}
