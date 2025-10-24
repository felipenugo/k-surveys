package edu.upc.prop.clusterxx;

/**
 * Controlador de dominio encargado de gestionar la sesión de usuario en la aplicación.
 *
 * <p>Permite registrar nuevos usuarios, iniciar y cerrar sesión, así como obtener o establecer
 * el usuario actualmente activo. Esta clase actúa como intermediaria entre la lógica de dominio
 * y la capa de persistencia.</p>
 */
public class CtrlDominioUsuario {

    /**
     * Usuario actualmente en sesión. Si no hay ninguno, su valor es {@code null}.
     */
    private Usuario usuario;

    /**
     * Crea un nuevo controlador sin usuario en sesión.
     */
    public CtrlDominioUsuario() {
        usuario = null;
    }

    /**
     * Obtiene el usuario actualmente en sesión.
     *
     * @return El usuario en sesión, o {@code null} si no hay ninguno.
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Establece el usuario actualmente en sesión.
     *
     * @param usuario El usuario a establecer como sesión actual.
     * @throws IllegalArgumentException Si el parámetro {@code usuario} es {@code null}.
     */
    public void setUsuario(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("Usuario no puede ser null.");
        this.usuario = usuario;
    }

    /**
     * Registra un nuevo usuario en la capa de persistencia.
     *
     * <p>Comprueba que el nombre y las contraseñas no sean nulas ni vacías, y que ambas contraseñas coincidan.
     * Si los datos son válidos, se delega el registro a la capa de persistencia.</p>
     *
     * @param nombre      El nombre del nuevo usuario.
     * @param contrasena1 La contraseña introducida.
     * @param contrasena2 La confirmación de la contraseña.
     * @return {@code true} Si el registro fue exitoso; {@code false} si ocurrió un error de persistencia.
     * @throws IllegalArgumentException Si algún campo es inválido o las contraseñas no coinciden.
     */
    public boolean registrarUsuario(String nombre, String contrasena1, String contrasena2) {
        if (nombre == null || nombre.isEmpty()) throw new IllegalArgumentException("Nombre no puede ser null o estar vacío.");
        if (contrasena1 == null || contrasena1.isEmpty()) throw new IllegalArgumentException("Contraseña1 no puede ser null o estar vacía.");
        if (contrasena2 == null || contrasena2.isEmpty()) throw new IllegalArgumentException("Contraseña2 no puede ser null o estar vacía.");
        if (!contrasena1.equals(contrasena2)) throw new IllegalArgumentException("Las contraseñas no coinciden.");

        StubPersistencia stubPersistencia = StubPersistencia.getInstancia();
        return stubPersistencia.registrarUsuario(new Usuario(nombre, contrasena1));
    }

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     *
     * <p>Si el nombre y la contraseña coinciden con un usuario registrado,
     * lo establece como el usuario actual en sesión.</p>
     *
     * @param nombre     El nombre del usuario.
     * @param contrasena La contraseña del usuario.
     * @return {@code true} Si el inicio de sesión fue exitoso; {@code false} en caso contrario.
     * @throws IllegalArgumentException Si el nombre o la contraseña son nulos o están vacíos.
     */
    public boolean iniciarSesion(String nombre, String contrasena) {
        if (nombre == null || nombre.isEmpty()) throw new IllegalArgumentException("Nombre no puede ser null o estar vacío.");
        if (contrasena == null || contrasena.isEmpty()) throw new IllegalArgumentException("Contraseña no puede ser null o estar vacía.");

        StubPersistencia stubPersistencia = StubPersistencia.getInstancia();
        usuario = stubPersistencia.iniciarSesion(nombre, contrasena);
        return usuario != null;
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * <p>Si no hay ningún usuario en sesión, muestra un mensaje indicándolo.
     * En caso contrario, elimina al usuario de la sesión y muestra un mensaje de cierre.</p>
     */
    public void cerrarSesion() {
        usuario = null;
    }

    public String getNombreUsuario() {
        if(usuario != null) return usuario.getNombre();
        else return null;
    }
}
