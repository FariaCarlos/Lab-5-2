package cncs.academy.ess.controller;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.security.JwtUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HandlerType;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.casbin.jcasbin.main.Enforcer;

public class AuthorizationMiddleware implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);
    private final UserRepository userRepository;
    private final Enforcer enforcer;

    public AuthorizationMiddleware(UserRepository userRepository, Enforcer enforcer) {
        this.userRepository = userRepository;
        this.enforcer = enforcer;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        // if method is OPTIONS bypass auth middleware
        if (ctx.method() == HandlerType.OPTIONS) {
            // Optionally: validate if it is a legitimate CORS preflight
            return;
        }

        // Allow unauthenticated requests to /user (register) and /login
        // Apenas login é público
        if (ctx.path().equals("/login") && ctx.method().name().equals("POST")) {
            return;
        }

        // Check if authorization header exists
        String authorizationHeader = ctx.header("Authorization");
        String path = ctx.path();
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("Authorization header is missing or invalid '{}' for path '{}'", authorizationHeader, path);
            throw new UnauthorizedResponse();
        }

        // Extract token from authorization header
        String token = authorizationHeader.substring(7); // Remove "Bearer "

        // Check if token is valid (perform authentication logic)
        String username;

        try {
            username = JwtUtil.validateToken(token);
        } catch (Exception e) {
            throw new UnauthorizedResponse();
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UnauthorizedResponse();
        }

        ctx.attribute("userId", user.getId());

        String subject = username;
        System.out.println("USERNAME = " + username);

        String object = ctx.path();
        String action = ctx.method().name();

        // Autorização com Casbin
        boolean allowed = enforcer.enforce(subject, object, action);

        if (!allowed) {
            throw new io.javalin.http.ForbiddenResponse("Access denied");
        }
    }
}

