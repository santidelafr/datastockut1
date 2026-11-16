package es.iesisidra.datastock.componente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Pruebas unitarias automatizadas con JUnit 5 para verificar los componentes
 * reutilizables de la UT3 ({@link LectorProductosJson} y {@link ConsultaProductoJdbc}).
 */
class ComponentesUT3Test {

    private final LectorProductosJson lector = new LectorProductosJson();
    private final ConsultaProductoJdbc consultaJdbc = new ConsultaProductoJdbc();

    @Test
    @DisplayName("CP-COMP-01: LectorProductosJson lee un fichero JSON válido y devuelve objetos Producto")
    void testLeerJsonValido() throws Exception {
        Path rutaValida = Paths.get("datos/ejemplos/productos_ut2_ejemplo.json");
        List<Producto> productos = lector.leerProductos(rutaValida);

        assertNotNull(productos);
        assertEquals(3, productos.size());
        assertEquals("TEC-001", productos.get(0).getCodigo());
        assertEquals("RAT-002", productos.get(1).getCodigo());
        assertEquals("MON-003", productos.get(2).getCodigo());
    }

    @Test
    @DisplayName("CP-COMP-02a: LectorProductosJson lanza FileNotFoundException ante fichero inexistente")
    void testLeerJsonInexistente() {
        Path rutaInexistente = Paths.get("datos/ejemplos/no_existe_ut3.json");
        assertThrows(FileNotFoundException.class, () -> lector.leerProductos(rutaInexistente));
    }

    @Test
    @DisplayName("CP-COMP-02b: LectorProductosJson lanza IllegalArgumentException ante JSON mal formado")
    void testLeerJsonMalFormado(@TempDir Path tempDir) throws Exception {
        Path jsonRoto = tempDir.resolve("mal_formado.json");
        Files.writeString(jsonRoto, "{ \"productos\": [ { \"codigo\": \"TEC-001\" ", StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> lector.leerProductos(jsonRoto));
    }

    @Test
    @DisplayName("CP-COMP-02c: LectorProductosJson rechaza catalogo_con_error.json sin clave raíz 'productos'")
    void testLeerJsonSinRaizProductos() {
        Path rutaConError = Paths.get("datos/ejemplos/catalogo_con_error.json");
        assertThrows(IllegalArgumentException.class, () -> lector.leerProductos(rutaConError));
    }

    @Test
    @DisplayName("LectorProductosJson filtra productos con datos inválidos y registra incidencias sin usar consola")
    void testLeerJsonConDatosInvalidos(@TempDir Path tempDir) throws Exception {
        Path jsonConInvalido = tempDir.resolve("parcial.json");
        String contenido = "{\n" +
                "  \"productos\": [\n" +
                "    {\"codigo\": \"TEC-001\", \"nombre\": \"Teclado USB\", \"precio\": 19.95, \"stock\": 10, \"categoriaId\": 1, \"proveedorId\": 1},\n" +
                "    {\"codigo\": \"ERR-002\", \"nombre\": \"Producto Precio Negativo\", \"precio\": -5.00, \"stock\": 4, \"categoriaId\": 1, \"proveedorId\": 1}\n" +
                "  ]\n" +
                "}";
        Files.writeString(jsonConInvalido, contenido, StandardCharsets.UTF_8);

        LectorProductosJson.ResultadoLectura resultado = lector.leerConDetalle(jsonConInvalido);
        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalEncontrados());
        assertEquals(1, resultado.getProductosValidos().size());
        assertFalse(resultado.getIncidencias().isEmpty());
        assertTrue(resultado.getProductosValidos().size() < resultado.getTotalEncontrados());
    }

    @Test
    @DisplayName("CP-COMP-03 (validación previa): ConsultaProductoJdbc rechaza códigos vacíos o con formato inválido")
    void testConsultaProductoCodigoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> consultaJdbc.buscarPorCodigo(""));
        assertThrows(IllegalArgumentException.class, () -> consultaJdbc.buscarPorCodigo("CODIGO_DEMASIADO_LARGO_12345"));
    }
}
