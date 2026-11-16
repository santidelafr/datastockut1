package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.componente.ConsultaProductoJdbc;
import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;

/** Operaciones CRUD y consultas con JOIN sobre la tabla producto (UT2 / UT3). */
public class ProductoDAO {
    private final ConsultaProductoJdbc consultaProductoJdbc = new ConsultaProductoJdbc();

    private static final String SELECT_BASE =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, c.nombre AS categoria_nombre, " +
            "p.proveedor_id, pr.nombre AS proveedor_nombre FROM producto p " +
            "INNER JOIN categoria c ON p.categoria_id = c.id INNER JOIN proveedor pr ON p.proveedor_id = pr.id ";

    public int contarProductos(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM producto");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean existeCodigo(String codigo) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) { return existeCodigo(c, codigo); }
    }

    public boolean existeCodigo(Connection c, String codigo) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int insertar(Producto p) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) { return insertar(c, p); }
    }

    public int insertar(Connection c, Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo().trim().toUpperCase());
            ps.setString(2, p.getNombre().trim());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCategoriaId());
            ps.setInt(6, p.getProveedorId());
            return ps.executeUpdate();
        }
    }

    public int actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre().trim());
            ps.setBigDecimal(2, p.getPrecio());
            ps.setInt(3, p.getCategoriaId());
            ps.setInt(4, p.getProveedorId());
            ps.setString(5, p.getCodigo().trim().toUpperCase());
            return ps.executeUpdate();
        }
    }

    public int eliminar(String codigo) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo.trim().toUpperCase());
            return ps.executeUpdate();
        }
    }

    public ProductoDetalle buscarPorCodigo(String codigo) throws SQLException {
        return consultaProductoJdbc.buscarPorCodigo(codigo);
    }

    public List<ProductoDetalle> listarTodos() throws SQLException {
        List<ProductoDetalle> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "ORDER BY p.codigo ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<ProductoDetalle> buscarPorNombre(String fragmento) throws SQLException {
        List<ProductoDetalle> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE p.nombre LIKE ? ORDER BY p.codigo ASC")) {
            ps.setString(1, "%" + fragmento.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        }
        return lista;
    }

    public List<ProductoDetalle> buscarStockInferior(int umbral) throws SQLException {
        List<ProductoDetalle> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE p.stock < ? ORDER BY p.stock ASC, p.codigo ASC")) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        }
        return lista;
    }

    private ProductoDetalle mapear(ResultSet rs) throws SQLException {
        return new ProductoDetalle(
                rs.getString("codigo"), rs.getString("nombre"), rs.getBigDecimal("precio"), rs.getInt("stock"),
                rs.getInt("categoria_id"), rs.getString("categoria_nombre"),
                rs.getInt("proveedor_id"), rs.getString("proveedor_nombre"));
    }
}
