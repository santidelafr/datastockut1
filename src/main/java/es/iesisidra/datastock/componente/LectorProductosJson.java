package es.iesisidra.datastock.componente;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

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
     * Lee un fichero JSON desde una ruta en texto y devuelve la lista de productos válidos.
     *
     * @param rutaFichero ruta del archivo JSON
     * @return lista de objetos {@link Producto} válidos
     * @throws IOException si el fichero no existe o no se puede leer
     */
    public List<Producto> leerProductos(String rutaFichero) throws IOException {
        return leerConDetalle(rutaFichero).getProductosValidos();
    }

    /**
     * Lee un fichero JSON desde un {@link Path} y devuelve la lista de productos válidos.
     *
     * @param rutaFichero ruta {@link Path} del archivo JSON
     * @return lista de objetos {@link Producto} válidos
     * @throws IOException si el fichero no existe o falla su lectura
     */
    public List<Producto> leerProductos(Path rutaFichero) throws IOException {
        return leerConDetalle(rutaFichero).getProductosValidos();
    }

    /**
     * Lee y valida un fichero JSON a partir de su ruta en texto devolviendo productos e incidencias.
     *
     * @param rutaFichero ruta en texto del fichero JSON
     * @return {@link ResultadoLectura} con productos válidos, total e incidencias
     * @throws IOException si el fichero no existe o no es accesible
     */
    public ResultadoLectura leerConDetalle(String rutaFichero) throws IOException {
        if (rutaFichero == null || rutaFichero.trim().isEmpty()) {
            throw new FileNotFoundException("La ruta del fichero JSON no puede estar vacía.");
        }
        return leerConDetalle(Paths.get(rutaFichero.trim()));
    }

    /**
     * Lee y valida un fichero JSON desde un {@link Path} sin escribir en consola.
     *
     * @param rutaFichero ruta {@link Path} del fichero JSON
     * @return {@link ResultadoLectura} con los productos válidos y las incidencias
     * @throws FileNotFoundException    si el fichero no existe o no se puede leer
     * @throws IllegalArgumentException si el JSON está mal formado o no tiene la clave {@code "productos"}
     * @throws IOException              si ocurre un error de entrada/salida
     */
    public ResultadoLectura leerConDetalle(Path rutaFichero) throws IOException {
        if (rutaFichero == null || !Files.exists(rutaFichero) || !Files.isRegularFile(rutaFichero) || !Files.isReadable(rutaFichero)) {
            throw new FileNotFoundException("El fichero no existe o no se puede leer.");
        }

        try (Reader reader = Files.newBufferedReader(rutaFichero, StandardCharsets.UTF_8)) {
            JsonElement element;
            try {
                element = JsonParser.parseReader(reader);
            } catch (JsonSyntaxException e) {
                throw new IllegalArgumentException("Sintaxis JSON incorrecta: " + e.getMessage(), e);
            }

            if (element == null || !element.isJsonObject() || !element.getAsJsonObject().has("productos")
                    || !element.getAsJsonObject().get("productos").isJsonArray()) {
                throw new IllegalArgumentException("Estructura JSON inválida: no contiene el objeto raíz 'productos'.");
            }

            JsonArray array = element.getAsJsonObject().getAsJsonArray("productos");
            List<Producto> validos = new ArrayList<>();
            List<String> incidencias = new ArrayList<>();
            Set<String> codigosEnFichero = new HashSet<>();

            for (int i = 0; i < array.size(); i++) {
                try {
                    JsonObject o = array.get(i).getAsJsonObject();
                    String codigo = str(o, "codigo");
                    String nombre = str(o, "nombre");
                    BigDecimal precio = o.has("precio") && !o.get("precio").isJsonNull() ? o.get("precio").getAsBigDecimal() : null;
                    int stock = entero(o, "stock");
                    int catId = entero(o, "categoriaId");
                    int provId = entero(o, "proveedorId");

                    Producto p = new Producto(codigo, nombre, precio, stock, catId, provId);
                    String error = validarProducto(p);

                    if (error != null) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): " + error);
                    } else if (!codigosEnFichero.add(codigo.toUpperCase())) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): código duplicado en el fichero.");
                    } else {
                        validos.add(p);
                    }
                } catch (Exception e) {
                    incidencias.add("Producto " + (i + 1) + ": error en el formato de los datos.");
                }
            }
            return new ResultadoLectura(validos, array.size(), incidencias);
        }
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

    private String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString().trim() : "";
    }

    private int entero(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : -1;
    }
}
