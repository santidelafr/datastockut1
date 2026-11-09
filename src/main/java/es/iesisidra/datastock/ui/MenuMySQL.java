package es.iesisidra.datastock.ui;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDAO;
import es.iesisidra.datastock.dao.ProductoDAO;
import es.iesisidra.datastock.dao.ProveedorDAO;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ServicioTransaccionesBD;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.ValidadorBD;

/**
 * Menú y submenús de consola para la gestión de la base de datos MySQL en la UT2.
 */
public class MenuMySQL {
    private final Inventario inventarioMemoria;
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ServicioTransaccionesBD servicioTransacciones = new ServicioTransaccionesBD();

    public MenuMySQL(Inventario inventarioMemoria) {
        this.inventarioMemoria = inventarioMemoria;
    }

    public void iniciar() {
        int opcion = -1;
        do {
            System.out.println("\n================================================");
            System.out.println("              GESTIÓN MYSQL - UT2               ");
            System.out.println("================================================");
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("0. Volver al menú principal");
            System.out.print("Selecciona una opción: ");

            opcion = leerOpcionMenu();
            switch (opcion) {
                case 1:
                    servicioTransacciones.comprobarConexionYEsquema();
                    break;
                case 2:
                    menuCategorias();
                    break;
                case 3:
                    menuProveedores();
                    break;
                case 4:
                    menuProductosCRUD();
                    break;
                case 5:
                    menuConsultasProductos();
                    break;
                case 0:
                    break;
                default:
                    ConsolaColor.aviso("Opción no válida.");
                    break;
            }
        } while (opcion != 0);
    }

    // ==================== 7.2 SUBMENÚ DE CATEGORÍAS ====================

    private void menuCategorias() {
        int op = -1;
        do {
            System.out.println("\n--- MENÚ DE CATEGORÍAS ---");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("0. Volver al menú MySQL");
            System.out.print("Selecciona una opción: ");

            op = leerOpcionMenu();
            switch (op) {
                case 1:
                    altaCategoria();
                    break;
                case 2:
                    listarCategorias();
                    break;
                case 3:
                    modificarCategoria();
                    break;
                case 4:
                    eliminarCategoria();
                    break;
                case 0:
                    break;
                default:
                    ConsolaColor.aviso("Opción no válida.");
                    break;
            }
        } while (op != 0);
    }

