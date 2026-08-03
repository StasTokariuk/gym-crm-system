package com.gym.crm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class WebConfigIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Web context loads all controllers")
    void contextLoads() {
        assertNotNull(wac);
        assertTrue(wac.containsBean("traineeController"));
        assertTrue(wac.containsBean("trainerController"));
        assertTrue(wac.containsBean("trainingController"));
        assertTrue(wac.containsBean("authController"));
    }

    @Test
    @DisplayName("Boot auto-configured ObjectMapper supports Java 8 date/time (jsr310)")
    void objectMapper_SupportsJavaTime() {
        assertNotNull(objectMapper);
        assertTrue(objectMapper.getRegisteredModuleIds().stream()
                .anyMatch(id -> id.toString().contains("jsr310")));
    }
}