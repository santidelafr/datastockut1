package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

/**
 * Operaciones JDBC sobre la tabla categoria (RF-JDBC-02).
 */
public class CategoriaDAO {

    public boolean existeId(int id) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeId(Connection conn, int id) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeNombre(String nombre) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeNombreEnOtra(String nombre, int idActual) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE LOWER(nombre) = LOWER(?) AND id <> ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, idActual);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nombre"));
                }
                return null;
            }
        }
    }

    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM categoria ORDER BY id ASC";
        List<Categoria> lista = new ArrayList<>();
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public int insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoria.getId());
            ps.setString(2, categoria.getNombre().trim());
            return ps.executeUpdate();
        }
    }

    public int actualizarNombre(int id, String nuevoNombre) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre.trim());
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    public int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /**
     * Busca identificadores altos libres (a partir de un valor inicial) para
     * emplearlos de forma segura en la demostración de transacciones (RF-JDBC-08).
     */
    public List<Integer> buscarIdsLibres(Connection conn, int desdeId, int cantidad) throws SQLException {
        List<Integer> libres = new ArrayList<>();
        int candidato = desdeId;
        while (libres.size() < cantidad) {
            if (!existeId(conn, candidato)) {
                libres.add(candidato);
            }
            candidato++;
        }
        return libres;
    }
}
