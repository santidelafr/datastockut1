# Registro de Pruebas - DataStock UT2 (JDBC MySQL y SQLite)
Santiago de la Fuente Redondo
  



---

## 1. Casos de Prueba JDBC sobre MySQL (`CP-JDBC-01` a `CP-JDBC-20`)

### RF-JDBC-01: Conectar y comprobar el esquema

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-01** | Opción `7 -> 1`: Comprobar conexión y mostrar información de la BD con servidor MySQL activo y credenciales válidas. | `db.url=jdbc:mysql://localhost:3306/datastock...`, usuario válido en `config/db.properties`. | Mensaje `[OK] Conexión establecida correctamente.` en verde; muestra nombre de BD (`datastock`), gestor (`MySQL`), versión detectada y total de productos almacenados. Recursos cerrados. | Muestra `[OK]` en verde con `Base de datos: datastock`, versión de MySQL 8 y recuento de productos sin errores. | SUPERADA |
| **CP-JDBC-02** | Opción `7 -> 1`: Intentar conectar con contraseña errónea (o servidor MySQL detenido). | Variable `JDBC_PASSWORD="clave_erronea"` (o contraseña incorrecta en `config/db.properties`). | Mensaje `[ERROR]` en rojo indicando fallo de credenciales/conexión **sin mostrar la contraseña** ni traza Java; regresa al menú `GESTIÓN MYSQL` sin cerrar la aplicación. | Muestra `[ERROR] Acceso denegado a MySQL: el usuario o la contraseña configurados no son correctos.` en rojo y devuelve el control al menú. | SUPERADA |

---

### RF-JDBC-02: Gestionar categorías y proveedores

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-03** | Opción `7 -> 2` (Alta y listado de categorías) y `7 -> 3` (Alta y listado de proveedores). | Categorías: `(1, "Periféricos")`, `(2, "Monitores")`. Proveedores: `(1, "Distribuciones Norte", "ventas@distribuciones.example")`, `(2, "Mayorista Sur", "")` (sin email). | Inserciones confirmadas con `[OK]` en verde; en los listados aparecen ordenados por ID y el proveedor sin correo muestra `—` (almacenado como `NULL` en MySQL). | Altas registradas en MySQL y listados formateados correctamente; `email` nulo representado con `—`. | SUPERADA |
| **CP-JDBC-04** | Intentar duplicar ID/nombre de categoría o email de proveedor, e intentar eliminar una categoría/proveedor con productos asociados. | Alta categoría con `ID = 1` o nombre `"Periféricos"`; alta proveedor con `"ventas@distribuciones.example"`; baja de categoría `1` teniendo `TEC-001` asociado. | Rechaza duplicados con `[ERROR]` en rojo; al intentar borrar la categoría `1` con productos asociados y confirmar `S`, informa del bloqueo por clave foránea en rojo sin borrar productos ni cerrar el programa. | Todos los duplicados y el borrado con hijos son bloqueados mostrando `[ERROR]` en rojo; los registros permanecen intactos en MySQL. | SUPERADA |

---

