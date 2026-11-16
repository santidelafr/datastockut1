package es.iesisidra.datastock.componente;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Componente reutilizable de lectura de productos desde ficheros JSON (UT3 - RA6).
 * No depende de la consola ni modifica por sí mismo el inventario ni la base de datos.
 */
public class LectorProductosJson {
    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Z0-9-]+$", Pattern.CASE_INSENSITIVE);

    /** Encapsula los productos válidos leídos, el total de elementos y las incidencias detectadas. */
    public static class ResultadoLectura {
        private final List<Producto> productosValidos;
        private final int totalEncontrados;
        private final List<String> incidencias;

        public ResultadoLectura(List<Producto> productosValidos, int totalEncontrados, List<String> incidencias) {
            this.productosValidos = Collections.unmodifiableList(new ArrayList<>(productosValidos));
            this.totalEncontrados = totalEncontrados;
            this.incidencias = Collections.unmodifiableList(new ArrayList<>(incidencias));
        }

        public List<Producto> getProductosValidos() { return productosValidos; }
        public int getTotalEncontrados() { return totalEncontrados; }
        public List<String> getIncidencias() { return incidencias; }
    }

    /**
     * Valida las reglas de negocio de un {@link Producto}.
     *
     * @param p producto a validar
     * @return mensaje de error si es inválido, o {@code null} si es correcto
     */
    public String validarProducto(Producto p) {
        if (p == null || p.getCodigo() == null || p.getCodigo().trim().isEmpty()) return "El código es obligatorio.";
        String cod = p.getCodigo().trim();
        if (cod.length() > 12 || !PATRON_CODIGO.matcher(cod).matches()) return "Código inválido (máx 12 caracteres A-Z, 0-9 o guion).";
        if (p.getNombre() == null || p.getNombre().trim().length() < 3 || p.getNombre().trim().length() > 100) return "El nombre debe tener entre 3 y 100 caracteres.";
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) return "El precio debe ser mayor que 0.";
        if (p.getStock() < 0) return "El stock no puede ser negativo.";
        if (p.getCategoriaId() <= 0) return "El ID de categoría debe ser positivo.";
        if (p.getProveedorId() <= 0) return "El ID de proveedor debe ser positivo.";
        return null;
    }
}
