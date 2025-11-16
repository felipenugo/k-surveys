package domain.exception;

/**
 * Excepción lanzada cuando ocurre un error durante el inicio de sesión.
 */
public class LogInException extends RuntimeException {
    public LogInException(String message) {
        super(message);
    }
}
