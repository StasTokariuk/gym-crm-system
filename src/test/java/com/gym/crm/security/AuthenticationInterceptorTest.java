package com.gym.crm.security;

import com.gym.crm.facade.GymFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    @InjectMocks
    private AuthenticationInterceptor interceptor;

    @Mock
    private GymFacade gymFacade;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    @DisplayName("preHandle - Should bypass authentication for Trainee registration (POST /api/trainees)")
    void preHandle_BypassTraineeRegistration() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees");
        when(request.getMethod()).thenReturn("POST");

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Registration endpoint must bypass authentication");
        verifyNoInteractions(gymFacade, response);
    }

    @Test
    @DisplayName("preHandle - Should bypass authentication for Trainer registration (POST /api/trainers)")
    void preHandle_BypassTrainerRegistration() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainers");
        when(request.getMethod()).thenReturn("POST");

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Registration endpoint must bypass authentication");
        verifyNoInteractions(gymFacade, response);
    }

    @Test
    @DisplayName("preHandle - Should return 401 when Authorization header is missing")
    void preHandle_MissingHeader_ShouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result, "Should block request without auth header");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
    }

    @Test
    @DisplayName("preHandle - Should return 401 when Authorization header is not Basic")
    void preHandle_InvalidHeaderType_ShouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token123");

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result, "Should block non-Basic auth requests");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
    }

    @Test
    @DisplayName("preHandle - Should return 401 when credentials format is invalid")
    void preHandle_InvalidCredentialsFormat_ShouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");

        String base64Credentials = Base64.getEncoder().encodeToString("invalid_format".getBytes(StandardCharsets.UTF_8));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + base64Credentials);

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result, "Should block request with invalid credentials format");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization format");
    }

    @Test
    @DisplayName("preHandle - Should return true when Trainee successfully authenticates")
    void preHandle_SuccessfulTraineeAuth_ShouldReturnTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");

        String credentials = "John.Doe:password123";
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + base64Credentials);

        when(gymFacade.authenticateTrainee("John.Doe", "password123")).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Should allow request with correct Trainee credentials");
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("preHandle - Should return true when Trainer successfully authenticates")
    void preHandle_SuccessfulTrainerAuth_ShouldReturnTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainers/Mike.Brown");
        when(request.getMethod()).thenReturn("GET");

        String credentials = "Mike.Brown:password123";
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + base64Credentials);

        when(gymFacade.authenticateTrainee("Mike.Brown", "password123")).thenReturn(false);
        when(gymFacade.authenticateTrainer("Mike.Brown", "password123")).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result, "Should allow request with correct Trainer credentials");
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("preHandle - Should return 401 when authentication fails")
    void preHandle_FailedAuth_ShouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");

        String credentials = "John.Doe:wrong_password";
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + base64Credentials);

        when(gymFacade.authenticateTrainee("John.Doe", "wrong_password")).thenReturn(false);
        when(gymFacade.authenticateTrainer("John.Doe", "wrong_password")).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result, "Should block request with invalid credentials");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
    }

    @Test
    @DisplayName("preHandle - Should return 401 when exception occurs during decoding")
    void preHandle_DecodingException_ShouldReturn401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic invalid_base64_#$%");

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result, "Should block request if exception occurs");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
    }
}