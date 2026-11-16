# Registro de Pruebas - DataStock UT3 (Componentes Reutilizables - RA6)

**Nombre del Alumno:** Santiago de la Fuente  
**Módulo:** Acceso a Datos (2.º DAM)  
**Centro:** IES Isidra de Guzmán (Curso 2026–2027)  
**Proyecto:** DataStock - Entrega 3 (UT3)  
**Componentes evaluados:** `LectorProductosJson` y `ConsultaProductoJdbc` (`es.iesisidra.datastock.componente`)  

---

## 1. Resumen de las Pruebas Mínimas Obligatorias (Apartado 7)

| ID Caso | Acción realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-COMP-01** | Importar un JSON válido desde el menú principal (`Opción 2 -> LectorProductosJson`). | Ruta: `datos/ejemplos/productos_ut2_ejemplo.json` (contiene 3 productos válidos: `TEC-001`, `RAT-002`, `MON-003`). | `LectorProductosJson` lee el archivo sin usar consola y devuelve la lista de 3 objetos `Producto`. `MenuPrincipal` los añade al inventario en memoria y muestra `[OK]` en verde. | Se muestran `Productos encontrados: 3`, `Importados: 3 | Rechazados: 0` y `[OK] Fichero JSON procesado. Productos disponibles en memoria: 3`. | **SUPERADA** |
| **CP-COMP-02** | Importar un JSON inexistente o mal formado desde el menú principal (`Opción 2 -> LectorProductosJson`). | 1) Ruta inexistente: `datos/ejemplos/no_existe.json`<br>2) Fichero con error / mal formado: `datos/ejemplos/catalogo_valido.xml` (sintaxis no JSON) y `datos/ejemplos/catalogo_con_error.json`. | `LectorProductosJson` lanza excepción controlada (`FileNotFoundException` o `IllegalArgumentException`). `MenuPrincipal` la captura, muestra `[ERROR]` en rojo y DataStock sigue funcionando en el menú principal. | Muestra en rojo `[ERROR] Error al importar JSON: El fichero no existe o no se puede leer.` (y ante sintaxis inválida: `[ERROR] Error al importar JSON: Sintaxis JSON incorrecta...`) regresando al menú sin cerrarse. | **SUPERADA** |
| **CP-COMP-03** | Buscar en MySQL un código existente y otro inexistente (`Opción 7 > 5 > 2 -> ConsultaProductoJdbc`). | 1) Código existente en MySQL: `TEC-001`<br>2) Código inexistente en MySQL: `NO-EXISTE` | `ConsultaProductoJdbc` ejecuta `PreparedStatement` con `?` y cierra `ResultSet`, `PreparedStatement` y `Connection`. Para `TEC-001` devuelve su `ProductoDetalle` y el menú imprime `[OK]` y la ficha; para `NO-EXISTE` devuelve `null` y el menú muestra `[AVISO]` sin lanzar excepciones sin controlar. | Para `TEC-001` muestra `[OK] Producto localizado en MySQL:` seguido de su ficha completa con categoría y proveedor. Para `NO-EXISTE` muestra en amarillo `[AVISO] No existe ningún producto con el código 'NO-EXISTE' en MySQL.`. | **SUPERADA** |

---

## 2. Detalle de Ejecución y Evidencia de Consola

### Prueba 1 (`CP-COMP-01`): Importar un JSON válido mediante `LectorProductosJson`
* **Opción del menú:** `2. Importar productos desde JSON (UT1)`
* **Componente invocado internamente:** `es.iesisidra.datastock.componente.LectorProductosJson`
* **Entrada introducida:** `datos/ejemplos/productos_ut2_ejemplo.json`
* **Salida obtenida en consola:**
```text
Selecciona una opción: 2
Ruta del fichero JSON: datos/ejemplos/productos_ut2_ejemplo.json
Productos encontrados: 3
Importados: 3 | Rechazados: 0
[OK] Fichero JSON procesado. Productos disponibles en memoria: 3
```

---

### Prueba 2 (`CP-COMP-02`): Importar un JSON inexistente o mal formado mediante `LectorProductosJson`
* **Opción del menú:** `2. Importar productos desde JSON (UT1)`
* **Componente invocado internamente:** `es.iesisidra.datastock.componente.LectorProductosJson`
* **Entradas introducidas:**
  1. Fichero inexistente: `datos/ejemplos/no_existe.json`
  2. Fichero mal formado (no JSON): `datos/ejemplos/catalogo_valido.xml`
* **Salida obtenida en consola:**
```text
Selecciona una opción: 2
Ruta del fichero JSON: datos/ejemplos/no_existe.json
[ERROR] Error al importar JSON: El fichero no existe o no se puede leer.

Selecciona una opción: 2
Ruta del fichero JSON: datos/ejemplos/catalogo_valido.xml
[ERROR] Error al importar JSON: Sintaxis JSON incorrecta: com.google.gson.stream.MalformedJsonException: Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 2 path $
```
*(La aplicación controla ambos errores en rojo y permanece activa en el menú principal).*

---

### Prueba 3 (`CP-COMP-03`): Buscar producto por código en MySQL mediante `ConsultaProductoJdbc`
* **Opción del menú:** `7. Gestionar base de datos MySQL -> 5. Consultar y buscar productos -> 2. Buscar producto por código`
* **Componente invocado internamente:** `es.iesisidra.datastock.componente.ConsultaProductoJdbc`
* **Entradas introducidas:**
  1. Código existente: `TEC-001`
  2. Código inexistente: `NO-EXISTE`
* **Salida obtenida en consola:**
```text
--- MENÚ DE CONSULTAS ---
1. Listar todos los productos
2. Buscar producto por código
3. Buscar productos por nombre
4. Consultar productos con stock inferior a un umbral
0. Volver al menú MySQL
Selecciona una opción: 2
Introduce el código del producto: TEC-001
[OK] Producto localizado en MySQL:

FICHA DEL PRODUCTO
----------------------------------------
Código:    TEC-001
Nombre:    Teclado mecánico
Precio:    54,90 €
Stock:     114
Categoría: [1] Perifericos
Proveedor: [1] Distribuciones Norte
----------------------------------------

Selecciona una opción: 2
Introduce el código del producto: NO-EXISTE
[AVISO] No existe ningún producto con el código 'NO-EXISTE' en MySQL.
```

---

## 3. Verificación Automatizada con JUnit 5 (`ComponentesUT3Test`)

Además de las pruebas funcionales desde el menú de consola, se ha incluido la clase de pruebas JUnit 5 `src/test/java/es/iesisidra/datastock/componente/ComponentesUT3Test.java` ejecutable con `mvn test`, que verifica los componentes de forma aislada (`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`).
