package es.iesisidra.datastock.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC hacia MySQL leyendo variables de entorno o config/db.properties
 * (excluido en .gitignore) y traduce errores SQL sin exponer nunca contraseñas.
 */
public class ConexionBD {
    private static final String RUTA_PROPERTIES = "config/db.properties";

    private ConexionBD() {}

    public static Connection obtenerConexion() throws SQLException {
        Properties props = new Properties();
        Path ruta = Paths.get(RUTA_PROPERTIES);
        if (Files.exists(ruta) && Files.isRegularFile(ruta)) {
            try (InputStream is = Files.newInputStream(ruta)) {
                props.load(is);
            } catch (Exception e) {
                throw new SQLException("No se pudo leer '" + RUTA_PROPERTIES + "'.");
            }
        }

        String url = leerValor("JDBC_URL", props, "db.url");
        String user = leerValor("JDBC_USER", props, "db.user");
        String pass = System.getenv("JDBC_PASSWORD") != null
                ? System.getenv("JDBC_PASSWORD") : props.getProperty("db.password", "");

        if (url == null || url.isBlank() || user == null || user.isBlank()) {
            throw new SQLException("Falta configurar url/usuario en variables de entorno o en " + RUTA_PROPERTIES);
        }
        return DriverManager.getConnection(url.trim(), user.trim(), pass);
    }

    private static String leerValor(String envKey, Properties props, String propKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) return env.trim();
        String p = props.getProperty(propKey);
        return p != null ? p.trim() : null;
    }

    public static String traducirErrorSQL(SQLException e) {
        if (e == null) return "Error desconocido de base de datos.";
        String state = e.getSQLState() != null ? e.getSQLState() : "";
        int code = e.getErrorCode();

        if ("45000".equals(state) && e.getMessage() != null) return e.getMessage();
        if (state.startsWith("08")) return "No se puede conectar con MySQL. Verifica que el servidor está activo.";
        if (code == 1045 || "28000".equals(state)) return "Acceso denegado a MySQL: usuario o contraseña incorrectos.";
        return switch (code) {
            case 1049 -> "El esquema 'datastock' no existe. Ejecuta sql/DataStock_BD_MySQL.sql.";
            case 1146, 1305 -> "Faltan tablas o procedimientos en 'datastock'. Ejecuta sql/DataStock_BD_MySQL.sql.";
            case 1062 -> "Ya existe un registro con ese identificador o valor único.";
            case 1451 -> "No se puede eliminar porque tiene registros asociados (integridad referencial).";
            case 1452 -> "Error de clave foránea: la categoría o el proveedor indicado no existe.";
            case 3819 -> "Los datos incumplen una restricción CHECK de la base de datos.";
            default -> "Error en MySQL (código SQL: " + code + ").";
        };
    }
}
