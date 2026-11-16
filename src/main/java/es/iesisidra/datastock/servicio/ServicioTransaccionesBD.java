package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDAO;
import es.iesisidra.datastock.dao.ProductoDAO;
import es.iesisidra.datastock.dao.ProveedorDAO;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.ValidadorBD;

/**
 * Servicio que agrupa la comprobación del esquema (7.1), la importación transaccional
 * desde memoria a MySQL (7.8) y la demostración de commit/rollback (7.9).
 */
public class ServicioTransaccionesBD {
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    // 7.1: Conectar y comprobar el esquema
    public void comprobarConexionYEsquema() {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String t : List.of("categoria", "proveedor", "movimiento_stock")) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + t);
                     ResultSet rs = ps.executeQuery()) { rs.next(); }
            }
            int total = productoDAO.contarProductos(conn);
            ConsolaColor.ok("Conexión establecida correctamente.");
            System.out.println("Base de datos: " + (conn.getCatalog() != null ? conn.getCatalog() : "datastock"));
            System.out.println("Gestor: " + meta.getDatabaseProductName() + " | Versión: " + meta.getDatabaseProductVersion());
            System.out.println("Productos almacenados: " + total);
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    // 7.8: Importación por lote con setAutoCommit(false), commit y rollback
    public void importarProductosDeMemoria(List<Producto> enMemoria) {
        System.out.println("\nIMPORTAR PRODUCTOS DE MEMORIA A MYSQL");
        System.out.println("Productos disponibles en memoria: " + enMemoria.size());
        if (enMemoria.isEmpty()) {
            ConsolaColor.aviso("No hay productos cargados en memoria para importar.");
            return;
        }
        if (!ConsolaColor.confirmar("¿Deseas importar estos productos a MySQL?")) return;

        try (Connection conn = ConexionBD.obtenerConexion()) {
            int omitidos = 0, rechazados = 0, insertados = 0;
            List<Producto> aceptados = new ArrayList<>();
            Set<String> codigosLote = new HashSet<>();

            for (Producto orig : enMemoria) {
                String cod = ValidadorBD.normalizarCodigo(orig.getCodigo());
                String nom = orig.getNombre() != null ? orig.getNombre().trim() : "";
                System.out.println("Comprobando " + cod + " - " + nom + "...");

                if (productoDAO.existeCodigo(conn, cod) || codigosLote.contains(cod)) {
                    ConsolaColor.aviso("El producto ya existe en MySQL. Se omite.");
                    omitidos++;
                    continue;
                }
                String err = validarParaBD(conn, cod, nom, orig);
                if (err != null) {
                    ConsolaColor.error(err + " El producto no se puede importar.");
                    rechazados++;
                    continue;
                }
                aceptados.add(new Producto(cod, nom, orig.getPrecio(), orig.getStock(), orig.getCategoriaId(), orig.getProveedorId()));
                codigosLote.add(cod);
                ConsolaColor.ok("Producto válido para importar.");
            }

            if (!aceptados.isEmpty()) {
                boolean autoOriginal = conn.getAutoCommit();
                try {
                    conn.setAutoCommit(false);
                    for (Producto p : aceptados) insertados += productoDAO.insertar(conn, p);
                    conn.commit();
                } catch (SQLException ex) {
                    insertados = 0;
                    conn.rollback();
                    ConsolaColor.error("Fallo en lote, ejecutado ROLLBACK: " + ConexionBD.traducirErrorSQL(ex));
                } finally {
                    conn.setAutoCommit(autoOriginal);
                }
            }

            System.out.printf("%nRESULTADO -> Analizados: %d | Insertados: %d | Omitidos: %d | Rechazados: %d%n",
                    enMemoria.size(), insertados, omitidos, rechazados);
            ConsolaColor.ok("Importación finalizada.");
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private String validarParaBD(Connection conn, String cod, String nom, Producto p) throws SQLException {
        if (ValidadorBD.validarCodigoProducto(cod) != null) return ValidadorBD.validarCodigoProducto(cod);
        if (ValidadorBD.validarNombreProducto(nom) != null) return ValidadorBD.validarNombreProducto(nom);
        if (ValidadorBD.validarPrecio(p.getPrecio()) != null) return ValidadorBD.validarPrecio(p.getPrecio());
        if (ValidadorBD.validarStock(p.getStock()) != null) return ValidadorBD.validarStock(p.getStock());
        if (p.getCategoriaId() <= 0 || !categoriaDAO.existeId(conn, p.getCategoriaId()))
            return "No existe la categoría con ID " + p.getCategoriaId() + ".";
        if (p.getProveedorId() <= 0 || !proveedorDAO.existeId(conn, p.getProveedorId()))
            return "No existe el proveedor con ID " + p.getProveedorId() + ".";
        return null;
    }

    // 7.9: Demostración explícita de COMMIT (Escenario A) y ROLLBACK (Escenario B)
    public void ejecutarDemostracionTransacciones() {
        System.out.println("\nDEMOSTRACIÓN DE TRANSACCIONES JDBC (COMMIT / ROLLBACK)");
        try (Connection conn = ConexionBD.obtenerConexion()) {
            List<Integer> ids = categoriaDAO.buscarIdsLibres(conn, 9001, 3);
            int idA1 = ids.get(0), idA2 = ids.get(1), idB1 = ids.get(2);
            if (!ConsolaColor.confirmar("¿Iniciar demostración con IDs libres " + idA1 + ", " + idA2 + " y " + idB1 + "?")) return;

            boolean autoOriginal = conn.getAutoCommit();
            String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
            try {
                // Escenario A: COMMIT
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, idA1); ps.setString(2, "Demo Commit " + idA1); ps.executeUpdate();
                    ps.setInt(1, idA2); ps.setString(2, "Demo Commit " + idA2); ps.executeUpdate();
                }
                conn.commit();
                ConsolaColor.ok("Escenario A (COMMIT) verificado: existen categorías " + idA1 + " y " + idA2 + ".");

                // Escenario B: ROLLBACK ante clave primaria duplicada
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, idB1); ps.setString(2, "Demo Rollback " + idB1); ps.executeUpdate();
                    ps.setInt(1, idB1); ps.setString(2, "Demo Duplicada " + idB1); ps.executeUpdate();
                    conn.commit();
                } catch (SQLException ex) {
                    ConsolaColor.error("Error esperado capturado (código " + ex.getErrorCode() + ": clave duplicada).");
                    conn.rollback();
                    if (!categoriaDAO.existeId(conn, idB1)) {
                        ConsolaColor.ok("Escenario B (ROLLBACK) verificado: la categoría " + idB1 + " no persiste en MySQL.");
                    }
                }
            } finally {
                conn.setAutoCommit(autoOriginal);
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }
}