    private void altaCategoria() {
        System.out.println("\nALTA DE CATEGORÍA");
        System.out.println("----------------------------------------");
        Integer id = pedirEnteroPositivo("ID de categoría: ");
        if (id == null) return;

        try {
            if (categoriaDAO.existeId(id)) {
                ConsolaColor.error("Ya existe una categoría con el ID " + id + ".");
                return;
            }

            System.out.print("Nombre: ");
            String nombre = LectorConsola.leerTexto();
            String errNombre = ValidadorBD.validarNombreCategoria(nombre);
            if (errNombre != null) {
                ConsolaColor.error(errNombre);
                return;
            }

            if (categoriaDAO.existeNombre(nombre)) {
                ConsolaColor.error("Ya existe otra categoría con el nombre '" + nombre + "'.");
                return;
            }

            int filas = categoriaDAO.insertar(new Categoria(id, nombre));
            if (filas > 0) {
                ConsolaColor.ok("Categoría registrada correctamente.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void listarCategorias() {
        System.out.println("\nLISTADO DE CATEGORÍAS");
        System.out.println("----------------------------------------");
        try {
            List<Categoria> lista = categoriaDAO.listarTodas();
            if (lista.isEmpty()) {
                System.out.println("No hay categorías registradas.");
                return;
            }
            System.out.printf("%-6s %-40s%n", "ID", "NOMBRE");
            System.out.println("----------------------------------------");
            for (Categoria c : lista) {
                System.out.println(c);
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void modificarCategoria() {
        System.out.println("\nMODIFICAR CATEGORÍA");
        System.out.println("----------------------------------------");
        Integer id = pedirEnteroPositivo("ID de la categoría a modificar: ");
        if (id == null) return;

        try {
            Categoria actual = categoriaDAO.buscarPorId(id);
            if (actual == null) {
                ConsolaColor.error("No existe ninguna categoría con ID " + id + ".");
                return;
            }

            System.out.println("Nombre actual: " + actual.getNombre());
            System.out.print("Nuevo nombre (1-60 caracteres): ");
            String nuevoNombre = LectorConsola.leerTexto();

            String errNombre = ValidadorBD.validarNombreCategoria(nuevoNombre);
            if (errNombre != null) {
                ConsolaColor.error(errNombre);
                return;
            }

            if (categoriaDAO.existeNombreEnOtra(nuevoNombre, id)) {
                ConsolaColor.error("Ya existe otra categoría con el nombre '" + nuevoNombre + "'.");
                return;
            }

            if (!ConsolaColor.confirmar("¿Confirmas cambiar el nombre a '" + nuevoNombre + "'?")) {
                System.out.println("Operación cancelada.");
                return;
            }

            int filas = categoriaDAO.actualizarNombre(id, nuevoNombre);
            if (filas > 0) {
                ConsolaColor.ok("Nombre de la categoría actualizado correctamente.");
            } else {
                ConsolaColor.aviso("No se modificó ningún registro.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void eliminarCategoria() {
        System.out.println("\nELIMINAR CATEGORÍA");
        System.out.println("----------------------------------------");
        Integer id = pedirEnteroPositivo("ID de la categoría a eliminar: ");
        if (id == null) return;

        try {
            Categoria actual = categoriaDAO.buscarPorId(id);
            if (actual == null) {
                ConsolaColor.error("No existe ninguna categoría con ID " + id + ".");
                return;
            }

            System.out.println("Categoría seleccionada -> ID: " + actual.getId() + " | Nombre: " + actual.getNombre());
            if (!ConsolaColor.confirmar("¿Confirmas la eliminación de esta categoría?")) {
                System.out.println("Operación cancelada.");
                return;
            }

            int filas = categoriaDAO.eliminar(id);
            if (filas > 0) {
                ConsolaColor.ok("Categoría eliminada correctamente.");
            } else {
                ConsolaColor.aviso("No existía el registro indicado.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                ConsolaColor.error("No se puede eliminar la categoría con ID " + id +
                        " porque tiene productos asociados (integridad referencial).");
            } else {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        }
    }

    // ==================== 7.3 SUBMENÚ DE PROVEEDORES ====================

    private void menuProveedores() {
        int op = -1;
        do {
            System.out.println("\n--- MENÚ DE PROVEEDORES ---");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar un proveedor");
            System.out.println("4. Eliminar un proveedor");
            System.out.println("0. Volver al menú MySQL");
            System.out.print("Selecciona una opción: ");

            op = leerOpcionMenu();
            switch (op) {
                case 1:
                    altaProveedor();
                    break;
                case 2:
                    listarProveedores();
                    break;
                case 3:
                    modificarProveedor();
                    break;
                case 4:
                    eliminarProveedor();
                    break;
                case 0:
                    break;
                default:
                    ConsolaColor.aviso("Opción no válida.");
                    break;
            }
        } while (op != 0);
    }

    private void altaProveedor() {
        System.out.println("\nALTA DE PROVEEDOR");
        System.out.println("----------------------------------------");
        Integer id = pedirEnteroPositivo("ID de proveedor: ");
        if (id == null) return;

        try {
            if (proveedorDAO.existeId(id)) {
                ConsolaColor.error("Ya existe un proveedor con el ID " + id + ".");
                return;
            }

            System.out.print("Nombre: ");
            String nombre = LectorConsola.leerTexto();
            String errNombre = ValidadorBD.validarNombreProveedor(nombre);
            if (errNombre != null) {
                ConsolaColor.error(errNombre);
                return;
            }

            System.out.print("Email (opcional): ");
            String entradaEmail = LectorConsola.leerTexto();
            String email = entradaEmail.isEmpty() ? null : entradaEmail;

            String errEmail = ValidadorBD.validarEmailProveedor(email);
            if (errEmail != null) {
                ConsolaColor.error(errEmail);
                return;
            }

            if (email != null && proveedorDAO.existeEmail(email)) {
                ConsolaColor.error("Ya existe un proveedor registrado con el correo '" + email + "'.");
                return;
            }

            int filas = proveedorDAO.insertar(new Proveedor(id, nombre, email));
            if (filas > 0) {
                ConsolaColor.ok("Proveedor registrado correctamente.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void listarProveedores() {
        System.out.println("\nLISTADO DE PROVEEDORES");
        System.out.println("------------------------------------------------------------------------------");
        try {
            List<Proveedor> lista = proveedorDAO.listarTodos();
            if (lista.isEmpty()) {
                ConsolaColor.aviso("No hay proveedores registrados.");
                return;
            }
            System.out.printf("%-6s %-35s %-35s%n", "ID", "NOMBRE", "EMAIL");
            System.out.println("------------------------------------------------------------------------------");
            for (Proveedor p : lista) {
                System.out.println(p);
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void modificarProveedor() {
        System.out.println("\nMODIFICAR PROVEEDOR");
        System.out.println("----------------------------------------");
        Integer id = pedirEnteroPositivo("ID del proveedor a modificar: ");
        if (id == null) return;

        try {
            Proveedor actual = proveedorDAO.buscarPorId(id);
            if (actual == null) {
                ConsolaColor.error("No existe ningún proveedor con ID " + id + ".");
                return;
            }

            System.out.println("Datos actuales -> Nombre: " + actual.getNombre() +
                    " | Email: " + actual.getEmailFormateado());
            System.out.println("(Pulsa Intro para conservar el dato actual. Escribe '-' en el email para dejarlo vacío/NULL)");

            System.out.print("Nuevo nombre: ");
            String entradaNombre = LectorConsola.leerTexto();
            String nuevoNombre = entradaNombre.isEmpty() ? actual.getNombre() : entradaNombre;

            String errNombre = ValidadorBD.validarNombreProveedor(nuevoNombre);
            if (errNombre != null) {
                ConsolaColor.error(errNombre);
                return;
            }

            System.out.print("Nuevo email: ");
            String entradaEmail = LectorConsola.leerTexto();
            String nuevoEmail;
            if (entradaEmail.isEmpty()) {
                nuevoEmail = actual.getEmail();
            } else if ("-".equals(entradaEmail)) {
                nuevoEmail = null;
            } else {
                nuevoEmail = entradaEmail;
            }

            String errEmail = ValidadorBD.validarEmailProveedor(nuevoEmail);
            if (errEmail != null) {
                ConsolaColor.error(errEmail);
                return;
            }

            if (nuevoEmail != null && proveedorDAO.existeEmailEnOtro(nuevoEmail, id)) {
                ConsolaColor.error("El correo '" + nuevoEmail + "' ya está registrado en otro proveedor.");
                return;
            }

            if (!ConsolaColor.confirmar("¿Confirmas la modificación del proveedor?")) {
                System.out.println("Operación cancelada.");
                return;
            }

            int filas = proveedorDAO.actualizar(new Proveedor(id, nuevoNombre, nuevoEmail));
            if (filas > 0) {
                ConsolaColor.ok("Proveedor actualizado correctamente.");
            } else {
                ConsolaColor.aviso("No se modificó ningún registro.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void eliminarProveedor() {
        System.out.println("\nELIMINAR PROVEEDOR");
        System.out.println("----------------------------------------");
        Integer id = pedirEnteroPositivo("ID del proveedor a eliminar: ");
        if (id == null) return;

        try {
            Proveedor actual = proveedorDAO.buscarPorId(id);
            if (actual == null) {
                ConsolaColor.error("No existe ningún proveedor con ID " + id + ".");
                return;
            }

            System.out.println("Proveedor seleccionado -> ID: " + actual.getId() +
                    " | Nombre: " + actual.getNombre() +
                    " | Email: " + actual.getEmailFormateado());

            if (!ConsolaColor.confirmar("¿Confirmas la eliminación de este proveedor?")) {
                System.out.println("Operación cancelada.");
                return;
            }

            int filas = proveedorDAO.eliminar(id);
            if (filas > 0) {
                ConsolaColor.ok("Proveedor eliminado correctamente.");
            } else {
                ConsolaColor.aviso("No existía el proveedor indicado.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                ConsolaColor.error("No se puede eliminar el proveedor con ID " + id +
                        " porque existen productos que lo referencian (integridad referencial).");
            } else {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        }
    }

    // ==================== 7.4 SUBMENÚ CRUD DE PRODUCTOS ====================

    private void menuProductosCRUD() {
        int op = -1;
        do {
            System.out.println("\n--- MENÚ DE PRODUCTOS ---");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menú MySQL");
            System.out.print("Selecciona una opción: ");

            op = leerOpcionMenu();
            switch (op) {
                case 1:
                    altaProducto();
                    break;
                case 2:
                    consultarProductoPorCodigo();
                    break;
                case 3:
                    modificarProducto();
                    break;
                case 4:
                    eliminarProducto();
                    break;
                case 0:
                    break;
                default:
                    ConsolaColor.aviso("Opción no válida.");
                    break;
            }
        } while (op != 0);
    }

    private void altaProducto() {
        System.out.println("\nALTA DE PRODUCTO");
        System.out.println("----------------------------------------");
        System.out.print("Código (1-12 caracteres, A-Z, 0-9 o guion): ");
        String codigo = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());

        String errCodigo = ValidadorBD.validarCodigoProducto(codigo);
        if (errCodigo != null) {
            ConsolaColor.error(errCodigo);
            return;
        }

        try {
            if (productoDAO.existeCodigo(codigo)) {
                ConsolaColor.error("El producto con código " + codigo + " ya existe en MySQL.");
                return;
            }

            System.out.print("Nombre (3-100 caracteres): ");
            String nombre = LectorConsola.leerTexto();
            String errNombre = ValidadorBD.validarNombreProducto(nombre);
            if (errNombre != null) {
                ConsolaColor.error(errNombre);
                return;
            }

            System.out.print("Precio (> 0, máx. 2 decimales): ");
            BigDecimal precio;
            try {
                precio = ValidadorBD.parsearPrecio(LectorConsola.leerTexto());
            } catch (IllegalArgumentException ex) {
                ConsolaColor.error(ex.getMessage());
                return;
            }

            Integer stock = pedirEnteroNoNegativo("Stock inicial (>= 0): ");
            if (stock == null) return;

            Integer categoriaId = pedirEnteroPositivo("ID de categoría: ");
            if (categoriaId == null) return;
            if (!categoriaDAO.existeId(categoriaId)) {
                ConsolaColor.error("No existe la categoría con ID " + categoriaId + " en MySQL.");
                return;
            }

            Integer proveedorId = pedirEnteroPositivo("ID de proveedor: ");
            if (proveedorId == null) return;
            if (!proveedorDAO.existeId(proveedorId)) {
                ConsolaColor.error("No existe el proveedor con ID " + proveedorId + " en MySQL.");
                return;
            }

            Producto nuevo = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            int filas = productoDAO.insertar(nuevo);
            if (filas > 0) {
                ConsolaColor.ok("Producto " + codigo + " guardado correctamente.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void consultarProductoPorCodigo() {
        System.out.print("Introduce el código del producto: ");
        String codigo = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        if (codigo.isEmpty()) {
            ConsolaColor.error("Debes indicar un código de producto.");
            return;
        }

        try {
            ProductoDetalle detalle = productoDAO.buscarPorCodigo(codigo);
            if (detalle == null) {
                ConsolaColor.aviso("No existe ningún producto con el código '" + codigo + "' en MySQL.");
                return;
            }
            System.out.println("\nFICHA DEL PRODUCTO");
            System.out.println("----------------------------------------");
            System.out.println(detalle.formatearFicha());
            System.out.println("----------------------------------------");
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void modificarProducto() {
        System.out.println("\nMODIFICAR PRODUCTO");
        System.out.println("----------------------------------------");
        System.out.print("Código del producto a modificar: ");
        String codigo = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        if (codigo.isEmpty()) {
            ConsolaColor.error("El código es obligatorio.");
            return;
        }

        try {
            ProductoDetalle actual = productoDAO.buscarPorCodigo(codigo);
            if (actual == null) {
                ConsolaColor.error("No existe ningún producto con el código '" + codigo + "'.");
                return;
            }

            System.out.println("Valores actuales:");
            System.out.println(actual.formatearFicha());
            System.out.println("(Pulsa Intro para conservar cada valor. El código y el stock no se modifican desde aquí)");

            System.out.print("Nuevo nombre [" + actual.getNombre() + "]: ");
            String entNombre = LectorConsola.leerTexto();
            String nuevoNombre = entNombre.isEmpty() ? actual.getNombre() : entNombre;
            String errNombre = ValidadorBD.validarNombreProducto(nuevoNombre);
            if (errNombre != null) {
                ConsolaColor.error(errNombre);
                return;
            }

            System.out.print("Nuevo precio [" + actual.getPrecio() + "]: ");
            String entPrecio = LectorConsola.leerTexto();
            BigDecimal nuevoPrecio = actual.getPrecio();
            if (!entPrecio.isEmpty()) {
                try {
                    nuevoPrecio = ValidadorBD.parsearPrecio(entPrecio);
                } catch (IllegalArgumentException ex) {
                    ConsolaColor.error(ex.getMessage());
                    return;
                }
            }

            System.out.print("Nuevo ID de categoría [" + actual.getCategoriaId() + "]: ");
            String entCat = LectorConsola.leerTexto();
            int nuevaCatId = actual.getCategoriaId();
            if (!entCat.isEmpty()) {
                try {
                    nuevaCatId = Integer.parseInt(entCat);
                    if (nuevaCatId <= 0) {
                        ConsolaColor.error("El ID de categoría debe ser un entero positivo.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    ConsolaColor.error("El ID de categoría debe ser un número entero válido.");
                    return;
                }
                if (!categoriaDAO.existeId(nuevaCatId)) {
                    ConsolaColor.error("No existe la categoría con ID " + nuevaCatId + " en MySQL.");
                    return;
                }
            }

            System.out.print("Nuevo ID de proveedor [" + actual.getProveedorId() + "]: ");
            String entProv = LectorConsola.leerTexto();
            int nuevoProvId = actual.getProveedorId();
            if (!entProv.isEmpty()) {
                try {
                    nuevoProvId = Integer.parseInt(entProv);
                    if (nuevoProvId <= 0) {
                        ConsolaColor.error("El ID de proveedor debe ser un entero positivo.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    ConsolaColor.error("El ID de proveedor debe ser un número entero válido.");
                    return;
                }
                if (!proveedorDAO.existeId(nuevoProvId)) {
                    ConsolaColor.error("No existe el proveedor con ID " + nuevoProvId + " en MySQL.");
                    return;
                }
            }

            if (!ConsolaColor.confirmar("¿Confirmas guardar los cambios del producto " + codigo + "?")) {
                System.out.println("Operación cancelada.");
                return;
            }

            Producto modificado = new Producto(
                    codigo, nuevoNombre, nuevoPrecio, actual.getStock(), nuevaCatId, nuevoProvId);
            int filas = productoDAO.actualizar(modificado);
            if (filas > 0) {
                ConsolaColor.ok("Producto " + codigo + " modificado correctamente.");
            } else {
                ConsolaColor.aviso("No se modificó ningún registro.");
            }
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void eliminarProducto() {
        System.out.println("\nELIMINAR PRODUCTO");
        System.out.println("----------------------------------------");
        System.out.print("Código del producto a eliminar: ");
        String codigo = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        if (codigo.isEmpty()) {
            ConsolaColor.error("El código es obligatorio.");
            return;
        }

        try {
            ProductoDetalle actual = productoDAO.buscarPorCodigo(codigo);
            if (actual == null) {
                ConsolaColor.error("No existe ningún producto con el código '" + codigo + "'.");
                return;
            }

            System.out.println("Datos del producto a eliminar:");
            System.out.println(actual.formatearFicha());

            if (!ConsolaColor.confirmar("¿Confirmas la eliminación del producto " + codigo + "?")) {
                System.out.println("Operación cancelada.");
                return;
            }

            int filas = productoDAO.eliminar(codigo);
            if (filas > 0) {
                ConsolaColor.ok("Producto " + codigo + " eliminado correctamente.");
            } else {
                ConsolaColor.aviso("No existía el producto indicado.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                ConsolaColor.error("No se puede eliminar el producto " + codigo +
                        " porque tiene movimientos de stock registrados en el historial.");
            } else {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        }
    }

    // ==================== 7.5 SUBMENÚ DE CONSULTAS DE PRODUCTOS ====================

    private void menuConsultasProductos() {
        int op = -1;
        do {
            System.out.println("\n--- MENÚ DE CONSULTAS ---");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock inferior a un umbral");
            System.out.println("0. Volver al menú MySQL");
            System.out.print("Selecciona una opción: ");

            op = leerOpcionMenu();
            switch (op) {
                case 1:
                    listarTodosProductosBD();
                    break;
                case 2:
                    consultarProductoPorCodigo();
                    break;
                case 3:
                    buscarProductosPorNombreBD();
                    break;
                case 4:
                    consultarStockInferiorUmbral();
                    break;
                case 0:
                    break;
                default:
                    ConsolaColor.aviso("Opción no válida.");
                    break;
            }
        } while (op != 0);
    }

    private void listarTodosProductosBD() {
        try {
            List<ProductoDetalle> lista = productoDAO.listarTodos();
            if (lista.isEmpty()) {
                ConsolaColor.aviso("No hay productos almacenados en MySQL.");
                return;
            }
            imprimirTablaProductosDetalle("PRODUCTOS ALMACENADOS EN MYSQL", lista);
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void buscarProductosPorNombreBD() {
        System.out.print("Fragmento del nombre a buscar: ");
        String fragmento = LectorConsola.leerTexto();
        if (fragmento.isEmpty()) {
            ConsolaColor.error("Debes introducir un fragmento de texto no vacío.");
            return;
        }

        try {
            List<ProductoDetalle> lista = productoDAO.buscarPorNombre(fragmento);
            if (lista.isEmpty()) {
                ConsolaColor.aviso("No se han encontrado productos cuyo nombre contenga '" + fragmento + "'.");
                return;
            }
            imprimirTablaProductosDetalle("RESULTADOS DE BÚSQUEDA POR NOMBRE", lista);
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void consultarStockInferiorUmbral() {
        Integer umbral = pedirEnteroNoNegativo("Introduce el umbral de stock (entero >= 0): ");
        if (umbral == null) return;

        try {
            List<ProductoDetalle> lista = productoDAO.buscarStockInferior(umbral);
            if (lista.isEmpty()) {
                ConsolaColor.aviso("No hay productos con stock estrictamente inferior a " + umbral + ".");
                return;
            }
            imprimirTablaProductosDetalle("PRODUCTOS CON STOCK INFERIOR A " + umbral, lista);
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void imprimirTablaProductosDetalle(String titulo, List<ProductoDetalle> lista) {
        System.out.println("\n" + titulo);
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-28s | %11s | %5s | %-20s | %-22s%n",
                "CÓDIGO", "NOMBRE", "PRECIO", "STOCK", "CATEGORÍA", "PROVEEDOR");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (ProductoDetalle p : lista) {
            System.out.println(p);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        System.out.println("Total de productos encontrados: " + lista.size());
    }

    // ==================== AUXILIARES DE LECTURA ====================

    private int leerOpcionMenu() {
        String entrada = LectorConsola.leerTexto();
        try {
            return Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Integer pedirEnteroPositivo(String etiqueta) {
        System.out.print(etiqueta);
        String entrada = LectorConsola.leerTexto();
        try {
            int valor = Integer.parseInt(entrada);
            if (valor <= 0) {
                ConsolaColor.error("El valor debe ser un número entero positivo (> 0).");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            ConsolaColor.error("Debes introducir un número entero válido.");
            return null;
        }
    }

    private Integer pedirEnteroNoNegativo(String etiqueta) {
        System.out.print(etiqueta);
        String entrada = LectorConsola.leerTexto();
        try {
            int valor = Integer.parseInt(entrada);
            if (valor < 0) {
                ConsolaColor.error("El valor no puede ser negativo (>= 0).");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            ConsolaColor.error("Debes introducir un número entero válido.");
            return null;
        }
    }
}
