package com.gym.crm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class, WebConfig.class})
@WebAppConfiguration
class WebConfigIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Web Application Context should load successfully")
    void contextLoads() {
        assertNotNull(wac);
        assertTrue(wac.containsBean("traineeController"));
        assertTrue(wac.containsBean("trainerController"));
        assertTrue(wac.containsBean("trainingController"));
        assertTrue(wac.containsBean("authController"));
    }

    @Test
    @DisplayName("ObjectMapper bean should be configured and support LocalDate module")
    void objectMapper_ConfiguredCorrectly() {
        assertNotNull(objectMapper);
        assertTrue(objectMapper.getRegisteredModuleIds().stream()
                .anyMatch(id -> id.toString().contains("jackson-datatype-jsr310")));
    }
}