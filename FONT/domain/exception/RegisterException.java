package domain.exception;

/**
 * Excepción lanzada cuando ocurre un error durante el registro de usuario.
 */
public class RegisterException extends RuntimeException {
    public RegisterException(String message) {
        super(message);
    }
}
