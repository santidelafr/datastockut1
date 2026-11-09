package es.iesisidra.datastock.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.MovimientoStock;

/**
 * Invocación del procedimiento almacenado registrar_movimiento (RF-JDBC-05)
 * y consultas de solo lectura sobre movimiento_stock (RF-JDBC-06).
 */
public class MovimientoDAO {

    /**
     * Registra una ENTRADA o SALIDA invocando el procedimiento almacenado de MySQL.
     * No se envuelve en una transacción exterior para no interferir con el
     * START TRANSACTION / COMMIT / ROLLBACK interno del procedimiento.
     */
    public void registrarMovimiento(String codigoProducto, String tipo, int cantidad) throws SQLException {
        String sql = "{CALL registrar_movimiento(?, ?, ?)}";
        try (Connection conn = ConexionBD.obtenerConexion();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, codigoProducto.trim().toUpperCase());
            cs.setString(2, tipo.trim().toUpperCase());
            cs.setInt(3, cantidad);
            cs.execute();
        }
    }

    public MovimientoStock obtenerUltimoMovimientoProducto(String codigoProducto) throws SQLException {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior " +
                     "FROM movimiento_stock WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoProducto.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearMovimiento(rs);
                }
                return null;
            }
        }
    }

    public List<MovimientoStock> listarTodos() throws SQLException {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior " +
                     "FROM movimiento_stock ORDER BY fecha ASC, id ASC";
        List<MovimientoStock> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearMovimiento(rs));
            }
        }
        return lista;
    }

    public List<MovimientoStock> listarPorProducto(String codigoProducto) throws SQLException {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior " +
                     "FROM movimiento_stock WHERE producto_codigo = ? ORDER BY fecha ASC, id ASC";
        List<MovimientoStock> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoProducto.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }
        }
        return lista;
    }

    private MovimientoStock mapearMovimiento(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha");
        return new MovimientoStock(
                rs.getLong("id"),
                rs.getString("producto_codigo"),
                rs.getString("tipo"),
                rs.getInt("cantidad"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getInt("stock_anterior"),
                rs.getInt("stock_posterior"));
    }
}