### RF-JDBC-03: CRUD de productos

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-05** | Opción `7 -> 4 -> 1` (Alta) y `7 -> 4 -> 2` (Consulta por código). | Código `"aur-004"` (en minúsculas para probar normalización), Nombre `"Auriculares USB"`, Precio `34.95`, Stock `15`, Categoría `1`, Proveedor `1`. | Normaliza el código a `"AUR-004"`, inserta con `PreparedStatement` y `BigDecimal`, muestra `[OK]` en verde y al consultar `"aur-004"` recupera la ficha completa con nombres de categoría y proveedor. | Insertado como `AUR-004` con precio `34.95 €`, stock `15` y ficha completa recuperada desde MySQL. | SUPERADA |
| **CP-JDBC-06** | Opción `7 -> 4 -> 3`: Modificar nombre y precio de un producto existente conservando el resto con Intro. | Producto `"AUR-004"`, nuevo nombre `"Auriculares Gaming Pro"`, nuevo precio `39.99`, Intro en categoría y proveedor, confirmar `S`. | Muestra valores actuales, no permite editar código ni stock, actualiza en MySQL tras confirmar `S` y muestra `[OK]` en verde. En MySQL Workbench figuran el nuevo nombre y precio `39.99`. | Actualización reflejada en MySQL manteniendo intactos el código `AUR-004` y el stock `15`. | SUPERADA |
| **CP-JDBC-07** | Opción `7 -> 4 -> 1`: Intentar duplicar código existente, código con `_`, precio con 3 decimales y categoría inexistente. | 1) Código `"AUR-004"`; 2) Código `"TEC_01"`; 3) Precio `12.345`; 4) Categoría `99` (inexistente). | Cada intento inválido es rechazado en Java antes del `INSERT` mostrando `[ERROR]` explicativo en rojo; no se inserta ningún registro erróneo. | Todos los casos inválidos muestran el motivo exacto en rojo y la tabla `producto` no sufre alteraciones indebidas. | SUPERADA |
| **CP-JDBC-08** | Opción `7 -> 4 -> 4`: Baja permitida de producto sin movimientos y baja bloqueada de producto con movimientos históricos. | 1) Eliminar `"AUR-004"` (sin movimientos, confirmar `S`); 2) Eliminar `"TEC-001"` (con movimientos registrados en `movimiento_stock`, confirmar `S`). | `"AUR-004"` se elimina con `[OK]` en verde. `"TEC-001"` es bloqueado por la FK `fk_mov_producto`, mostrando `[ERROR]` en rojo y conservando tanto el producto como su historial. | `"AUR-004"` eliminado; `"TEC-001"` conservado junto con todos sus registros en `movimiento_stock`. | SUPERADA |

---

### RF-JDBC-04: Consultas con `ResultSet`

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-09** | Opción `7 -> 5 -> 1`: Listar todos los productos almacenados en MySQL. | Productos `MON-003`, `RAT-002`, `TEC-001` en MySQL. | Ejecuta `SELECT` con `INNER JOIN` a `categoria` y `proveedor`, ordena por código ascendente (`MON-003`, `RAT-002`, `TEC-001`), mapea a `ProductoDetalle` y muestra tabla con encabezados y nombres de categoría/proveedor. | Tabla mostrada en orden alfabético por código con nombres reales de categoría y proveedor y total de encontrados. | SUPERADA |
| **CP-JDBC-10** | Opción `7 -> 5 -> 3`: Búsqueda parcial por nombre con resultados y sin resultados. | 1) Fragmento `"ratón"` / `"rat"`; 2) Fragmento `"impresora"`. | Con `"rat"` devuelve `RAT-002` mediante `LIKE ?` preparado sin distinguir mayúsculas/minúsculas. Con `"impresora"` muestra `[AVISO]` informativo sin lanzar error y regresa al menú. | Coincidencia parcial recuperada correctamente; búsqueda sin coincidencias controlada con mensaje informativo. | SUPERADA |
| **CP-JDBC-11** | Opción `7 -> 5 -> 4`: Consultar productos con stock estrictamente inferior a un umbral (`stock < umbral`). | Umbral `10` (teniendo `MON-003` con stock `8` y `TEC-001` con stock `10`). | Muestra únicamente `MON-003` (stock `8 < 10`), excluyendo `TEC-001` (stock `10 == 10`), ordenado por `stock ASC, codigo ASC`. | Solo aparece `MON-003`; `TEC-001` no se incluye al tener stock igual al umbral. | SUPERADA |

---

