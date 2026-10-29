package es.iesisidra.datastock.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private int categoriaId;
    private int proveedorId;

    public Producto() {}

    public Producto(String codigo, String nombre, BigDecimal precio, int stock, int categoriaId, int proveedorId) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.proveedorId = proveedorId;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(codigo != null ? codigo.trim().toUpperCase() : null,
                              producto.codigo != null ? producto.codigo.trim().toUpperCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo != null ? codigo.trim().toUpperCase() : null);
    }

    @Override
    public String toString() {
        return String.format("Código: %-12s | Nombre: %-25s | Precio: %8.2f € | Stock: %4d | Cat: %d | Prov: %d",
                codigo, nombre, precio, stock, categoriaId, proveedorId);
    }
}