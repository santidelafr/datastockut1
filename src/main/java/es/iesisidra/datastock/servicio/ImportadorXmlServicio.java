package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Servicio encargado exclusivamente de la importación de productos desde ficheros XML (UT1 - RF-FIC-03).
 */
public class ImportadorXmlServicio {

    public void importarXML(String rutaArchivo, Inventario inventario) throws Exception {
        File file = new File(rutaArchivo);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new FileNotFoundException("El fichero XML no existe o no es accesible.");
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        if (!"catalogo".equals(doc.getDocumentElement().getNodeName())) {
            throw new IllegalArgumentException("La raíz del XML debe ser '<catalogo>'.");
        }

        NodeList nodes = doc.getElementsByTagName("producto");
        int encontrados = nodes.getLength();
        int importados = 0;
        List<String> incidencias = new ArrayList<>();
        Set<String> codigosEnFichero = new HashSet<>();

        for (int i = 0; i < encontrados; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                try {
                    String codigo = obtenerValorEtiqueta("codigo", elem);
                    String nombre = obtenerValorEtiqueta("nombre", elem);
                    BigDecimal precio = new BigDecimal(obtenerValorEtiqueta("precio", elem));
                    int stock = Integer.parseInt(obtenerValorEtiqueta("stock", elem));
                    int catId = Integer.parseInt(obtenerValorEtiqueta("categoriaId", elem));
                    int provId = Integer.parseInt(obtenerValorEtiqueta("proveedorId", elem));

                    Producto p = new Producto(codigo, nombre, precio, stock, catId, provId);
                    String error = inventario.validarProducto(p);

                    if (error != null) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): " + error);
                    } else if (codigosEnFichero.contains(codigo.toUpperCase()) || !inventario.agregarProducto(p)) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): código duplicado.");
                    } else {
                        codigosEnFichero.add(codigo.toUpperCase());
                        importados++;
                    }
                } catch (Exception e) {
                    incidencias.add("Producto " + (i + 1) + ": faltan campos o contienen formato inválido.");
                }
            }
        }

        System.out.println("Encontrados: " + encontrados + " | Importados: " + importados + " | Rechazados: " + (encontrados - importados));
        if (!incidencias.isEmpty()) {
            System.out.println("INCIDENCIAS:");
            incidencias.forEach(inc -> System.out.println(" - " + inc));
        }
    }

    private String obtenerValorEtiqueta(String tag, Element elem) {
        NodeList nl = elem.getElementsByTagName(tag);
        if (nl != null && nl.getLength() > 0 && nl.item(0) != null) {
            return nl.item(0).getTextContent().trim();
        }
        return "";
    }
}
