package com.gym.crm.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionLoggingFilterTest {

    @InjectMocks
    private TransactionLoggingFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private FilterConfig filterConfig;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    @DisplayName("doFilter should generate transactionId, put in MDC, execute chain and clear MDC afterwards")
    void doFilter_ShouldManageMdcAndExecuteChain() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/trainees");
        when(request.getQueryString()).thenReturn("param=value");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getStatus()).thenReturn(200);

        doAnswer(invocation -> {
            String currentTxId = MDC.get("transactionId");
            assertNotNull(currentTxId, "TransactionId must be set in MDC during request processing");
            assertFalse(currentTxId.trim().isEmpty(), "TransactionId must not be empty");
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(MDC.get("transactionId"), "MDC must be cleared after filter execution completes");
    }

    @Test
    @DisplayName("init and destroy should execute without throwing exceptions")
    void initAndDestroy_ShouldExecuteSuccessfully() {
        assertDoesNotThrow(() -> filter.init(filterConfig));
        assertDoesNotThrow(() -> filter.destroy());
    }
}