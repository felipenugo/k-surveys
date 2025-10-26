package data;

import java.util.HashMap;
import java.util.Map;

import domain.model.User;

public class UserRepository {
    private final Map<String, User> users;

    public UserRepository() {
        users = new HashMap<>();
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public void deleteUser(String username) {
        users.remove(username);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public void updateUser(String username, User updatedUser) {
        users.put(username, updatedUser);
    }

    public boolean existsUser(String username) {
        return users.containsKey(username);
    }

}
