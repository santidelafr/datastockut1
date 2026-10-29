# DataStock UT1 - Gestión de Inventario

Proyecto desarrollado en Java 21 utilizando Maven para la gestión de inventario de productos. Permite importar/exportar datos en formatos JSON y XML, así como trabajar con ficheros de acceso aleatorio (RAF) y consultas XPath.

## Requisitos
* Java 21 (JDK)
* Apache Maven

## Estructura del Proyecto
* `es.iesisidra.datastock.modelo`: Clases de datos (`Producto`).
* `es.iesisidra.datastock.servicio`: Lógica de negocio e importación/exportación (`Inventario`).
* `es.iesisidra.datastock.ui`: Interfaz de usuario por consola (`MenuPrincipal`).
* `es.iesisidra.datastock.util`: Utilidades de lectura de consola.
* `es.iesisidra.datastock.complementarias`: Actividades de XPath y RandomAccessFile.

## Compilación y Ejecución

Para compilar el proyecto:

mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.App"