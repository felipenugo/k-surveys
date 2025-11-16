package domain.exception;

/**
 * Excepción lanzada cuando ocurre un error en operaciones con respuestas.
 */
public class ResponseException extends RuntimeException {
    public ResponseException(String message) {
        super(message);
    }
}
