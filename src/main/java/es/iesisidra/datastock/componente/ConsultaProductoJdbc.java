package es.iesisidra.datastock.componente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.util.ValidadorBD;

/**
 * Componente reutilizable de consulta de productos por código en MySQL mediante JDBC (UT3 - RA6).
 * Utiliza {@link PreparedStatement} y {@link ResultSet}, cierra recursos con {@code try-with-resources}
 * y no escribe mensajes por consola.
 */
public class ConsultaProductoJdbc {

    private static final String SQL_BUSCAR_POR_CODIGO =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, " +
            "p.categoria_id, c.nombre AS categoria_nombre, " +
            "p.proveedor_id, pr.nombre AS proveedor_nombre " +
            "FROM producto p " +
            "INNER JOIN categoria c ON p.categoria_id = c.id " +
            "INNER JOIN proveedor pr ON p.proveedor_id = pr.id " +
            "WHERE p.codigo = ?";

    /**
     * Consulta un producto en MySQL por su código devolviendo su ficha completa con categoría y proveedor.
     *
     * @param codigo código del producto a buscar
     * @return {@link ProductoDetalle} si existe en MySQL, o {@code null} si no existe
     * @throws IllegalArgumentException si el código es vacío o tiene un formato inválido
     * @throws SQLException             si ocurre un error de conexión o consulta en MySQL
     */
    public ProductoDetalle buscarPorCodigo(String codigo) throws SQLException {
        String codNorm = ValidadorBD.normalizarCodigo(codigo);
        String error = ValidadorBD.validarCodigoProducto(codNorm);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_CODIGO)) {
            ps.setString(1, codNorm);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ProductoDetalle(
                        rs.getString("codigo"), rs.getString("nombre"),
                        rs.getBigDecimal("precio"), rs.getInt("stock"),
                        rs.getInt("categoria_id"), rs.getString("categoria_nombre"),
                        rs.getInt("proveedor_id"), rs.getString("proveedor_nombre"));
            }
        }
    }

    /**
     * Consulta un producto en MySQL por su código devolviendo un objeto {@link Producto}.
     *
     * @param codigo código del producto
     * @return {@link Producto} si existe en MySQL, o {@code null} si no existe
     * @throws IllegalArgumentException si el código tiene formato inválido
     * @throws SQLException             si ocurre un error JDBC
     */
    public Producto buscarProducto(String codigo) throws SQLException {
        ProductoDetalle d = buscarPorCodigo(codigo);
        return d == null ? null : new Producto(
                d.getCodigo(), d.getNombre(), d.getPrecio(), d.getStock(), d.getCategoriaId(), d.getProveedorId());
    }
}
