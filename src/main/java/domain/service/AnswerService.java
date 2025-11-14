package domain.service;

import data.AnswerRepository;
import domain.controller.UserController;
import domain.model.User;
/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con
 * las respuestas individuales de las encuestas.
 *
 * En esta versión inicial, el servicio no implementa operaciones directas,
 * pero forma parte de la arquitectura general del sistema y está preparado
 * para ampliarse en futuras iteraciones.
 *
 * Su responsabilidad principal será:
 * <ul>
 *     <li>Validar permisos del usuario que responde.</li>
 *     <li>Gestionar operaciones sobre respuestas individuales
 *         (creación, actualización, eliminación o validación).</li>
 *     <li>Delegar en {@link AnswerRepository} la persistencia de datos.</li>
 * </ul>
 *
 * Aunque actualmente la lógica de respuestas está centralizada en
 * {@link domain.service.ResponseService}, esta clase representa el servicio
 * específico para la manipulación directa de objetos Answer, en caso de que
 * el sistema requiera una capa separada para esa responsabilidad.
 *
 * Las validaciones relacionadas con el estado de sesión se realizan mediante
 * {@link UserController}.
 */
public class AnswerService {
     /** Repositorio encargado de almacenar respuestas individuales. */
    private final AnswerRepository answerRepository;
    /** Controlador para verificar permisos y estado del usuario. */
    private final UserController userController;
    /**
     * Construye el servicio de respuestas individuales.
     *
     * @param answerRepository repositorio de respuestas
     * @param userController controlador utilizado para validar la sesión del usuario
     */
    public AnswerService(AnswerRepository answerRepository, UserController userController) {
        this.answerRepository = answerRepository;
        this.userController = userController;
    }
}
