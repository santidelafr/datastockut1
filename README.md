# DataStock - Gestión de Inventario (UT1 + UT2 + UT3)

Aplicación de consola desarrollada en **Java 21** con **Apache Maven** para el módulo de *Acceso a Datos* (2.º DAM - IES Isidra de Guzmán).

El proyecto integra las funcionalidades de ficheros y memoria desarrolladas en la **UT1** (JSON, XML, serialización, XPath y acceso aleatorio), la persistencia relacional en **MySQL** y **SQLite** mediante **JDBC** incorporada en la **UT2**, y la refactorización hacia **componentes reutilizables desacoplados** (`LectorProductosJson` y `ConsultaProductoJdbc`) en la **UT3 (RA6)**.

---

## 1. Requisitos del entorno

* **Java JDK 21**
* **Apache Maven 3.8+**
* **MySQL Server 8.0.16** o superior (y MySQL Workbench o cliente equivalente)
* **Dependencias empleadas en `pom.xml`:**
  * **MySQL Connector/J:** `com.mysql:mysql-connector-j` versión **`8.4.0`**
  * **SQLite JDBC:** `org.xerial:sqlite-jdbc` versión **`3.46.1.3`** (usada en `DemoSQLite`)
  * **Gson:** `com.google.code.gson:gson` versión **`2.10.1`**
  * **JUnit 5 (Jupiter):** `org.junit.jupiter:junit-jupiter` versión **`5.10.2`** (pruebas de componentes UT3)

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
* `es.iesisidra.datastock.componente`: **Componentes reutilizables de la UT3 (`LectorProductosJson` y `ConsultaProductoJdbc`)** documentados con Javadoc y sin dependencias de consola.
* `es.iesisidra.datastock.config`: Configuración y apertura de conexiones JDBC (`ConexionBD`).
* `es.iesisidra.datastock.modelo`: Entidades y DTOs (`Producto`, `Categoria`, `Proveedor`, `MovimientoStock`, `ProductoDetalle`).
* `es.iesisidra.datastock.dao`: Acceso a datos JDBC con `PreparedStatement`, `CallableStatement` y `ResultSet` (`CategoriaDAO`, `ProveedorDAO`, `ProductoDAO`, `MovimientoDAO`).
* `es.iesisidra.datastock.servicio`: Inventario en memoria (`Inventario`), servicios especializados de ficheros UT1 (`ImportadorXmlServicio`, `ExportadorServicio`, `SerializacionServicio`) y transacciones JDBC UT2 (`ServicioTransaccionesBD`).
* `es.iesisidra.datastock.ui`: Menú principal (`MenuPrincipal`) y menús MySQL divididos por responsabilidad (`MenuMySQL`, `MenuCategoriasProveedoresUI`, `MenuProductosUI`, `MenuMovimientosUI`).
* `es.iesisidra.datastock.util`: Lectura de teclado, colores ANSI y validaciones (`LectorConsola`, `ConsolaColor`, `ValidadorBD`).
* `es.iesisidra.datastock.complementarias`: Actividades independientes (`AccesoAleatorioActividad`, `XPathActividad`, `DemoSQLite`).
* `sql/`: Script oficial `DataStock_BD_MySQL.sql`.
* `doc/`: Registros de pruebas (`Registro_Pruebas_UT2.md` y `Registro_Pruebas_UT3.md`).

---

## 5. Diferencias entre el inventario en memoria (UT1) y MySQL (UT2)

* **Almacenes independientes:** Las opciones **1 a 6** del menú principal trabajan exclusivamente sobre la colección Java `List<Producto>` en memoria RAM (se pierde al cerrar el programa salvo que se exporte o serialice). La opción **7** opera directamente contra la base de datos relacional **MySQL**, cuyos datos persisten entre ejecuciones.
* **Sin sincronización automática:** Importar un fichero JSON o XML en las opciones 2 o 3 **no** inserta productos automáticamente en MySQL. La transferencia desde la colección en memoria hacia MySQL se realiza de forma explícita y validada mediante la opción **7 -> 8 (Importar productos de memoria a MySQL)** dentro de una transacción JDBC.

---

## 6. Compilación, ejecución y pruebas

### Compilar el proyecto
```bash
mvn clean compile
```

### Ejecutar la aplicación principal DataStock
```bash
mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.App"
```

### Ejecutar las pruebas automatizadas con JUnit 5 (UT3)
```bash
mvn test
```

### Generar la documentación Javadoc de los componentes (UT3)
```bash
mvn javadoc:javadoc
```

### Ejecutar la actividad comparativa independiente `DemoSQLite`
```bash
mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.complementarias.DemoSQLite"
```

---

## 7. Comparativa MySQL y SQLite (Actividad obligatoria - UT2)

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

---

## 8. UT3: componentes reutilizables (Reflexión técnica - RA6)

