package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/**
 * Repositorio dedicado a la persistencia de un único valor: el contador
 * del siguiente ID de Respuesta disponible.
 */
public class IdCounterRepository {

    /** Ruta del fichero JSON donde se almacena el contador. */
    private static final String FILE_PATH = "./../DATA/db/response_id_counter.json";
    
    /** Objeto Gson para serialización/deserialización JSON. */
    private final Gson gson;
    
    /** El contador interno, almacenado como un entero. */
    private int nextResponseId;

    /**
     * Inicializa el repositorio y carga el contador desde el archivo.
     */
    public IdCounterRepository(int initialId) {
        // Usamos un Gson simple ya que solo manejamos un entero
        this.gson = new GsonBuilder().setPrettyPrinting().create(); 
        
        // Carga el ID desde el fichero, o usa el ID inicial si no existe.
        this.nextResponseId = loadId(initialId);
    }

    // -------------------------------------------------------------------
    // MÉTODOS DE PERSISTENCIA
    // -------------------------------------------------------------------
    
    /**
     * Carga el último ID guardado desde el fichero JSON.
     * @param defaultId El ID inicial a usar si el fichero no existe o está vacío.
     * @return El ID cargado.
     */
    private int loadId(int defaultId) {
        File file = new File(FILE_PATH);
        
        // Si el archivo no existe o está vacío, usa el valor inicial por defecto.
        if (!file.exists() || file.length() == 0) {
            return defaultId; 
        }

        try (Reader reader = new FileReader(file)) {
            // Gson puede deserializar directamente un entero desde un JSON que contiene solo ese número.
            Integer loadedId = gson.fromJson(reader, Integer.class);
            return (loadedId != null) ? loadedId : defaultId;
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo cargar el ID. Usando ID inicial. " + e.getMessage());
            return defaultId;
        }
    }

    /**
     * Guarda el contador actual en el fichero JSON.
     */
    public void saveId() {
        File file = new File(FILE_PATH);
        
        // Crear el directorio si no existe (robustez)
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (Writer writer = new FileWriter(file)) {
            // Serializa el entero directamente en el archivo.
            gson.toJson(nextResponseId, writer);
        } catch (Exception e) {
            System.err.println("Error al guardar el contador de IDs: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------
    // MÉTODOS DE ACCESO
    // -------------------------------------------------------------------

    /**
     * Retorna el ID actual (como String) y luego lo incrementa, guardándolo inmediatamente.
     * Se recomienda usar esto cada vez que se crea una nueva respuesta.
     * @return El ID de respuesta único.
     */
    public String getNextIdAndIncrement() {
        String currentId = String.valueOf(nextResponseId);
        
        // 1. Incrementa el contador
        nextResponseId++;
        
        // 2. Guarda el nuevo contador en el fichero (para persistencia inmediata)
        saveId(); 
        
        return currentId;
    }
}