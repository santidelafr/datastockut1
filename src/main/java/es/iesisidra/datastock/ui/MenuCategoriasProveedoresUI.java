package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDAO;
import es.iesisidra.datastock.dao.ProveedorDAO;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.ValidadorBD;

/** Submenús resumidos de Categorías (7.2) y Proveedores (7.3) en MySQL. */
public class MenuCategoriasProveedoresUI {
    private final CategoriaDAO catDAO = new CategoriaDAO();
    private final ProveedorDAO provDAO = new ProveedorDAO();

    public void menuCategorias() {
        int op;
        do {
            System.out.print("\n--- CATEGORÍAS (1. Alta | 2. Listar | 3. Modificar | 4. Eliminar | 0. Volver): ");
            op = MenuMySQL.leerOpcionMenu();
            try {
                switch (op) {
                    case 1 -> altaCategoria();
                    case 2 -> {
                        List<Categoria> l = catDAO.listarTodas();
                        if (l.isEmpty()) System.out.println("No hay categorías.");
                        else l.forEach(System.out::println);
                    }
                    case 3 -> modificarCategoria();
                    case 4 -> eliminarCategoria();
                    case 0 -> {}
                    default -> ConsolaColor.aviso("Opción no válida.");
                }
            } catch (SQLException e) {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        } while (op != 0);
    }

    private void altaCategoria() throws SQLException {
        Integer id = MenuMySQL.pedirEnteroPositivo("ID de categoría: ");
        if (id == null) return;
        if (catDAO.existeId(id)) { ConsolaColor.error("Ya existe el ID " + id + "."); return; }
        System.out.print("Nombre: ");
        String nom = LectorConsola.leerTexto();
        String err = ValidadorBD.validarNombreCategoria(nom);
        if (err != null) { ConsolaColor.error(err); return; }
        if (catDAO.existeNombre(nom)) { ConsolaColor.error("Ya existe ese nombre."); return; }
        if (catDAO.insertar(new Categoria(id, nom)) > 0) ConsolaColor.ok("Categoría registrada.");
    }

    private void modificarCategoria() throws SQLException {
        Integer id = MenuMySQL.pedirEnteroPositivo("ID de categoría a modificar: ");
        if (id == null) return;
        Categoria act = catDAO.buscarPorId(id);
        if (act == null) { ConsolaColor.error("No existe la categoría " + id + "."); return; }
        System.out.print("Nuevo nombre [" + act.getNombre() + "]: ");
        String nom = LectorConsola.leerTexto();
        String err = ValidadorBD.validarNombreCategoria(nom);
        if (err != null) { ConsolaColor.error(err); return; }
        if (catDAO.existeNombreEnOtra(nom, id)) { ConsolaColor.error("Nombre duplicado."); return; }
        if (ConsolaColor.confirmar("¿Confirmas el cambio?") && catDAO.actualizarNombre(id, nom) > 0) {
            ConsolaColor.ok("Categoría actualizada.");
        }
    }

    private void eliminarCategoria() throws SQLException {
        Integer id = MenuMySQL.pedirEnteroPositivo("ID de categoría a eliminar: ");
        if (id == null) return;
        if (catDAO.buscarPorId(id) == null) { ConsolaColor.error("No existe la categoría " + id + "."); return; }
        if (ConsolaColor.confirmar("¿Eliminar categoría " + id + "?") && catDAO.eliminar(id) > 0) {
            ConsolaColor.ok("Categoría eliminada.");
        }
    }

    public void menuProveedores() {
        int op;
        do {
            System.out.print("\n--- PROVEEDORES (1. Alta | 2. Listar | 3. Modificar | 4. Eliminar | 0. Volver): ");
            op = MenuMySQL.leerOpcionMenu();
            try {
                switch (op) {
                    case 1 -> altaProveedor();
                    case 2 -> {
                        List<Proveedor> l = provDAO.listarTodos();
                        if (l.isEmpty()) ConsolaColor.aviso("No hay proveedores.");
                        else l.forEach(System.out::println);
                    }
                    case 3 -> modificarProveedor();
                    case 4 -> eliminarProveedor();
                    case 0 -> {}
                    default -> ConsolaColor.aviso("Opción no válida.");
                }
            } catch (SQLException e) {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        } while (op != 0);
    }

    private void altaProveedor() throws SQLException {
        Integer id = MenuMySQL.pedirEnteroPositivo("ID de proveedor: ");
        if (id == null) return;
        if (provDAO.existeId(id)) { ConsolaColor.error("Ya existe el proveedor " + id + "."); return; }
        System.out.print("Nombre: ");
        String nom = LectorConsola.leerTexto();
        if (ValidadorBD.validarNombreProveedor(nom) != null) { ConsolaColor.error(ValidadorBD.validarNombreProveedor(nom)); return; }
        System.out.print("Email (opcional): ");
        String em = LectorConsola.leerTexto();
        String email = em.isEmpty() ? null : em;
        if (ValidadorBD.validarEmailProveedor(email) != null) { ConsolaColor.error(ValidadorBD.validarEmailProveedor(email)); return; }
        if (email != null && provDAO.existeEmail(email)) { ConsolaColor.error("Email ya registrado."); return; }
        if (provDAO.insertar(new Proveedor(id, nom, email)) > 0) ConsolaColor.ok("Proveedor registrado.");
    }

    private void modificarProveedor() throws SQLException {
        Integer id = MenuMySQL.pedirEnteroPositivo("ID de proveedor a modificar: ");
        if (id == null) return;
        Proveedor act = provDAO.buscarPorId(id);
        if (act == null) { ConsolaColor.error("No existe el proveedor " + id + "."); return; }
        System.out.print("Nuevo nombre (Intro conserva [" + act.getNombre() + "]): ");
        String entNom = LectorConsola.leerTexto();
        String nom = entNom.isEmpty() ? act.getNombre() : entNom;
        if (ValidadorBD.validarNombreProveedor(nom) != null) { ConsolaColor.error(ValidadorBD.validarNombreProveedor(nom)); return; }
        System.out.print("Nuevo email (Intro conserva, '-' borra): ");
        String entEm = LectorConsola.leerTexto();
        String email = entEm.isEmpty() ? act.getEmail() : ("-".equals(entEm) ? null : entEm);
        if (ValidadorBD.validarEmailProveedor(email) != null) { ConsolaColor.error(ValidadorBD.validarEmailProveedor(email)); return; }
        if (email != null && provDAO.existeEmailEnOtro(email, id)) { ConsolaColor.error("Email duplicado."); return; }
        if (ConsolaColor.confirmar("¿Confirmas cambios?") && provDAO.actualizar(new Proveedor(id, nom, email)) > 0) {
            ConsolaColor.ok("Proveedor actualizado.");
        }
    }

    private void eliminarProveedor() throws SQLException {
        Integer id = MenuMySQL.pedirEnteroPositivo("ID de proveedor a eliminar: ");
        if (id == null) return;
        if (provDAO.buscarPorId(id) == null) { ConsolaColor.error("No existe el proveedor " + id + "."); return; }
        if (ConsolaColor.confirmar("¿Eliminar proveedor " + id + "?") && provDAO.eliminar(id) > 0) {
            ConsolaColor.ok("Proveedor eliminado.");
        }
    }
}
