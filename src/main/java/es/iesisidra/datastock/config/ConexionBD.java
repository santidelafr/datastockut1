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
 * Gestiona la apertura de conexiones JDBC hacia MySQL leyendo las credenciales
 * de variables de entorno (JDBC_URL, JDBC_USER, JDBC_PASSWORD) o del fichero
 * local config/db.properties (excluido de Git).
 */
public class ConexionBD {
    private static final String RUTA_PROPERTIES = "config/db.properties";

    private ConexionBD() {}

    /**
     * Obtiene una nueva conexión JDBC a la base de datos MySQL configurada.
     */
    public static Connection obtenerConexion() throws SQLException {
        Properties props = new Properties();
        Path ruta = Paths.get(RUTA_PROPERTIES);

        if (Files.exists(ruta) && Files.isRegularFile(ruta)) {
            try (InputStream is = Files.newInputStream(ruta)) {
                props.load(is);
            } catch (Exception e) {
                throw new SQLException("No se pudo leer el fichero de configuración local '" + RUTA_PROPERTIES + "'.");
            }
        }

        String url = obtenerValor("JDBC_URL", props, "db.url");
        String user = obtenerValor("JDBC_USER", props, "db.user");
        String password = obtenerPassword("JDBC_PASSWORD", props, "db.password");

        if (url == null || url.isBlank()) {
            throw new SQLException("Falta configurar la URL de conexión (variable JDBC_URL o 'db.url' en " + RUTA_PROPERTIES + ").");
        }
        if (user == null || user.isBlank()) {
            throw new SQLException("Falta configurar el usuario de BD (variable JDBC_USER o 'db.user' en " + RUTA_PROPERTIES + ").");
        }

        return DriverManager.getConnection(url.trim(), user.trim(), password);
    }

    private static String obtenerValor(String nombreEnv, Properties props, String claveProp) {
        String env = System.getenv(nombreEnv);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String prop = props.getProperty(claveProp);
        return prop != null ? prop.trim() : null;
    }

    private static String obtenerPassword(String nombreEnv, Properties props, String claveProp) {
        String env = System.getenv(nombreEnv);
        if (env != null) {
            return env;
        }
        return props.getProperty(claveProp, "");
    }

    /**
     * Traduce una excepción JDBC en un mensaje claro para el usuario final,
     * evitando mostrar contraseñas o trazas internas.
     */
    public static String traducirErrorSQL(SQLException e) {
        if (e == null) {
            return "Error desconocido de base de datos.";
        }
        String sqlState = e.getSQLState() != null ? e.getSQLState() : "";
        int code = e.getErrorCode();

        // Señales enviadas por el procedimiento almacenado registrar_movimiento
        if ("45000".equals(sqlState) && e.getMessage() != null) {
            return e.getMessage();
        }
        // Fallo de red / servidor MySQL detenido
        if (sqlState.startsWith("08")) {
            return "No se puede conectar con el servidor MySQL. Verifica que el servicio está iniciado y el puerto es accesible.";
        }
        // Credenciales inválidas (nunca mostrar la contraseña)
        if (code == 1045 || "28000".equals(sqlState)) {
            return "Acceso denegado a MySQL: el usuario o la contraseña configurados no son correctos.";
        }
        // Base de datos datastock no creada
        if (code == 1049) {
            return "El esquema 'datastock' no existe en MySQL. Ejecuta primero el script sql/DataStock_BD_MySQL.sql.";
        }
        // Tabla o procedimiento inexistente
        if (code == 1146 || code == 1305) {
            return "Faltan tablas o procedimientos del esquema 'datastock'. Ejecuta el script sql/DataStock_BD_MySQL.sql.";
        }
        // Clave primaria o campo UNIQUE duplicado
        if (code == 1062) {
            return "No se puede completar la operación: ya existe un registro con ese identificador o valor único.";
        }
        // Restricción de clave foránea al eliminar (tiene registros hijos asociados)
        if (code == 1451) {
            return "No se puede eliminar el registro porque tiene otros datos asociados en la base de datos (integridad referencial).";
        }
        // Restricción de clave foránea al insertar/actualizar (no existe la referencia padre)
        if (code == 1452) {
            return "Error de integridad referencial: la categoría o el proveedor indicado no existe en MySQL.";
        }
        // Restricción CHECK incumplida en MySQL 8
        if (code == 3819) {
            return "Los datos no cumplen las restricciones de validación (CHECK) definidas en la base de datos.";
        }

        String msg = e.getMessage();
        if (msg != null && (msg.startsWith("Falta configurar") || msg.startsWith("No se pudo leer"))) {
            return msg;
        }
        return "No se pudo completar la operación en MySQL (código SQL: " + code + ").";
    }
}
