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

/** Operaciones JDBC sobre la tabla proveedor (RF-JDBC-02). */
public class ProveedorDAO {

    public boolean existeId(int id) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) { return existeId(c, id); }
    }

    public boolean existeId(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM proveedor WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existeEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) return false;
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM proveedor WHERE LOWER(email) = LOWER(?)")) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existeEmailEnOtro(String email, int idActual) throws SQLException {
        if (email == null || email.isBlank()) return false;
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM proveedor WHERE LOWER(email) = LOWER(?) AND id <> ?")) {
            ps.setString(1, email.trim());
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Proveedor buscarPorId(int id) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT id, nombre, email FROM proveedor WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("email")) : null;
            }
        }
    }

    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT id, nombre, email FROM proveedor ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("email")));
        }
        return lista;
    }

    public int insertar(Proveedor p) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)")) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre().trim());
            if (p.getEmail() == null || p.getEmail().isBlank()) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, p.getEmail().trim());
            return ps.executeUpdate();
        }
    }

    public int actualizar(Proveedor p) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?")) {
            ps.setString(1, p.getNombre().trim());
            if (p.getEmail() == null || p.getEmail().isBlank()) ps.setNull(2, Types.VARCHAR);
            else ps.setString(2, p.getEmail().trim());
            ps.setInt(3, p.getId());
            return ps.executeUpdate();
        }
    }

    public int eliminar(int id) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM proveedor WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
