package es.iesisidra.datastock.ui;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.componente.ConsultaProductoJdbc;
import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDAO;
import es.iesisidra.datastock.dao.ProductoDAO;
import es.iesisidra.datastock.dao.ProveedorDAO;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.util.ConsolaColor;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.ValidadorBD;

/** Submenús resumidos de Productos CRUD (7.4) y Consultas (7.5). Integra ConsultaProductoJdbc (UT3). */
public class MenuProductosUI {
    private final CategoriaDAO catDAO = new CategoriaDAO();
    private final ProveedorDAO provDAO = new ProveedorDAO();
    private final ProductoDAO prodDAO = new ProductoDAO();
    private final ConsultaProductoJdbc consultaProductoJdbc = new ConsultaProductoJdbc();

    public void menuProductosCRUD() {
        int op;
        do {
            System.out.print("\n--- PRODUCTOS (1. Alta | 2. Consultar código | 3. Modificar | 4. Eliminar | 0. Volver): ");
            op = MenuMySQL.leerOpcionMenu();
            try {
                switch (op) {
                    case 1 -> altaProducto();
                    case 2 -> consultarProductoPorCodigo();
                    case 3 -> modificarProducto();
                    case 4 -> eliminarProducto();
                    case 0 -> {}
                    default -> ConsolaColor.aviso("Opción no válida.");
                }
            } catch (Exception e) {
                ConsolaColor.error(e instanceof SQLException sqle ? ConexionBD.traducirErrorSQL(sqle) : e.getMessage());
            }
        } while (op != 0);
    }

    private void altaProducto() throws SQLException {
        System.out.print("Código: ");
        String cod = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        if (ValidadorBD.validarCodigoProducto(cod) != null) { ConsolaColor.error(ValidadorBD.validarCodigoProducto(cod)); return; }
        if (prodDAO.existeCodigo(cod)) { ConsolaColor.error("El código " + cod + " ya existe."); return; }

        System.out.print("Nombre: ");
        String nom = LectorConsola.leerTexto();
        if (ValidadorBD.validarNombreProducto(nom) != null) { ConsolaColor.error(ValidadorBD.validarNombreProducto(nom)); return; }

        System.out.print("Precio: ");
        BigDecimal precio = ValidadorBD.parsearPrecio(LectorConsola.leerTexto());
        Integer stock = MenuMySQL.pedirEnteroNoNegativo("Stock inicial: ");
        Integer catId = MenuMySQL.pedirEnteroPositivo("ID categoría: ");
        Integer provId = MenuMySQL.pedirEnteroPositivo("ID proveedor: ");
        if (stock == null || catId == null || provId == null) return;

        if (!catDAO.existeId(catId)) { ConsolaColor.error("Categoría " + catId + " no existe."); return; }
        if (!provDAO.existeId(provId)) { ConsolaColor.error("Proveedor " + provId + " no existe."); return; }

        if (prodDAO.insertar(new Producto(cod, nom, precio, stock, catId, provId)) > 0) {
            ConsolaColor.ok("Producto " + cod + " guardado.");
        }
    }

    /** Opción 7 > 5 > 2: Buscar producto por código usando el componente ConsultaProductoJdbc (UT3). */
    public void consultarProductoPorCodigo() {
        System.out.print("Introduce el código del producto: ");
        String cod = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        try {
            ProductoDetalle d = consultaProductoJdbc.buscarPorCodigo(cod);
            if (d == null) ConsolaColor.aviso("No existe ningún producto con el código '" + cod + "' en MySQL.");
            else { ConsolaColor.ok("Producto localizado en MySQL:"); System.out.println(d.formatearFicha()); }
        } catch (IllegalArgumentException e) {
            ConsolaColor.error(e.getMessage());
        } catch (SQLException e) {
            ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
        }
    }

