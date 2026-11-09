package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Proveedor;

/**
 * Operaciones JDBC sobre la tabla proveedor (RF-JDBC-02).
 */
public class ProveedorDAO {

    public boolean existeId(int id) throws SQLException {
        String sql = "SELECT 1 FROM proveedor WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeId(Connection conn, int id) throws SQLException {
        String sql = "SELECT 1 FROM proveedor WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM proveedor WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeEmailEnOtro(String email, int idActual) throws SQLException {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM proveedor WHERE LOWER(email) = LOWER(?) AND id <> ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Proveedor(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("email"));
                }
                return null;
            }
        }
    }

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id ASC";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Proveedor(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email")));
            }
        }
        return lista;
    }

    public int insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, proveedor.getId());
            ps.setString(2, proveedor.getNombre().trim());
            if (proveedor.getEmail() == null || proveedor.getEmail().isBlank()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, proveedor.getEmail().trim());
            }
            return ps.executeUpdate();
        }
    }

    public int actualizar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNombre().trim());
            if (proveedor.getEmail() == null || proveedor.getEmail().isBlank()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, proveedor.getEmail().trim());
            }
            ps.setInt(3, proveedor.getId());
            return ps.executeUpdate();
        }
    }

    public int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
