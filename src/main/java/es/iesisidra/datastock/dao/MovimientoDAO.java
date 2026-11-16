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

/** Invoca el procedimiento almacenado registrar_movimiento (7.6) y consulta el historial (7.7). */
public class MovimientoDAO {
    private static final String SELECT_MOV =
            "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior FROM movimiento_stock ";

    public void registrarMovimiento(String codigoProducto, String tipo, int cantidad) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             CallableStatement cs = c.prepareCall("{CALL registrar_movimiento(?, ?, ?)}")) {
            cs.setString(1, codigoProducto.trim().toUpperCase());
            cs.setString(2, tipo.trim().toUpperCase());
            cs.setInt(3, cantidad);
            cs.execute();
        }
    }

    public MovimientoStock obtenerUltimoMovimientoProducto(String codigoProducto) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(SELECT_MOV + "WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1")) {
            ps.setString(1, codigoProducto.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        }
    }

    public List<MovimientoStock> listarTodos() throws SQLException {
        List<MovimientoStock> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(SELECT_MOV + "ORDER BY fecha ASC, id ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<MovimientoStock> listarPorProducto(String codigoProducto) throws SQLException {
        List<MovimientoStock> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(SELECT_MOV + "WHERE producto_codigo = ? ORDER BY fecha ASC, id ASC")) {
            ps.setString(1, codigoProducto.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) lista.add(mapear(rs)); }
        }
        return lista;
    }

    private MovimientoStock mapear(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha");
        return new MovimientoStock(
                rs.getLong("id"), rs.getString("producto_codigo"), rs.getString("tipo"),
                rs.getInt("cantidad"), ts != null ? ts.toLocalDateTime() : null,
                rs.getInt("stock_anterior"), rs.getInt("stock_posterior"));
    }
}
