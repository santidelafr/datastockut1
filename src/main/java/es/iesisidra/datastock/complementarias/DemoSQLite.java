package es.iesisidra.datastock.complementarias;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import es.iesisidra.datastock.util.ConsolaColor;

/**
 * Actividad comparativa obligatoria (apartado 7 - RA2-b):
 * Demostración independiente de JDBC con un gestor embebido (SQLite) sin servidor.
 */
public class DemoSQLite {
    private static final String RUTA_DB = "datos/sqlite/productos_demo.db";
    private static final String URL_SQLITE = "jdbc:sqlite:" + RUTA_DB;

    public static void main(String[] args) {
        try {
            // 1. Crear el directorio datos/sqlite si todavía no existe
            Path rutaFichero = Paths.get(RUTA_DB);
            if (rutaFichero.getParent() != null && !Files.exists(rutaFichero.getParent())) {
                Files.createDirectories(rutaFichero.getParent());
            }

            // 2. Abrir conexión JDBC SQLite y cerrar todos los recursos con try-with-resources
            try (Connection conn = DriverManager.getConnection(URL_SQLITE)) {
                ConsolaColor.ok("Conexión SQLite abierta: " + RUTA_DB);

                // 3. Crear tabla de demostración mediante Statement
                String sqlTabla = "CREATE TABLE IF NOT EXISTS producto_demo (" +
                                  "  codigo TEXT PRIMARY KEY NOT NULL," +
                                  "  nombre TEXT NOT NULL" +
                                  ");";
                try (Statement st = conn.createStatement()) {
                    st.execute(sqlTabla);
                }
                ConsolaColor.ok("Tabla producto_demo disponible.");

                // 4. Insertar dos registros de ejemplo controlando duplicados con INSERT OR IGNORE
                String sqlInsert = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
                int insertados = 0;
                int totalIntentos = 2;

                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setString(1, "SQL-001");
                    ps.setString(2, "Producto demo A");
                    insertados += ps.executeUpdate();

                    ps.setString(1, "SQL-002");
                    ps.setString(2, "Producto demo B");
                    insertados += ps.executeUpdate();
                }

                int yaExistentes = totalIntentos - insertados;
                System.out.println("Insertados: " + insertados + " | Ya existentes: " + yaExistentes);

                // 5. Consultar y recorrer los resultados con ResultSet
                String sqlSelect = "SELECT codigo, nombre FROM producto_demo ORDER BY codigo";
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(sqlSelect)) {
                    while (rs.next()) {
                        System.out.printf("%s | %s%n",
                                rs.getString("codigo"),
                                rs.getString("nombre"));
                    }
                }
            }

            // 6. Comprobar que el fichero .db persiste tras cerrar la conexión
            if (Files.exists(rutaFichero)) {
                ConsolaColor.ok("Recursos cerrados. Fichero persistente verificado (" +
                        Files.size(rutaFichero) + " bytes).");
            }
        } catch (Exception e) {
            ConsolaColor.error("Error en la demostración de SQLite: " + e.getMessage());
        }
    }
}
