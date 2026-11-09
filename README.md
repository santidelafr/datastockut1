# DataStock - Gestión de Inventario (UT1 + UT2)

Aplicación de consola desarrollada en **Java 21** con **Apache Maven** para el módulo de *Acceso a Datos* (2.º DAM - IES Isidra de Guzmán).

El proyecto integra las funcionalidades de ficheros y memoria desarrolladas en la **UT1** (JSON, XML, serialización, XPath y acceso aleatorio) con la persistencia relacional en **MySQL** mediante **JDBC** incorporada en la **UT2**, además de una actividad comparativa independiente con **SQLite**.

---

## 1. Requisitos del entorno

* **Java JDK 21**
* **Apache Maven 3.8+**
* **MySQL Server 8.0.16** o superior (y MySQL Workbench o cliente equivalente)
* **Dependencias JDBC empleadas en `pom.xml`:**
  * **MySQL Connector/J:** `com.mysql:mysql-connector-j` versión **`8.4.0`**
  * **SQLite JDBC:** `org.xerial:sqlite-jdbc` versión **`3.46.1.3`** (usada únicamente en `DemoSQLite`)
  * **Gson:** `com.google.code.gson:gson` versión **`2.10.1`**

---

## 2. Preparación de la base de datos MySQL

1. Inicia tu servidor **MySQL 8**.
2. Abre MySQL Workbench (o el cliente `mysql` por terminal) y ejecuta el script incluido en el repositorio:
   * Ruta del script: `sql/DataStock_BD_MySQL.sql`
3. Comprueba que se ha creado el esquema `datastock` con sus cuatro tablas (`categoria`, `proveedor`, `producto`, `movimiento_stock`) y el procedimiento almacenado `registrar_movimiento`.

```sql
USE datastock;
SHOW TABLES;
SHOW PROCEDURE STATUS WHERE Db = 'datastock';
```

---

## 3. Configuración de credenciales JDBC (sin publicar secretos)

La clase `es.iesisidra.datastock.config.ConexionBD` nunca incluye contraseñas en el código fuente. Obtiene los parámetros de conexión siguiendo este orden de prioridad:

### Opción A: Fichero local `config/db.properties` (excluido en `.gitignore`)
1. Copia la plantilla `config/db.properties.example` como `config/db.properties`.
2. Edita tus credenciales locales en `config/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/datastock?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
   db.user=root
   db.password=tu_password_local
   ```

### Opción B: Variables de entorno
Puedes definir las variables de entorno `JDBC_URL`, `JDBC_USER` y `JDBC_PASSWORD` (tienen prioridad si están definidas). Por ejemplo, en PowerShell:

```powershell
$env:JDBC_URL="jdbc:mysql://localhost:3306/datastock?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"
$env:JDBC_USER="root"
$env:JDBC_PASSWORD="tu_password_local"
```

---

## 4. Estructura del proyecto

* `es.iesisidra.datastock`: Clase principal `App`.
* `es.iesisidra.datastock.config`: Configuración y apertura de conexiones JDBC (`ConexionBD`).
* `es.iesisidra.datastock.modelo`: Entidades y DTOs (`Producto`, `Categoria`, `Proveedor`, `MovimientoStock`, `ProductoDetalle`).
* `es.iesisidra.datastock.dao`: Acceso a datos JDBC con `PreparedStatement`, `CallableStatement` y `ResultSet` (`CategoriaDAO`, `ProveedorDAO`, `ProductoDAO`, `MovimientoDAO`).
* `es.iesisidra.datastock.servicio`: Lógica de inventario en memoria de UT1 (`Inventario`) y operaciones transaccionales de UT2 (`ServicioTransaccionesBD`).
* `es.iesisidra.datastock.ui`: Menús de consola (`MenuPrincipal`, `MenuMySQL`).
* `es.iesisidra.datastock.util`: Lectura de teclado, colores ANSI y validaciones (`LectorConsola`, `ConsolaColor`, `ValidadorBD`).
* `es.iesisidra.datastock.complementarias`: Actividades independientes (`AccesoAleatorioActividad`, `XPathActividad`, `DemoSQLite`).
* `sql/`: Script oficial `DataStock_BD_MySQL.sql`.
* `doc/`: Registro de pruebas de la práctica (`Registro_Pruebas_UT2.md`).

---

## 5. Diferencias entre el inventario en memoria (UT1) y MySQL (UT2)

* **Almacenes independientes:** Las opciones **1 a 6** del menú principal trabajan exclusivamente sobre la colección Java `List<Producto>` en memoria RAM (se pierde al cerrar el programa salvo que se exporte o serialice). La opción **7** opera directamente contra la base de datos relacional **MySQL**, cuyos datos persisten entre ejecuciones.
* **Sin sincronización automática:** Importar un fichero JSON o XML en las opciones 2 o 3 **no** inserta productos automáticamente en MySQL. La transferencia desde la colección en memoria hacia MySQL se realiza de forma explícita y validada mediante la opción **7 -> 8 (Importar productos de memoria a MySQL)** dentro de una transacción JDBC.

---

## 6. Compilación y ejecución

### Compilar el proyecto
```bash
mvn clean compile
```

### Ejecutar la aplicación principal DataStock
```bash
mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.App"
```

### Ejecutar la actividad comparativa independiente `DemoSQLite`
*(No requiere tener ningún servidor arrancado; crea automáticamente `datos/sqlite/productos_demo.db`)*:
```bash
mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.complementarias.DemoSQLite"
```

---
