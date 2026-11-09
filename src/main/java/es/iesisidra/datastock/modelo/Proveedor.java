package es.iesisidra.datastock.modelo;

public class Proveedor {
    private int id;
    private String nombre;
    private String email;

    public Proveedor() {}

    public Proveedor(int id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailFormateado() {
        return (email == null || email.isBlank()) ? "—" : email;
    }

    @Override
    public String toString() {
        return String.format("%-6d %-35s %-35s", id, nombre, getEmailFormateado());
    }
}
