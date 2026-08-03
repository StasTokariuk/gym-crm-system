package com.gym.crm.security;

import com.gym.crm.facade.GymFacade;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private final GymFacade gymFacade;
    private final Counter authSuccessCounter;
    private final Counter authFailureCounter;

    public AuthenticationInterceptor(GymFacade gymFacade, MeterRegistry meterRegistry) {
        this.gymFacade = gymFacade;
        this.authSuccessCounter = Counter.builder("gym.authentication.attempts")
                .tag("result", "success")
                .description("Number of authentication attempts")
                .register(meterRegistry);
        this.authFailureCounter = Counter.builder("gym.authentication.attempts")
                .tag("result", "failure")
                .description("Number of authentication attempts")
                .register(meterRegistry);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Authenticating request: {} {}", method, path);

        if (path.equals("/api/trainees") && method.equalsIgnoreCase("POST")) {
            return true;
        }
        if (path.equals("/api/trainers") && method.equalsIgnoreCase("POST")) {
            return true;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            authFailureCounter.increment();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        try {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            String[] values = credentials.split(":", 2);
            if (values.length != 2) {
                authFailureCounter.increment();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization format");
                return false;
            }

            String username = values[0];
            String password = values[1];

            boolean authenticated = gymFacade.authenticateTrainee(username, password)
                    || gymFacade.authenticateTrainer(username, password);

            if (!authenticated) {
                log.warn("Authentication failed for user: {}", username);
                authFailureCounter.increment();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
                return false;
            }

            log.debug("User '{}' successfully authenticated for path: {}", username, path);
            authSuccessCounter.increment();
            request.setAttribute("authenticatedUser", username);
            return true;

        } catch (Exception e) {
            log.error("Authentication error for path: {}", path, e);
            authFailureCounter.increment();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
            return false;
        }
    }
}