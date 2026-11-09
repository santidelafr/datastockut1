package es.iesisidra.datastock.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones previas en Java acordes a las restricciones definidas
 * en el esquema MySQL de DataStock (UT2 - apartado 3.1).
 */
public class ValidadorBD {
    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Z0-9-]{1,12}$");
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final BigDecimal MAX_PRECIO = new BigDecimal("99999999.99");

    private ValidadorBD() {}

    public static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.trim().toUpperCase();
    }

    public static String validarCodigoProducto(String codigoNormalizado) {
        if (codigoNormalizado == null || codigoNormalizado.isEmpty()) {
            return "El código del producto es obligatorio.";
        }
        if (codigoNormalizado.length() > 12) {
            return "El código supera la longitud máxima permitida (12 caracteres).";
        }
        if (!PATRON_CODIGO.matcher(codigoNormalizado).matches()) {
            return "El código solo puede contener letras mayúsculas (A-Z), números (0-9) y guiones (-).";
        }
        return null;
    }

    public static String validarNombreProducto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del producto no puede estar vacío.";
        }
        int len = nombre.trim().length();
        if (len < 3 || len > 100) {
            return "El nombre del producto debe tener entre 3 y 100 caracteres.";
        }
        return null;
    }

    public static BigDecimal parsearPrecio(String entrada) {
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        String limpio = entrada.trim();
        if (!limpio.contains(".")) {
            limpio = limpio.replace(',', '.');
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio introducido no es un número decimal válido.");
        }
        String error = validarPrecio(precio);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        return precio;
    }

    public static String validarPrecio(BigDecimal precio) {
        if (precio == null) {
            return "El precio es obligatorio.";
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio debe ser mayor que 0.";
        }
        if (precio.scale() > 2) {
            return "El precio admite como máximo dos cifras decimales.";
        }
        if (precio.compareTo(MAX_PRECIO) > 0) {
            return "El precio supera el rango máximo permitido por DECIMAL(10,2).";
        }
        return null;
    }

    public static String validarStock(int stock) {
        if (stock < 0) {
            return "El stock no puede ser negativo.";
        }
        return null;
    }

    public static String validarNombreCategoria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre de la categoría es obligatorio.";
        }
        if (nombre.trim().length() > 60) {
            return "El nombre de la categoría no puede superar los 60 caracteres.";
        }
        return null;
    }

    public static String validarNombreProveedor(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del proveedor es obligatorio.";
        }
        if (nombre.trim().length() > 100) {
            return "El nombre del proveedor no puede superar los 100 caracteres.";
        }
        return null;
    }

    public static String validarEmailProveedor(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null; // El correo es opcional
        }
        String limpio = email.trim();
        if (limpio.length() > 120) {
            return "El correo electrónico no puede superar los 120 caracteres.";
        }
        if (!PATRON_EMAIL.matcher(limpio).matches()) {
            return "El correo electrónico no tiene un formato válido (ejemplo: usuario@dominio.com).";
        }
        return null;
    }
}
