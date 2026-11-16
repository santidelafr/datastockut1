package es.iesisidra.datastock.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones previas en Java acordes al esquema MySQL de DataStock (UT2 / UT3).
 */
public class ValidadorBD {
    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Z0-9-]{1,12}$");
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final BigDecimal MAX_PRECIO = new BigDecimal("99999999.99");

    private ValidadorBD() {}

    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    public static String validarCodigoProducto(String cod) {
        if (cod == null || cod.isEmpty()) return "El código del producto es obligatorio.";
        if (cod.length() > 12 || !PATRON_CODIGO.matcher(cod).matches()) {
            return "El código debe tener entre 1 y 12 caracteres (A-Z, 0-9 o guion).";
        }
        return null;
    }

    public static String validarNombreProducto(String nombre) {
        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 100) {
            return "El nombre del producto debe tener entre 3 y 100 caracteres.";
        }
        return null;
    }

    public static BigDecimal parsearPrecio(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        String limpio = entrada.trim().contains(".") ? entrada.trim() : entrada.trim().replace(',', '.');
        try {
            BigDecimal precio = new BigDecimal(limpio);
            String err = validarPrecio(precio);
            if (err != null) throw new IllegalArgumentException(err);
            return precio;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio no es un número decimal válido.");
        }
    }

    public static String validarPrecio(BigDecimal p) {
        if (p == null || p.compareTo(BigDecimal.ZERO) <= 0) return "El precio debe ser mayor que 0.";
        if (p.scale() > 2 || p.compareTo(MAX_PRECIO) > 0) return "El precio supera el formato DECIMAL(10,2).";
        return null;
    }

    public static String validarStock(int stock) {
        return stock < 0 ? "El stock no puede ser negativo." : null;
    }

    public static String validarNombreCategoria(String n) {
        return (n == null || n.trim().isEmpty() || n.trim().length() > 60)
                ? "El nombre de categoría es obligatorio (máx. 60 caracteres)." : null;
    }

    public static String validarNombreProveedor(String n) {
        return (n == null || n.trim().isEmpty() || n.trim().length() > 100)
                ? "El nombre de proveedor es obligatorio (máx. 100 caracteres)." : null;
    }

    public static String validarEmailProveedor(String email) {
        if (email == null || email.trim().isEmpty()) return null;
        String e = email.trim();
        return (e.length() > 120 || !PATRON_EMAIL.matcher(e).matches())
                ? "Formato de email inválido (ej: usuario@dominio.com)." : null;
    }
}
