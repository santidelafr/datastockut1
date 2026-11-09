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
 * Servicio que agrupa la comprobación del esquema (RF-JDBC-01),
 * la importación transaccional desde el inventario en memoria (RF-JDBC-07)
 * y la demostración explícita de commit/rollback en JDBC (RF-JDBC-08).
 */
public class ServicioTransaccionesBD {
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    // RF-JDBC-01: Conectar y comprobar el esquema
    public void comprobarConexionYEsquema() {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = conn.getMetaData();
            String catalogo = conn.getCatalog();

            // Verificamos el acceso a las 4 tablas del esquema obligatorio
            verificarTabla(conn, "categoria");
            verificarTabla(conn, "proveedor");
            verificarTabla(conn, "movimiento_stock");
            int totalProductos = productoDAO.contarProductos(conn);

            ConsolaColor.ok("Conexión establecida correctamente.");
            System.out.println("Base de datos: " + (catalogo != null ? catalogo : "datastock"));
            System.out.println("Gestor: " + meta.getDatabaseProductName());
            System.out.println("Versión: " + meta.getDatabaseProductVersion());
            System.out.println("Productos almacenados: " + totalProductos);
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void verificarTabla(Connection conn, String tabla) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabla;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
        }
    }

    // RF-JDBC-07: Importación controlada desde la colección en memoria de la UT1
    public void importarProductosDeMemoria(List<Producto> productosEnMemoria) {
        System.out.println("\nIMPORTAR PRODUCTOS DE MEMORIA A MYSQL");
        System.out.println("------------------------------------------------");
        System.out.println("Productos disponibles en memoria: " + productosEnMemoria.size());

        if (productosEnMemoria.isEmpty()) {
            ConsolaColor.aviso("No hay productos cargados en memoria para importar.");
            return;
        }

        if (!ConsolaColor.confirmar("¿Deseas importar estos productos a MySQL?")) {
            System.out.println("Operación cancelada.");
            return;
        }

        System.out.println("Iniciando comprobación de productos...\n");

        try (Connection conn = ConexionBD.obtenerConexion()) {
            int analizados = productosEnMemoria.size();
            int omitidos = 0;
            int rechazados = 0;
            List<Producto> aceptados = new ArrayList<>();
            Set<String> codigosEnLote = new HashSet<>();

            for (Producto orig : productosEnMemoria) {
                String codigoNorm = ValidadorBD.normalizarCodigo(orig.getCodigo());
                String nombreLim = orig.getNombre() != null ? orig.getNombre().trim() : "";
                System.out.println("Comprobando " + codigoNorm + " - " + nombreLim + "...");

                String errCodigo = ValidadorBD.validarCodigoProducto(codigoNorm);
                if (errCodigo != null) {
                    ConsolaColor.error(errCodigo + " El producto no se puede importar.");
                    rechazados++;
                    continue;
                }

                // Si ya existe en MySQL o está repetido en el lote, se omite sin sobrescribir
                if (productoDAO.existeCodigo(conn, codigoNorm) || codigosEnLote.contains(codigoNorm)) {
                    ConsolaColor.aviso("El producto ya existe en MySQL. Se omite.");
                    omitidos++;
                    continue;
                }

                String errNombre = ValidadorBD.validarNombreProducto(nombreLim);
                if (errNombre != null) {
                    ConsolaColor.error(errNombre + " El producto no se puede importar.");
                    rechazados++;
                    continue;
                }

                String errPrecio = ValidadorBD.validarPrecio(orig.getPrecio());
                if (errPrecio != null) {
                    ConsolaColor.error(errPrecio + " El producto no se puede importar.");
                    rechazados++;
                    continue;
                }

                String errStock = ValidadorBD.validarStock(orig.getStock());
                if (errStock != null) {
                    ConsolaColor.error(errStock + " El producto no se puede importar.");
                    rechazados++;
                    continue;
                }

                if (orig.getCategoriaId() <= 0 || !categoriaDAO.existeId(conn, orig.getCategoriaId())) {
                    ConsolaColor.error("No existe la categoría con ID " + orig.getCategoriaId() +
                            ". El producto no se puede importar.");
                    rechazados++;
                    continue;
                }

                if (orig.getProveedorId() <= 0 || !proveedorDAO.existeId(conn, orig.getProveedorId())) {
                    ConsolaColor.error("No existe el proveedor con ID " + orig.getProveedorId() +
                            ". El producto no se puede importar.");
                    rechazados++;
                    continue;
                }

                aceptados.add(new Producto(
                        codigoNorm,
                        nombreLim,
                        orig.getPrecio(),
                        orig.getStock(),
                        orig.getCategoriaId(),
                        orig.getProveedorId()));
                codigosEnLote.add(codigoNorm);
                ConsolaColor.ok("Producto válido para importar.");
            }

            int insertados = 0;
            boolean huboRollback = false;

            if (!aceptados.isEmpty()) {
                boolean autoCommitOriginal = conn.getAutoCommit();
                try {
                    conn.setAutoCommit(false);
                    for (Producto p : aceptados) {
                        insertados += productoDAO.insertar(conn, p);
                    }
                    conn.commit();
                } catch (SQLException exLote) {
                    huboRollback = true;
                    insertados = 0;
                    try {
                        conn.rollback();
                    } catch (SQLException exRb) {
                        ConsolaColor.error("Error adicional al ejecutar rollback.");
                    }
                    ConsolaColor.error("Fallo SQL inesperado durante el lote: " +
                            ConexionBD.traducirErrorSQL(exLote));
                    ConsolaColor.error("Se ha ejecutado ROLLBACK: no se ha confirmado ninguna inserción del lote.");
                } finally {
                    conn.setAutoCommit(autoCommitOriginal);
                }
            }

            System.out.println("\n------------------------------------------------");
            System.out.println("RESULTADO DE LA IMPORTACIÓN");
            System.out.println("------------------------------------------------");
            System.out.printf("Productos analizados:             %d%n", analizados);
            System.out.printf("Productos insertados:             %d%n", insertados);
            System.out.printf("Omitidos por código duplicado:    %d%n", omitidos);
            System.out.printf("Rechazados por datos incorrectos: %d%n", rechazados);
            System.out.println();

            if (!huboRollback) {
                ConsolaColor.ok("Importación finalizada.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    // RF-JDBC-08: Demostración de transacción con commit y rollback desde Java
    public void ejecutarDemostracionTransacciones() {
        System.out.println("\nDEMOSTRACIÓN DE TRANSACCIONES JDBC (COMMIT / ROLLBACK)");
        System.out.println("------------------------------------------------------");

        try (Connection conn = ConexionBD.obtenerConexion()) {
            List<Integer> ids = categoriaDAO.buscarIdsLibres(conn, 9001, 3);
            int idA1 = ids.get(0);
            int idA2 = ids.get(1);
            int idB1 = ids.get(2);

            String nombreA1 = "Demo Commit " + idA1;
            String nombreA2 = "Demo Commit " + idA2;
            String nombreB1 = "Demo Rollback " + idB1;

            System.out.println("Se van a emplear identificadores de categoría de prueba libres (sin productos asociados):");
            System.out.printf(" - Escenario A (COMMIT):   ID %d ('%s') e ID %d ('%s')%n", idA1, nombreA1, idA2, nombreA2);
            System.out.printf(" - Escenario B (ROLLBACK): ID %d ('%s') seguido de clave duplicada ID %d%n", idB1, nombreB1, idB1);

            if (!ConsolaColor.confirmar("¿Deseas iniciar la demostración de transacciones?")) {
                System.out.println("Demostración cancelada.");
                return;
            }

            boolean autoCommitOriginal = conn.getAutoCommit();
            String sqlInsert = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";

            try {
                // ESCENARIO A: COMMIT
                System.out.println("\n--- Escenario A: Transacción confirmada con COMMIT ---");
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setInt(1, idA1);
                    ps.setString(2, nombreA1);
                    ps.executeUpdate();

                    ps.setInt(1, idA2);
                    ps.setString(2, nombreA2);
                    ps.executeUpdate();
                }
                conn.commit();

                boolean existenAmbas = categoriaDAO.existeId(conn, idA1) && categoriaDAO.existeId(conn, idA2);
                if (existenAmbas) {
                    ConsolaColor.ok("COMMIT ejecutado y verificado con SELECT: existen las categorías " +
                            idA1 + " y " + idA2 + " en MySQL.");
                }

                // ESCENARIO B: ROLLBACK ANTE CLAVE DUPLICADA
                System.out.println("\n--- Escenario B: Error forzado y reversión con ROLLBACK ---");
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    // Primera inserción válida dentro de la nueva transacción
                    ps.setInt(1, idB1);
                    ps.setString(2, nombreB1);
                    ps.executeUpdate();
                    System.out.println("1ª inserción temporal realizada (ID " + idB1 + ", pendiente de confirmar).");

                    // Segunda inserción intencionadamente duplicada (mismo ID)
                    System.out.println("2ª inserción provocando clave primaria duplicada (ID " + idB1 + ")...");
                    ps.setInt(1, idB1);
                    ps.setString(2, "Demo Duplicada " + idB1);
                    ps.executeUpdate();

                    conn.commit();
                } catch (SQLException exEsperada) {
                    ConsolaColor.error("Error SQL esperado capturado (código " + exEsperada.getErrorCode() +
                            "): clave primaria duplicada.");
                    conn.rollback();

                    boolean persisteB1 = categoriaDAO.existeId(conn, idB1);
                    if (!persisteB1) {
                        ConsolaColor.ok("ROLLBACK ejecutado correctamente: la categoría " + idB1 +
                                " del intento fallido NO permanece almacenada en MySQL.");
                    } else {
                        ConsolaColor.error("La categoría " + idB1 + " seguía presente tras el rollback.");
                    }
                }
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
                System.out.println("\nEstado de autoCommit restaurado a: " + conn.getAutoCommit());
            }

            System.out.println("------------------------------------------------------");
            System.out.println("Documentación de limpieza de datos de demostración:");
            System.out.printf(" - IDs confirmados en Escenario A: %d, %d (ID revertido en Escenario B: %d)%n",
                    idA1, idA2, idB1);
            System.out.println(" - Puedes eliminarlos desde el menú 7 -> 2 -> 4 (Eliminar categoría) o en SQL con:");
            System.out.printf("   DELETE FROM categoria WHERE id IN (%d, %d) AND id NOT IN (SELECT DISTINCT categoria_id FROM producto);%n",
                    idA1, idA2);

        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }
}
