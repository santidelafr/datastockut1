package es.iesisidra.datastock.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MovimientoStock {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private long id;
    private String productoCodigo;
    private String tipo;
    private int cantidad;
    private LocalDateTime fecha;
    private int stockAnterior;
    private int stockPosterior;

    public MovimientoStock(long id, String productoCodigo, String tipo, int cantidad,
                           LocalDateTime fecha, int stockAnterior, int stockPosterior) {
        this.id = id;
        this.productoCodigo = productoCodigo;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.stockAnterior = stockAnterior;
        this.stockPosterior = stockPosterior;
    }

    public long getId() { return id; }
    public String getProductoCodigo() { return productoCodigo; }
    public String getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public int getStockAnterior() { return stockAnterior; }
    public int getStockPosterior() { return stockPosterior; }

    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "—";
    }

    @Override
    public String toString() {
        return String.format("%-6d | %-19s | %-12s | %-7s | %8d | %10d | %10d",
                id, getFechaFormateada(), productoCodigo, tipo, cantidad, stockAnterior, stockPosterior);
    }
}
