package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.security.JwtUtil;
import cncs.academy.ess.security.PasswordUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TodoUserService {
    private final UserRepository repository;

    public TodoUserService(UserRepository userRepository) {
        this.repository = userRepository;
    }
    public User addUser(String username, String password) throws Exception {

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        // Por defeito todos os novos users são Base
        String role = "Base";

        User user = new User(username, hash, salt, role);

        int id = repository.save(user);
        user.setId(id);

        return user;
    }

    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    public String login(String username, String password) throws Exception {

        User user = repository.findByUsername(username);

        if (user == null) {
            return null;
        }

        boolean valid = PasswordUtil.verifyPassword(
                password,
                user.getSalt(),
                user.getPasswordHash()
        );

        if (!valid) {
            return null;
        }

        return createAuthToken(user);
    }


    private String createAuthToken(User user) {
        return "Bearer " + JwtUtil.generateToken(user.getUsername());
    }
}
