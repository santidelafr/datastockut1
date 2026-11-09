package es.iesisidra.datastock.servicio;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.ProductoDAO;
import es.iesisidra.datastock.util.ConsolaColor;

/**
 * Servicio para comprobar la conexión y el esquema de la base de datos MySQL (UT2).
 */
public class ServicioTransaccionesBD {
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
}
