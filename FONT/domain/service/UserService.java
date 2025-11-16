package domain.service;

import domain.exception.LogInException;
import domain.exception.RegisterException;
import domain.model.User;
import data.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    private boolean isInputBlank(String text) { return text == null || text.trim().isEmpty(); }

    public void registerUser(String username, String email, String password) {
        if (isInputBlank(username)) throw new RegisterException("El campo nombre de usuario es obligatorio y no puede estar vacío.");
        if (isInputBlank(email)) throw new RegisterException("El campo email es obligatorio y no puede estar vacío.");
        if (isInputBlank(password)) throw new RegisterException("El campo contraseña es obligatorio y no puede estar vacío.");
        if (!email.contains("@gmail.com") && !email.contains("@fib.upc.edu")) throw new RegisterException("El formato del email debe terminar en @gmail.com o @fib.upc.edu.");
        if (userRepository.existsUser(username)) throw new RegisterException("El usuario " + username + " ya existe. Por favor escoge otro nombre de usuario.");
        User newUser = new User(username, email, password);
        userRepository.addUser(newUser);
    }

    public void verifyCredentials(String username, String password) {
        if (isInputBlank(username)) throw new LogInException("El campo nombre de usuario es obligatorio y no puede estar vacío.");
        if (isInputBlank(password)) throw new LogInException("El campo contraseña es obligatorio y no puede estar vacío");
        if (!userRepository.existsUser(username)) throw new LogInException("El usuario " + username + " no existe.");
        if (!userRepository.getUser(username).getPassword().equals(password)) throw new LogInException("La contraseña es incorrecta.");
    }

    public void addResponseId(String username, String surveyId, String responseId) { userRepository.addRespondedSurveyIdEntry(username, surveyId); userRepository.addResponseId(username, surveyId, responseId); }
    public void loginUser(String username, String password) {}
    public String logoutUser(String username) { return null; }
    public boolean deleteUser(String username) { if (!userRepository.existsUser(username)) return false; userRepository.deleteUser(username); return true; }
    public String updateUserEmail(String username, String oldEmail, String newEmail) { return null; }
    public String changePassword(String username, String email, String oldPassword, String newPassword) { return null; }
    public User getUser(String username) { return userRepository.getUser(username); }
}
