package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import es.iesisidra.datastock.componente.LectorProductosJson;
import es.iesisidra.datastock.modelo.Producto;

/**
 * Gestiona la colección de productos en memoria RAM (UT1) y coordina las consultas
 * y los servicios de importación, exportación y serialización.
 */
public class Inventario {
    private List<Producto> productos = new ArrayList<>();
    private final LectorProductosJson lectorJson = new LectorProductosJson();
    private final ImportadorXmlServicio importadorXml = new ImportadorXmlServicio();
    private final ExportadorServicio exportador = new ExportadorServicio();
    private final SerializacionServicio serializador = new SerializacionServicio();

    public List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public void setProductos(List<Producto> productos) {
        this.productos = new ArrayList<>(productos);
    }

    public String validarProducto(Producto p) {
        return lectorJson.validarProducto(p);
    }

    public boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        String c = codigo.trim();
        return productos.stream().anyMatch(p -> p.getCodigo().equalsIgnoreCase(c));
    }

    public boolean agregarProducto(Producto producto) {
        if (producto == null || validarProducto(producto) != null || existeCodigo(producto.getCodigo())) {
            return false;
        }
        productos.add(producto);
        return true;
    }

    // Consultas en memoria RAM con Java Streams (RF-FIC-04)
    public List<Producto> listarOrdenados() {
        return productos.stream()
                .sorted(Comparator.comparing(p -> p.getCodigo().toUpperCase()))
                .collect(Collectors.toList());
    }

    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) return null;
        String c = codigo.trim();
        return productos.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(c))
                .findFirst()
                .orElse(null);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String n = nombre.trim().toLowerCase();
        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(n))
                .sorted(Comparator.comparing(p -> p.getCodigo().toUpperCase()))
                .collect(Collectors.toList());
    }

    // Delegación en servicios especializados de ficheros
    public void importarJSON(String rutaArchivo) throws Exception {
        LectorProductosJson.ResultadoLectura res = lectorJson.leerConDetalle(rutaArchivo);
        int importados = 0;
        for (Producto p : res.getProductosValidos()) {
            if (agregarProducto(p)) importados++;
        }
        int encontrados = res.getTotalEncontrados();
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Importados: " + importados + " | Rechazados: " + (encontrados - importados));
    }

    public void importarXML(String rutaArchivo) throws Exception {
        importadorXml.importarXML(rutaArchivo, this);
    }

    public void exportarJSON(String rutaDestino) throws Exception {
        exportador.exportarJSON(rutaDestino, listarOrdenados());
    }

    public void exportarXML(String rutaDestino) throws Exception {
        exportador.exportarXML(rutaDestino, listarOrdenados());
    }

    public void guardarSerializado(String ruta) throws Exception {
        serializador.guardar(ruta, productos);
    }

    public void recuperarSerializado(String ruta) throws Exception {
        this.productos = serializador.recuperar(ruta);
    }
}
