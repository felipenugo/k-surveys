package data.stub;

import domain.model.Survey;
import domain.model.Usuario;

import java.util.*;

/**
 * Clase StubPersistencia.
 *
 * Esta clase implementa un mecanismo de persistencia "stub" para almacenar usuarios y encuestas.
 * Sigue el patrón Singleton, de modo que se maneja una única instancia de StubPersistencia a lo largo
 * del ciclo de vida de la aplicación.
 */
public class StubPersistencia {
    private static StubPersistencia instancia;
    private final Map<String, Usuario> usuarios;
    private Map<String, List<Survey>> encuestas;

    /**
     * Constructor privado.
     *
     * Inicializa los mapas para usuarios y encuestas.
     */
    private StubPersistencia() {
        usuarios = new HashMap<>();
        encuestas = new HashMap<>();
    }

    /**
     * Retorna la instancia única de StubPersistencia. Si no existe, la crea.
     *
     * @return la instancia única de StubPersistencia
     */
    public static StubPersistencia getInstancia() {
        if (instancia == null) instancia = new StubPersistencia();
        return instancia;
    }
    /**
     * Establece la instancia actual de StubPersistencia.
     *
     * @param instancia la instancia a asignar
     */
    public static void setInstancia(StubPersistencia instancia) {
        StubPersistencia.instancia = instancia;
    }

    //Retorna null, cuidado
    /**
     * Obtiene la lista de encuestas asociadas a un usuario.
     * <p>
     * Puede retornar null si no existen encuestas registradas para el usuario indicado.
     * </p>
     *
     * @param usuario el nombre del usuario
     * @return la lista de Encuesta asociada al usuario o null si no hay encuestas
     */
    public List<Survey> getEncuestasUsuario(String usuario) {
        return encuestas.get(usuario);
    }

    /**
     * Agrega una encuesta al listado del usuario especificado.
     * <p>
     * Si el usuario no tiene una lista de encuestas en el mapa, se crea una nueva.
     * </p>
     *
     * @param usuario  el nombre del usuario
     * @param encuesta  la encuesta a agregar
     */
    public void agregarEncuesta(String usuario, Survey encuesta) {
        encuestas.computeIfAbsent(usuario, k -> new ArrayList<>()).add(encuesta);
    }
    /**
     * Recupera una Encuesta específica del usuario en base a su id.
     * <p>
     * Se itera sobre la lista de encuestas del usuario para buscar la encuesta cuyo id sea igual al indicado.
     * </p>
     *
     * @param usuario el nombre del usuario
     * @param id      el identificador de la encuesta
     * @return la Encuesta que coincide con el id, o null si no se encuentra
     */
    public Survey getEncuesta(String usuario, String id) {
        List<Survey> encuestas = this.encuestas.get(usuario);
        if(encuestas == null) return null;
        for(Survey encuesta : encuestas) {
            if(encuesta.getSURVEY_ID().equals(id)) {
                return encuesta;
            }
        }
        return null;
    }

    /**
     * Elimina la encuesta identificada por idEncuesta del usuario especificado.
     * <p>
     * Si se elimina la última encuesta del usuario, se elimina la entrada del usuario del mapa,
     * y retorna true. Si no se encuentra la encuesta, retorna false.
     * </p>
     *
     * @param usuario    el nombre del usuario
     * @param idEncuesta  el identificador de la encuesta a eliminar
     * @return true si la encuesta se eliminó y, si era la única, se removió la entrada del usuario; false en caso contrario
     */
    public boolean eliminarEncuesta (String usuario, String idEncuesta) {
        List<Survey> encuestas = this.encuestas.get(usuario);
        if (encuestas != null) {
            encuestas.removeIf(encuesta -> encuesta.getSURVEY_ID().equals(idEncuesta));
            if (encuestas.isEmpty()) {
                this.encuestas.remove(usuario);
                return true;
            }
        }
        return false;
    }
    /**
     * Registra un nuevo usuario.
     * <p>
     * Retorna false si ya existe un usuario con el mismo nombre.
     * </p>
     *
     * @param usuario el usuario a registrar
     * @return true si el usuario se registró exitosamente, false si ya existía
     */
    public boolean registrarUsuario(Usuario usuario) {
        if (usuarios.containsKey(usuario.getNombre())) return false;
        usuarios.put(usuario.getNombre(), usuario);
        return true;
    }
    /**
     * Inicia sesión de usuario.
     * <p>
     * Retorna el Usuario si el nombre existe y la contraseña es correcta, de lo contrario, retorna null.
     * </p>
     *
     * @param nombre      el nombre del usuario
     * @param contrasena  la contraseña del usuario
     * @return el Usuario autenticado, o null si no se autenticó correctamente
     */
    public Usuario iniciarSesion(String nombre, String contrasena) {
        Usuario user = usuarios.get(nombre);
        if (user != null && user.getContrasena().equals(contrasena)) return user;
        return null;
    }
    /**
     * Reinicia los usuarios
     * <p>
     * Elimina todos los usuarios registrados.
     * </p>
     */
    public void reset() {
        usuarios.clear();
    }

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return una colección con todos los usuarios
     */
    public Collection<Usuario> getTodosLosUsuarios() {
        return usuarios.values();
    }

}

