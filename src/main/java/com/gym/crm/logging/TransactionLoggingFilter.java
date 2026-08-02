package com.gym.crm.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("TransactionLoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        log.info("REST CALL START: {} {} | Client IP: {}", method, fullPath, httpRequest.getRemoteAddr());

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            log.info("REST CALL END: {} {} | Status: {} | Time Taken: {} ms", method, fullPath, status, duration);

            MDC.clear();
        }
    }

    @Override
    public void destroy() {
        log.info("TransactionLoggingFilter destroyed");
    }
}