### RF-JDBC-05: Registrar movimientos con procedimiento almacenado

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-12** | Opción `7 -> 6 -> 1`: Registrar una `ENTRADA` de 5 unidades. | Producto `"TEC-001"` (stock inicial `8`), tipo `ENTRADA`, cantidad `5`, confirmar `S`. | Invoca `{CALL registrar_movimiento(?, ?, ?)}` con `CallableStatement`; muestra `[OK]` en verde con `Stock anterior: 8 -> Stock posterior: 13`; actualiza `producto.stock` a `13` y crea fila en `movimiento_stock`. | Stock actualizado a `13` en `producto` y movimiento `ENTRADA` de `5` (`8 -> 13`) registrado en `movimiento_stock`. | SUPERADA |
| **CP-JDBC-13** | Opción `7 -> 6 -> 2`: Registrar una `SALIDA` válida de 4 unidades. | Producto `"TEC-001"` (stock actual `13`), tipo `SALIDA`, cantidad `4`, confirmar `S`. | Invoca `{CALL registrar_movimiento(?, ?, ?)}`; muestra `[OK]` en verde con `Stock anterior: 13 -> Stock posterior: 9` y registra el movimiento. | Stock pasa de `13` a `9` y se inserta el movimiento `SALIDA` en `movimiento_stock`. | SUPERADA |
| **CP-JDBC-14** | Opción `7 -> 6 -> 2`: Intentar una `SALIDA` superior al stock disponible. | Producto `"TEC-001"` (stock actual `9`), tipo `SALIDA`, cantidad `20`, confirmar `S`. | El procedimiento lanza `SIGNAL SQLSTATE '45000' ('Stock insuficiente')` y ejecuta `ROLLBACK`; Java muestra `[ERROR] Stock insuficiente. El stock permanece en 9.` en rojo; no se añade fila en `movimiento_stock`. | Muestra error en rojo; `producto.stock` sigue en `9` y `movimiento_stock` no contiene ningún registro fallido. | SUPERADA |

---

### RF-JDBC-06: Consultar historial de movimientos

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-15** | Opción `7 -> 7 -> 1` (Historial completo) y `7 -> 7 -> 2` (Filtrado por código). | Código `"TEC-001"`. | Muestra movimientos con `ID`, `FECHA Y HORA`, `PRODUCTO`, `TIPO`, `CANTIDAD`, `STOCK ANT.` y `STOCK POST.`, ordenados por `fecha ASC, id ASC`, sin alterar ningún dato. | Listado cronológico estable con todos los campos tanto en vista global como filtrada por `TEC-001`. | SUPERADA |
| **CP-JDBC-16** | Opción `7 -> 7 -> 2`: Consultar movimientos de un producto existente sin movimientos y de un código inexistente. | 1) Producto `"RAT-002"` (sin movimientos); 2) Código `"NO-EXISTE"`. | Para `"RAT-002"` muestra `[AVISO] Sin movimientos registrados.` sin error. Para `"NO-EXISTE"` informa expresamente con `[ERROR]` de que el producto no existe en MySQL. | Diferencia claramente entre producto existente sin historial y producto inexistente. | SUPERADA |

---

### RF-JDBC-07: Importación controlada desde la UT1

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-17** | Importar JSON en UT1 (opción `2`) y ejecutar `7 -> 8` dos veces siguiendo el recorrido del apartado 6.1. | `datos/ejemplos/productos_ut2_ejemplo.json` (`TEC-001`, `RAT-002`, `MON-003`). 1ª vez: sin proveedor `3`; 2ª vez: tras crear proveedor `3`. | 1ª ejecución: inserta `2` (`TEC-001`, `RAT-002`), omite `0`, rechaza `1` (`MON-003` por proveedor `3` inexistente). 2ª ejecución: inserta `1` (`MON-003`), omite `2` por duplicado, rechaza `0`. Memoria UT1 intacta. | Resúmenes exactos (`2/0/1` y después `1/2/0`), con `commit()` del lote válido y colección en memoria conservada. | SUPERADA |
| **CP-JDBC-18** | Fallo SQL inesperado durante la ejecución del lote transaccional en `7 -> 8`. | Lote con `setAutoCommit(false)` provocando fallo SQL en mitad de la transacción (p. ej. restricción/bloqueo concurrente en BD antes del `commit`). | Se captura `SQLException`, se ejecuta `conn.rollback()`, el contador de insertados queda en `0` y no permanece insertado ningún producto parcial del lote en MySQL. | `rollback()` revierte todas las inserciones previas del lote y muestra aviso de reversión completa en rojo. | SUPERADA |

