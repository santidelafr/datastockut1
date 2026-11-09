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

## 7. Comparativa MySQL y SQLite (Actividad obligatoria - Apartado 7.4)

| Criterio | MySQL (Gestor independiente cliente/servidor) | SQLite (Gestor embebido) |
| :--- | :--- | :--- |
| **¿Necesita servidor?** | **Sí.** Requiere instalar y tener en ejecución el servicio `mysqld` escuchando peticiones por red/puerto (por defecto `3306`). | **No.** Es un motor *serverless* embebido: la biblioteca JDBC lee y escribe directamente sobre el fichero desde el propio proceso Java. |
| **¿Dónde se guardan los datos?** | En el directorio de datos gestionado internamente por el servidor MySQL (p. ej. `/var/lib/mysql` o `ProgramData\MySQL`), accesible mediante sockets/TCP. | En un **único fichero local** dentro de la ruta indicada del proyecto (p. ej. `datos/sqlite/productos_demo.db`). |
| **¿Cómo se establece la conexión JDBC?** | `DriverManager.getConnection(url, user, password)` usando `jdbc:mysql://host:puerto/datastock` y autenticación con usuario y contraseña. | `DriverManager.getConnection("jdbc:sqlite:datos/sqlite/productos_demo.db")` indicando solo la ruta del archivo, sin usuario ni contraseña de red. |
| **¿Qué dependencia se utiliza?** | `com.mysql:mysql-connector-j` (`8.4.0`). | `org.xerial:sqlite-jdbc` (`3.46.1.3`). |
| **Ventajas** | Alta concurrencia multiusuario, control granular de permisos y usuarios, procedimientos almacenados (`registrar_movimiento`), bloqueos a nivel de fila (`InnoDB` + `FOR UPDATE`) y escalabilidad en red. | Cero administración e instalación, muy ligero, portable en un solo archivo `.db` e ideal para pruebas rápidas o almacenamiento local. |
| **Inconvenientes** | Requiere instalar, configurar y mantener un servidor activo, gestionar credenciales y mayor consumo de recursos. | Escrituras concurrentes muy limitadas (bloqueo a nivel de fichero/base de datos), sin gestión nativa de usuarios de red ni procedimientos almacenados (`CALL`). |
| **¿Para qué tipo de aplicación lo elegirías?** | Aplicaciones empresariales, backends web, sistemas multiusuario en red o inventarios compartidos donde varios puestos consultan y actualizan el stock simultáneamente con fuertes garantías transaccionales. | Aplicaciones móviles (Android), utilidades de escritorio monousuario, catálogos locales offline, dispositivos IoT o prototipos y tests locales. |

### ¿Qué es un pool de conexiones y por qué DataStock usa conexiones cortas?

Un **pool de conexiones** (como *HikariCP* o *Apache DBCP*) es un componente que mantiene un conjunto de conexiones JDBC ya abiertas y listas para reutilizarse. Cuando la aplicación necesita ejecutar una sentencia SQL, toma prestada una conexión libre del *pool* y, al cerrarla (`conn.close()`), la conexión no se destruye físicamente sino que regresa al *pool* para la siguiente petición.

* **¿Cuándo resulta útil?** Es imprescindible en **aplicaciones web, APIs REST o servidores concurrentes** que reciben decenas o cientos de peticiones por segundo, porque abrir una conexión física TCP, negociar TLS y autenticar contra MySQL en cada petición tendría un coste de tiempo y CPU muy elevado, además de poder agotar el límite de conexiones del servidor.
* **¿Por qué DataStock puede funcionar con conexiones cortas y bien cerradas?** Porque DataStock es una **aplicación interactiva de consola para un único usuario**, donde las operaciones SQL ocurren a ritmo humano (una acción cada varios segundos tras introducir datos por teclado). En este escenario no hay cuello de botella por concurrencia; abrir la conexión bajo demanda y cerrarla inmediatamente al terminar el bloque `try-with-resources` mantiene el diseño sencillo, evita dejar conexiones colgadas o inactivas por *timeout* mientras el usuario está en un menú y garantiza la liberación inmediata de `Connection`, `PreparedStatement`, `CallableStatement` y `ResultSet`.
