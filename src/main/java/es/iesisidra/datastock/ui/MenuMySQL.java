package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDAO;
import es.iesisidra.datastock.dao.ProveedorDAO;
import es.iesisidra.datastock.modelo.Categoria;
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
