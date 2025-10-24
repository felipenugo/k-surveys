package domain.model;


/**
 * Representa un usuario, incluyendo su nombre Y contraseña.
 */
public class Usuario {

    /**
     * Nombre de usuario.
     */
    private String nombre;

    /**
     * Contraseña del usuario.
     */
    private String contrasena;

    /**
     * Constructor por defecto. Inicializa el usuario con valores nulos y puntuación 0.
     */
    public Usuario() {
        nombre = null;
        contrasena = null;
    }

    /**
     * Crea un usuario con nombre y contraseña.
     *
     * @param nombre Nombre del usuario (no puede ser null ni vacío).
     * @param contrasena Contraseña del usuario (no puede ser null ni vacía).
     * @throws IllegalArgumentException Si algún parámetro es inválido.
     */
    public Usuario(String nombre, String contrasena) {
        if(nombre == null || nombre.isEmpty()) throw new IllegalArgumentException("El nombre no puede ser null o estar vacio.");
        if(contrasena == null || contrasena.isEmpty()) throw new IllegalArgumentException("La contraseña no puede ser null o estar vacia.");
        this.nombre = nombre;
        this.contrasena = contrasena;
    }

    /**
     * Devuelve el nombre del usuario.
     *
     * @return Nombre actual del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve la contraseña del usuario.
     *
     * @return Contraseña actual del usuario.
     */
    public String getContrasena() {
        return contrasena;
    }

    /**
     * Establece un nuevo nombre para el usuario.
     *
     * @param nombre Nuevo nombre (no puede ser null ni vacío).
     * @throws IllegalArgumentException Si el nombre es inválido.
     */
    public void setNombre(String nombre) {
        if(nombre == null || nombre.isEmpty()) throw new IllegalArgumentException("El nombre no puede ser null o estar vacio.");
        this.nombre = nombre;
    }

    /**
     * Establece una nueva contraseña para el usuario.
     *
     * @param contrasena Nueva contraseña (no puede ser null ni vacía).
     * @throws IllegalArgumentException Si la contraseña es inválida.
     */
    public void setContrasena(String contrasena) {
        if(contrasena == null || contrasena.isEmpty()) throw new IllegalArgumentException("La contraseña no puede ser null o estar vacia.");
        this.contrasena = contrasena;
    }

}
