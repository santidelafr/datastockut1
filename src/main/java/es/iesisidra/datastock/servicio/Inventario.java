package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.iesisidra.datastock.modelo.Producto;

public class Inventario {
    private List<Producto> productos = new ArrayList<>();
    private static final Pattern PATRON_CODIGO = Pattern.compile("^[A-Z0-9-]+$", Pattern.CASE_INSENSITIVE);

    public List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public void setProductos(List<Producto> productos) {
        this.productos = new ArrayList<>(productos);
    }

    // Validaciones de negocio común para JSON y XML
    public String validarProducto(Producto p) {
        if (p.getCodigo() == null || p.getCodigo().trim().isEmpty()) {
            return "El código es obligatorio.";
        }
        String cod = p.getCodigo().trim();
        if (cod.length() > 12 || !PATRON_CODIGO.matcher(cod).matches()) {
            return "El código no cumple el patrón permitido (máx 12 caracteres alfanuméricos/guiones).";
        }
        if (p.getNombre() == null || p.getNombre().trim().length() < 3 || p.getNombre().trim().length() > 100) {
            return "El nombre debe tener entre 3 y 100 caracteres.";
        }
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio debe ser mayor que 0.";
        }
        if (p.getStock() < 0) {
            return "El stock no puede ser negativo.";
        }
        if (p.getCategoriaId() <= 0) {
            return "El ID de categoría debe ser un entero positivo.";
        }
        if (p.getProveedorId() <= 0) {
            return "El ID de proveedor debe ser un entero positivo.";
        }
        return null;
    }

    public boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        String c = codigo.trim();
        return productos.stream().anyMatch(p -> p.getCodigo().equalsIgnoreCase(c));
    }

    //Metodos para validar JSON
    public void importarJSON(String rutaArchivo) throws Exception {
        Path path = Paths.get(rutaArchivo);
        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new FileNotFoundException("El fichero no existe o no se puede leer.");
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject() || !element.getAsJsonObject().has("productos")) {
                throw new IllegalArgumentException("Estructura JSON inválida: no contiene el objeto raíz 'productos'.");
            }

            JsonArray array = element.getAsJsonObject().getAsJsonArray("productos");
            int encontrados = array.size();
            int importados = 0;
            List<String> incidencias = new ArrayList<>();
            Set<String> codigosEnFichero = new HashSet<>();

            for (int i = 0; i < array.size(); i++) {
                try {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    String codigo = obj.has("codigo") && !obj.get("codigo").isJsonNull() ? obj.get("codigo").getAsString().trim() : "";
                    String nombre = obj.has("nombre") && !obj.get("nombre").isJsonNull() ? obj.get("nombre").getAsString().trim() : "";
                    BigDecimal precio = obj.has("precio") && !obj.get("precio").isJsonNull() ? obj.get("precio").getAsBigDecimal() : null;
                    int stock = obj.has("stock") && !obj.get("stock").isJsonNull() ? obj.get("stock").getAsInt() : -1;
                    int catId = obj.has("categoriaId") && !obj.get("categoriaId").isJsonNull() ? obj.get("categoriaId").getAsInt() : -1;
                    int provId = obj.has("proveedorId") && !obj.get("proveedorId").isJsonNull() ? obj.get("proveedorId").getAsInt() : -1;

                    Producto p = new Producto(codigo, nombre, precio, stock, catId, provId);
                    String error = validarProducto(p);

                    if (error != null) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): " + error);
                    } else if (existeCodigo(codigo) || codigosEnFichero.contains(codigo.toUpperCase())) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): código duplicado.");
                    } else {
                        productos.add(p);
                        codigosEnFichero.add(codigo.toUpperCase());
                        importados++;
                    }
                } catch (Exception e) {
                    incidencias.add("Producto " + (i + 1) + ": error en el formato de los datos.");
                }
            }

            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + (encontrados - importados));
            if (!incidencias.isEmpty()) {
                System.out.println("INCIDENCIAS:");
                incidencias.forEach(inc -> System.out.println(" - " + inc));
            }
        }
    }

    // RF-FIC-03: Importar XML
    public void importarXML(String rutaArchivo) throws Exception {
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

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                try {
                    String codigo = getTagValue("codigo", elem);
                    String nombre = getTagValue("nombre", elem);
                    BigDecimal precio = new BigDecimal(getTagValue("precio", elem));
                    int stock = Integer.parseInt(getTagValue("stock", elem));
                    int catId = Integer.parseInt(getTagValue("categoriaId", elem));
                    int provId = Integer.parseInt(getTagValue("proveedorId", elem));

                    Producto p = new Producto(codigo, nombre, precio, stock, catId, provId);
                    String error = validarProducto(p);

                    if (error != null) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): " + error);
                    } else if (existeCodigo(codigo) || codigosEnFichero.contains(codigo.toUpperCase())) {
                        incidencias.add("Producto " + (i + 1) + " (" + codigo + "): código duplicado.");
                    } else {
                        productos.add(p);
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

    private String getTagValue(String tag, Element elem) {
        NodeList nl = elem.getElementsByTagName(tag);
        if (nl != null && nl.getLength() > 0 && nl.item(0) != null) {
            return nl.item(0).getTextContent().trim();
        }
        return "";
    }

    // RF-FIC-04: Consultas
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

    // RF-FIC-05: Exportar
    public void exportarJSON(String rutaDestino) throws Exception {
        if (productos.isEmpty()) {
            throw new IllegalStateException("El inventario está vacío. No hay productos para exportar.");
        }
        Path path = Paths.get(rutaDestino);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        JsonObject root = new JsonObject();
        JsonArray array = new JsonArray();

        for (Producto p : listarOrdenados()) {
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
        try (Writer writer = Files.newBufferedWriter(path)) {
            gson.toJson(root, writer);
        }
    }

    public void exportarXML(String rutaDestino) throws Exception {
        if (productos.isEmpty()) {
            throw new IllegalStateException("El inventario está vacío. No hay productos para exportar.");
        }
        Path path = Paths.get(rutaDestino);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("catalogo");
        doc.appendChild(root);

        for (Producto p : listarOrdenados()) {
            Element prod = doc.createElement("producto");

            Element c = doc.createElement("codigo"); c.setTextContent(p.getCodigo()); prod.appendChild(c);
            Element n = doc.createElement("nombre"); n.setTextContent(p.getNombre()); prod.appendChild(n);
            Element pr = doc.createElement("precio"); pr.setTextContent(p.getPrecio().toString()); prod.appendChild(pr);
            Element s = doc.createElement("stock"); s.setTextContent(String.valueOf(p.getStock())); prod.appendChild(s);
            Element cat = doc.createElement("categoriaId"); cat.setTextContent(String.valueOf(p.getCategoriaId())); prod.appendChild(cat);
            Element prov = doc.createElement("proveedorId"); prov.setTextContent(String.valueOf(p.getProveedorId())); prod.appendChild(prov);

            root.appendChild(prod);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(path.toFile());
        transformer.transform(source, result);
    }

    // RF-FIC-06: Serialización
    public void guardarSerializado(String ruta) throws Exception {
        if (productos.isEmpty()) {
            throw new IllegalStateException("No hay productos en memoria para guardar.");
        }
        File file = new File(ruta);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(productos);
        }
    }

    @SuppressWarnings("unchecked")
    public void recuperarSerializado(String ruta) throws Exception {
        File file = new File(ruta);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Fichero de datos no encontrado.");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                this.productos = (List<Producto>) obj;
            } else {
                throw new IllegalArgumentException("El fichero no contiene un objeto válido de inventario.");
            }
        }
    }
}