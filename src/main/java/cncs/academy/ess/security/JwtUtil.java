package cncs.academy.ess.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "super_secret_key_change_this";
    private static final String ISSUER = "TodoList-API";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    public static String generateToken(String username) {

        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        return JWT.create()
                .withIssuer(ISSUER)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public static String validateToken(String token) {

        Algorithm algorithm = Algorithm.HMAC256(SECRET);

        return JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build()
                .verify(token)
                .getClaim("username")
                .asString();
    }
}