### 1. ¿Qué ventaja tiene separar la lectura JSON del menú? ¿Qué inconveniente o coste introduce crear más clases?
* **Ventajas:** Al extraer la lectura y validación del fichero JSON a la clase independiente `LectorProductosJson` (eliminando cualquier uso de `Scanner`, `System.out.println` o modificación directa de `Inventario`), cumplimos el **Principio de Responsabilidad Única (SRP)**. El componente recibe un `Path` y devuelve objetos `Producto` validados (o lanza excepciones controladas si el archivo no existe o está mal formado). Gracias a ello, `LectorProductosJson` puede reutilizarse directamente desde cualquier otra interfaz (una ventana JavaFX/Swing, un servicio REST o un proceso por lotes) y probarse de forma aislada con **JUnit 5** sin tener que simular pulsaciones de teclado.
* **Inconvenientes o coste:** Incrementa el número de clases y paquetes del proyecto (`es.iesisidra.datastock.componente`), requiere definir objetos de transferencia o resultados intermedios (`ResultadoLectura`) para comunicar tanto los productos válidos como las incidencias a la capa de interfaz, y añade una pequeña sobrecarga de diseño frente a tener todo el flujo junto en un único método para programas muy pequeños.

### 2. ¿Por qué conviene separar una consulta JDBC de la presentación por consola?
Porque el acceso a datos mediante conectores JDBC (`Connection`, `PreparedStatement` y `ResultSet`) y la interfaz de usuario por consola tienen motivos de cambio totalmente distintos. Al encapsular la consulta parametrizada en `ConsultaProductoJdbc` —que recibe el código del producto, cierra sus recursos con `try-with-resources` y devuelve `ProductoDetalle` (o `null` si no existe) sin imprimir nada por pantalla—, evitamos mezclar sentencias SQL con códigos de color ANSI o menús de texto. Si mañana cambia el formato visual de la ficha en consola, no hay riesgo de romper la sentencia SQL; y si otra parte del programa (como la modificación o baja de un producto, o un test unitario) necesita comprobar un producto en MySQL, puede invocar `ConsultaProductoJdbc` sin ensuciar la salida estándar.

### 3. ¿Qué herramientas has utilizado (IDE, Maven, Javadoc y JUnit) y para qué sirve cada una?
* **IDE (Visual Studio Code con Extension Pack for Java):** entorno de desarrollo integrado utilizado para editar el código fuente, refactorizar las clases hacia el paquete `componente`, navegar entre referencias y detectar errores de compilación en tiempo real.
* **Apache Maven (`pom.xml`):** herramienta de gestión y construcción del proyecto que descarga automáticamente las dependencias (`gson`, `mysql-connector-j`, `sqlite-jdbc` y `junit-jupiter`), compila con JDK 21 (`mvn clean compile`), ejecuta la aplicación (`exec-maven-plugin`) y lanza el ciclo de pruebas (`maven-surefire-plugin`).
* **Javadoc (`/** ... */`):** herramienta estándar de documentación de API en Java. Permite documentar en `LectorProductosJson` y `ConsultaProductoJdbc` la finalidad de cada componente, sus parámetros de entrada (`@param`), el valor devuelto (`@return`) y las excepciones lanzadas (`@throws`), facilitando que otro desarrollador utilice el componente sin necesidad de leer su implementación interna.
* **JUnit 5 (`org.junit.jupiter`):** *framework* de pruebas unitarias automatizadas utilizado en `ComponentesUT3Test` para verificar de forma repetible que `LectorProductosJson` y `ConsultaProductoJdbc` responden adecuadamente ante ficheros válidos, rutas inexistentes, JSON corruptos y códigos inválidos.

### 4. ¿Qué dos opciones de DataStock has modificado para integrar los componentes?
Se han modificado internamente dos opciones ya existentes sin alterar la estructura de menús visible para el usuario:
1. **Opción `2` del Menú Principal (`2. Importar productos desde JSON` en `MenuPrincipal.importarJSON()`):** solicita la ruta por teclado, invoca `lectorProductosJson.leerConDetalle(Paths.get(ruta))`, incorpora los objetos `Producto` válidos al `Inventario` en memoria y muestra por consola el resumen, los avisos de incidencias y los mensajes `[OK]` en verde o `[ERROR]` en rojo.
2. **Opción `7 > 5 > 2` del Menú MySQL (`7. Gestionar base de datos MySQL -> 5. Consultar y buscar productos -> 2. Buscar producto por código` en `MenuProductosUI.consultarProductoPorCodigo()`):** solicita el código de producto al usuario, invoca `consultaProductoJdbc.buscarPorCodigo(codigo)` y se encarga de presentar por consola la ficha formateada con `[OK]` en verde si existe, un `[AVISO]` en amarillo si no se encuentra en MySQL, o `[ERROR]` en rojo si ocurre algún fallo controlado.
