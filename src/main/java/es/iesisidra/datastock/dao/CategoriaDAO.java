package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

/** Operaciones JDBC sobre la tabla categoria (RF-JDBC-02). */
public class CategoriaDAO {

    public boolean existeId(int id) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion()) { return existeId(c, id); }
    }

    public boolean existeId(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM categoria WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existeNombre(String nombre) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?)")) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existeNombreEnOtra(String nombre, int idActual) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?) AND id <> ?")) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public Categoria buscarPorId(int id) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT id, nombre FROM categoria WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Categoria(rs.getInt("id"), rs.getString("nombre")) : null;
            }
        }
    }

    public List<Categoria> listarTodas() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("SELECT id, nombre FROM categoria ORDER BY id ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
        }
        return lista;
    }

    public int insertar(Categoria cat) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("INSERT INTO categoria (id, nombre) VALUES (?, ?)")) {
            ps.setInt(1, cat.getId());
            ps.setString(2, cat.getNombre().trim());
            return ps.executeUpdate();
        }
    }

    public int actualizarNombre(int id, String nuevoNombre) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("UPDATE categoria SET nombre = ? WHERE id = ?")) {
            ps.setString(1, nuevoNombre.trim());
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    public int eliminar(int id) throws SQLException {
        try (Connection c = ConexionBD.obtenerConexion();
             PreparedStatement ps = c.prepareStatement("DELETE FROM categoria WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    public List<Integer> buscarIdsLibres(Connection c, int desdeId, int cantidad) throws SQLException {
        List<Integer> libres = new ArrayList<>();
        for (int id = desdeId; libres.size() < cantidad; id++) {
            if (!existeId(c, id)) libres.add(id);
        }
        return libres;
    }
}
