import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.security.PasswordUtil;
import cncs.academy.ess.service.TodoUserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoUserServiceTest {

    private UserRepository userRepository;
    private TodoUserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new TodoUserService(userRepository);
    }

    @Test
    void login_shouldReturnValidJWTWhenCredentialsMatch() throws Exception {

        // Arrange
        String username = "john";
        String password = "password";

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        User user = new User(1, username, hash, salt, "Base");

        when(userRepository.findByUsername(username))
                .thenReturn(user);

        // Act
        String result = userService.login(username, password);

        // Assert 1 — Não deve ser null
        assertNotNull(result);

        // Assert 2 — JWT deve começar com Bearer
        assertTrue(result.startsWith("Bearer "));

        String token = result.substring(7);

        DecodedJWT decodedJWT = JWT.decode(token);

        assertEquals(username, decodedJWT.getClaim("username").asString());
        assertNotNull(decodedJWT.getExpiresAt());
    }
}