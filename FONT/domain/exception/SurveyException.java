package domain.exception;

/**
 * Excepción lanzada cuando ocurre un error en operaciones con encuestas.
 */
public class SurveyException extends RuntimeException {
    public SurveyException(String message) {
        super(message);
    }
}
