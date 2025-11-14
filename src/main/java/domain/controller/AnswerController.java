package domain.controller;

import domain.service.AnswerService;
/**
 * Controlador responsable de gestionar las operaciones relacionadas con las respuestas
 * individuales de una encuesta.
 * 
 * Esta clase actúa como intermediario entre la capa de presentación y la lógica
 * definida en {@link AnswerService}. Aunque en la versión actual no expone métodos
 * adicionales, está diseñada para ampliarse en futuras funcionalidades, como la
 * creación, actualización o validación de respuestas concretas.
 * 
 * Forma parte de la arquitectura general de controladores del sistema, manteniendo
 * una separación clara entre la lógica de negocio y la interacción con el usuario.
 */
public class AnswerController {
    /** Servicio encargado de la lógica de negocio relacionada con las respuestas. */
    private final AnswerService answerService;
    /**
     * Crea una nueva instancia del controlador de respuestas.
     *
     * @param answerService servicio de respuestas utilizado para gestionar la lógica de negocio
     */
    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }
}
