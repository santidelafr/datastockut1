package es.iesisidra.datastock.servicio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Servicio encargado exclusivamente de serializar (.dat) y deserializar el inventario (UT1 - RF-FIC-06).
 */
public class SerializacionServicio {

    public void guardar(String ruta, List<Producto> productos) throws Exception {
        if (productos == null || productos.isEmpty()) {
            throw new IllegalStateException("No hay productos en memoria para guardar.");
        }
        File file = new File(ruta);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(productos));
        }
    }

    public List<Producto> recuperar(String ruta) throws Exception {
        File file = new File(ruta);
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("Fichero de datos no encontrado.");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (!(obj instanceof List<?> listaCruda)) {
                throw new IllegalArgumentException("El fichero no contiene una lista válida de inventario.");
            }
            List<Producto> recuperados = new ArrayList<>();
            for (Object elem : listaCruda) {
                if (!(elem instanceof Producto prod)) {
                    throw new IllegalArgumentException("El fichero contiene elementos que no son instancias de Producto.");
                }
                recuperados.add(prod);
            }
            return recuperados;
        }
    }
}