    private void modificarProducto() throws SQLException {
        System.out.print("Código a modificar: ");
        String cod = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        ProductoDetalle act = consultaProductoJdbc.buscarPorCodigo(cod);
        if (act == null) { ConsolaColor.error("No existe el producto " + cod + "."); return; }

        System.out.print("Nuevo nombre (Intro conserva [" + act.getNombre() + "]): ");
        String entNom = LectorConsola.leerTexto();
        String nom = entNom.isEmpty() ? act.getNombre() : entNom;
        if (ValidadorBD.validarNombreProducto(nom) != null) { ConsolaColor.error(ValidadorBD.validarNombreProducto(nom)); return; }

        System.out.print("Nuevo precio (Intro conserva [" + act.getPrecio() + "]): ");
        String entPr = LectorConsola.leerTexto();
        BigDecimal precio = entPr.isEmpty() ? act.getPrecio() : ValidadorBD.parsearPrecio(entPr);

        System.out.print("Nuevo ID categoría (Intro conserva [" + act.getCategoriaId() + "]): ");
        String entCat = LectorConsola.leerTexto();
        int catId = entCat.isEmpty() ? act.getCategoriaId() : Integer.parseInt(entCat);
        if (!catDAO.existeId(catId)) { ConsolaColor.error("Categoría inexistente."); return; }

        System.out.print("Nuevo ID proveedor (Intro conserva [" + act.getProveedorId() + "]): ");
        String entProv = LectorConsola.leerTexto();
        int provId = entProv.isEmpty() ? act.getProveedorId() : Integer.parseInt(entProv);
        if (!provDAO.existeId(provId)) { ConsolaColor.error("Proveedor inexistente."); return; }

        if (ConsolaColor.confirmar("¿Guardar cambios de " + cod + "?")
                && prodDAO.actualizar(new Producto(cod, nom, precio, act.getStock(), catId, provId)) > 0) {
            ConsolaColor.ok("Producto " + cod + " modificado.");
        }
    }

    private void eliminarProducto() throws SQLException {
        System.out.print("Código a eliminar: ");
        String cod = ValidadorBD.normalizarCodigo(LectorConsola.leerTexto());
        ProductoDetalle act = consultaProductoJdbc.buscarPorCodigo(cod);
        if (act == null) { ConsolaColor.error("No existe el producto " + cod + "."); return; }
        System.out.println(act.formatearFicha());
        if (ConsolaColor.confirmar("¿Eliminar " + cod + "?") && prodDAO.eliminar(cod) > 0) {
            ConsolaColor.ok("Producto " + cod + " eliminado.");
        }
    }

    public void menuConsultasProductos() {
        int op;
        do {
            System.out.print("\n--- CONSULTAS (1. Listar todos | 2. Por código | 3. Por nombre | 4. Stock < umbral | 0. Volver): ");
            op = MenuMySQL.leerOpcionMenu();
            try {
                switch (op) {
                    case 1 -> mostrarLista(prodDAO.listarTodos());
                    case 2 -> consultarProductoPorCodigo();
                    case 3 -> {
                        System.out.print("Fragmento de nombre: ");
                        mostrarLista(prodDAO.buscarPorNombre(LectorConsola.leerTexto()));
                    }
                    case 4 -> {
                        Integer u = MenuMySQL.pedirEnteroNoNegativo("Umbral de stock: ");
                        if (u != null) mostrarLista(prodDAO.buscarStockInferior(u));
                    }
                    case 0 -> {}
                    default -> ConsolaColor.aviso("Opción no válida.");
                }
            } catch (SQLException e) {
                ConsolaColor.error(ConexionBD.traducirErrorSQL(e));
            }
        } while (op != 0);
    }

    private void mostrarLista(List<ProductoDetalle> lista) {
        if (lista.isEmpty()) ConsolaColor.aviso("No se encontraron productos.");
        else { lista.forEach(System.out::println); System.out.println("Total: " + lista.size()); }
    }
}