---

### RF-JDBC-08: Transacción JDBC de demostración

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-JDBC-19** | Opción `7 -> 9`: Escenario A de la demostración de transacciones. | Categorías de prueba libres `ID 9001` (`Demo Commit 9001`) e `ID 9002` (`Demo Commit 9002`), confirmar `S`. | Desactiva `autoCommit`, inserta ambas categorías con `PreparedStatement`, ejecuta `commit()`, comprueba con `SELECT` que ambas existen y muestra `[OK]` en verde. | Ambas categorías (`9001` y `9002`) quedan persistidas y verificadas en MySQL. | SUPERADA |
| **CP-JDBC-20** | Opción `7 -> 9`: Escenario B de la demostración de transacciones. | Inserción de categoría `ID 9003` (`Demo Rollback 9003`) seguida de segunda inserción con `ID 9003` duplicado en la misma transacción. | La 2ª inserción lanza `SQLException` (`1062`), se captura, se ejecuta `rollback()`, se verifica con `SELECT` que `9003` **no** existe en MySQL, se restaura `autoCommit` y se documenta la limpieza. | Error `1062` capturado, `rollback()` confirmado, `ID 9003` ausente en MySQL y `autoCommit` restaurado a `true`. | SUPERADA |

---

## 2. Casos de Prueba de la Actividad Comparativa SQLite (`CP-SQLITE-01` a `CP-SQLITE-03`)

| ID Caso | Operación realizada | Datos utilizados | Resultado esperado | Resultado obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **CP-SQLITE-01** | Primera ejecución independiente de `DemoSQLite` sin que exista `datos/sqlite/productos_demo.db`. | `mvn exec:java -Dexec.mainClass="es.iesisidra.datastock.complementarias.DemoSQLite"` con registros `SQL-001` y `SQL-002`. | Crea la carpeta `datos/sqlite` y el archivo `productos_demo.db`, crea la tabla `producto_demo`, muestra `Insertados: 2 | Ya existentes: 0` y lista `SQL-001` y `SQL-002`. | Salida exacta con `[OK]`, `Insertados: 2 | Ya existentes: 0` y las dos filas recuperadas por `ResultSet`. | SUPERADA |
| **CP-SQLITE-02** | Segunda ejecución consecutiva de `DemoSQLite` sobre el mismo fichero `.db`. | Misma orden Maven sobre `datos/sqlite/productos_demo.db` ya existente. | `INSERT OR IGNORE` evita duplicados y errores de PK; muestra `Insertados: 0 | Ya existentes: 2` y el `SELECT` sigue devolviendo únicamente las dos filas (`SQL-001` y `SQL-002`). | Muestra `Insertados: 0 | Ya existentes: 2` y las dos únicas filas sin duplicados. | SUPERADA |
| **CP-SQLITE-03** | Comprobación de cierre de recursos JDBC y persistencia del fichero local tras finalizar el proceso. | Inspección del sistema de archivos en `datos/sqlite/productos_demo.db` tras terminar `DemoSQLite`. | `Connection`, `Statement`, `PreparedStatement` y `ResultSet` se cierran mediante `try-with-resources` sin bloqueos y el archivo `productos_demo.db` permanece en disco con tamaño > 0 bytes. | Mensaje final `[OK] Recursos cerrados. Fichero persistente verificado (...)` y archivo `.db` presente en `datos/sqlite/`. | SUPERADA |
