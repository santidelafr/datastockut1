package es.iesisidra.datastock.modelo;

import java.math.BigDecimal;

/**
 * DTO utilizado en las consultas con JOIN sobre MySQL (RF-JDBC-04) para
 * transportar los datos del producto junto con los nombres de su categoría y proveedor.
 */
public class ProductoDetalle {
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private int categoriaId;
    private String categoriaNombre;
    private int proveedorId;
    private String proveedorNombre;

    public ProductoDetalle(String codigo, String nombre, BigDecimal precio, int stock,
                           int categoriaId, String categoriaNombre,
                           int proveedorId, String proveedorNombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public int getProveedorId() { return proveedorId; }
    public String getProveedorNombre() { return proveedorNombre; }

    public String formatearFicha() {
        return String.format(
                "Código:    %s%n" +
                "Nombre:    %s%n" +
                "Precio:    %.2f €%n" +
                "Stock:     %d%n" +
                "Categoría: [%d] %s%n" +
                "Proveedor: [%d] %s",
                codigo, nombre, precio, stock,
                categoriaId, categoriaNombre,
                proveedorId, proveedorNombre);
    }

    @Override
    public String toString() {
        return String.format("%-12s | %-28s | %9.2f € | %5d | %-20s | %-22s",
                codigo, nombre, precio, stock, categoriaNombre, proveedorNombre);
    }
}
