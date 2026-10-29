package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XPathActividad {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("===== CONSULTAS XPATH =====");
        System.out.print("Ruta del fichero XML: ");
        String ruta = sc.nextLine();

        try {
            File file = new File(ruta);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();

            int op = -1;
            do {
                System.out.println("\n1. Buscar por código | 2. Filtrar por categoría | 3. Precio superior | 4. Contar | 0. Salir");
                System.out.print("Selecciona opción: ");
                try {
                    op = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    op = -1;
                }

                switch (op) {
                    case 1:
                        System.out.print("Código: ");
                        String cod = sc.nextLine().trim();
                        String expr = String.format("/catalogo/producto[translate(codigo, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')='%s']", cod.toUpperCase());
                        NodeList nodes1 = (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
                        imprimirNodos(nodes1);
                        break;
                    case 2:
                        System.out.print("ID Categoría: ");
                        int cat = Integer.parseInt(sc.nextLine());
                        String exprCat = "/catalogo/producto[categoriaId=" + cat + "]";
                        NodeList nodes2 = (NodeList) xpath.evaluate(exprCat, doc, XPathConstants.NODESET);
                        imprimirNodos(nodes2);
                        break;
                    case 3:
                        System.out.print("Precio mínimo: ");
                        double pMin = Double.parseDouble(sc.nextLine());
                        String exprPrecio = "/catalogo/producto[precio > " + pMin + "]";
                        NodeList nodes3 = (NodeList) xpath.evaluate(exprPrecio, doc, XPathConstants.NODESET);
                        imprimirNodos(nodes3);
                        break;
                    case 4:
                        Double count = (Double) xpath.evaluate("count(/catalogo/producto)", doc, XPathConstants.NUMBER);
                        System.out.println("Total de productos en XML: " + count.intValue());
                        break;
                    case 0:
                        System.out.println("Saliendo de la actividad XPath...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            } while (op != 0);

        } catch (Exception e) {
            System.out.println("Error XPath: " + e.getMessage());
        }
    }

    private static void imprimirNodos(NodeList nodes) {
        System.out.println("Resultados encontrados: " + nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String c = el.getElementsByTagName("codigo").item(0).getTextContent();
            String n = el.getElementsByTagName("nombre").item(0).getTextContent();
            String p = el.getElementsByTagName("precio").item(0).getTextContent();
            System.out.printf(" - %-10s | %-25s | %s €%n", c, n, p);
        }
    }
}