package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;

/**
 * Operaciones CRUD y consultas con JOIN sobre la tabla producto
 * (RF-JDBC-03, RF-JDBC-04 y RF-JDBC-07).
 */
public class ProductoDAO {

    private static final String SELECT_DETALLE_BASE =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
            "p.categoria_id, c.nombre AS categoria_nombre, " +
            "p.proveedor_id, pr.nombre AS proveedor_nombre " +
            "FROM producto p " +
            "INNER JOIN categoria c ON p.categoria_id = c.id " +
            "INNER JOIN proveedor pr ON p.proveedor_id = pr.id ";

    public int contarProductos(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean existeCodigo(String codigo) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            return existeCodigo(conn, codigo);
        }
    }

    public boolean existeCodigo(Connection conn, String codigo) throws SQLException {
        String sql = "SELECT 1 FROM producto WHERE codigo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertar(Producto producto) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            return insertar(conn, producto);
        }
    }

    public int insertar(Connection conn, Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCodigo().trim().toUpperCase());
            ps.setString(2, producto.getNombre().trim());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setInt(5, producto.getCategoriaId());
            ps.setInt(6, producto.getProveedorId());
            return ps.executeUpdate();
        }
    }

    /**
     * Modifica únicamente nombre, precio, categoría y proveedor.
     * El código (PK) no se cambia y el stock solo se altera mediante movimientos.
     */
    public int actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? " +
                     "WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre().trim());
            ps.setBigDecimal(2, producto.getPrecio());
            ps.setInt(3, producto.getCategoriaId());
            ps.setInt(4, producto.getProveedorId());
            ps.setString(5, producto.getCodigo().trim().toUpperCase());
            return ps.executeUpdate();
        }
    }

    public int eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            return ps.executeUpdate();
        }
    }

    public ProductoDetalle buscarPorCodigo(String codigo) throws SQLException {
        String sql = SELECT_DETALLE_BASE + "WHERE p.codigo = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearDetalle(rs);
                }
                return null;
            }
        }
    }

    public List<ProductoDetalle> listarTodos() throws SQLException {
        String sql = SELECT_DETALLE_BASE + "ORDER BY p.codigo ASC";
        List<ProductoDetalle> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearDetalle(rs));
            }
        }
        return lista;
    }

    public List<ProductoDetalle> buscarPorNombre(String fragmento) throws SQLException {
        String sql = SELECT_DETALLE_BASE + "WHERE p.nombre LIKE ? ORDER BY p.codigo ASC";
        List<ProductoDetalle> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + fragmento.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDetalle(rs));
                }
            }
        }
        return lista;
    }

    public List<ProductoDetalle> buscarStockInferior(int umbral) throws SQLException {
        String sql = SELECT_DETALLE_BASE + "WHERE p.stock < ? ORDER BY p.stock ASC, p.codigo ASC";
        List<ProductoDetalle> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearDetalle(rs));
                }
            }
        }
        return lista;
    }

    private ProductoDetalle mapearDetalle(ResultSet rs) throws SQLException {
        return new ProductoDetalle(
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getInt("categoria_id"),
                rs.getString("categoria_nombre"),
                rs.getInt("proveedor_id"),
                rs.getString("proveedor_nombre"));
    }
}
