package es.iesisidra.datastock.servicio;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Servicio encargado exclusivamente de exportar el inventario a ficheros JSON y XML (UT1 - RF-FIC-05).
 */
public class ExportadorServicio {

    public void exportarJSON(String rutaDestino, List<Producto> productosOrdenados) throws Exception {
        if (productosOrdenados == null || productosOrdenados.isEmpty()) {
            throw new IllegalStateException("El inventario está vacío. No hay productos para exportar.");
        }
        Path path = prepararDirectorioPadre(rutaDestino);

        JsonObject root = new JsonObject();
        JsonArray array = new JsonArray();

        for (Producto p : productosOrdenados) {
            JsonObject obj = new JsonObject();
            obj.addProperty("codigo", p.getCodigo());
            obj.addProperty("nombre", p.getNombre());
            obj.addProperty("precio", p.getPrecio());
            obj.addProperty("stock", p.getStock());
            obj.addProperty("categoriaId", p.getCategoriaId());
            obj.addProperty("proveedorId", p.getProveedorId());
            array.add(obj);
        }
        root.add("productos", array);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        }
    }

    public void exportarXML(String rutaDestino, List<Producto> productosOrdenados) throws Exception {
        if (productosOrdenados == null || productosOrdenados.isEmpty()) {
            throw new IllegalStateException("El inventario está vacío. No hay productos para exportar.");
        }
        Path path = prepararDirectorioPadre(rutaDestino);

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("catalogo");
        doc.appendChild(root);

        for (Producto p : productosOrdenados) {
            Element prod = doc.createElement("producto");
            agregarNodoTexto(doc, prod, "codigo", p.getCodigo());
            agregarNodoTexto(doc, prod, "nombre", p.getNombre());
            agregarNodoTexto(doc, prod, "precio", p.getPrecio().toString());
            agregarNodoTexto(doc, prod, "stock", String.valueOf(p.getStock()));
            agregarNodoTexto(doc, prod, "categoriaId", String.valueOf(p.getCategoriaId()));
            agregarNodoTexto(doc, prod, "proveedorId", String.valueOf(p.getProveedorId()));
            root.appendChild(prod);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(doc), new StreamResult(path.toFile()));
    }

    private Path prepararDirectorioPadre(String rutaDestino) throws Exception {
        Path path = Paths.get(rutaDestino);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        return path;
    }

    private void agregarNodoTexto(Document doc, Element padre, String etiqueta, String valor) {
        Element nodo = doc.createElement(etiqueta);
        nodo.setTextContent(valor);
        padre.appendChild(nodo);
    }
}
