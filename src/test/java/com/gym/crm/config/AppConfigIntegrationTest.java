package com.gym.crm.config;

import com.gym.crm.facade.GymFacade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigIntegrationTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("Spring context loads the GymFacade bean")
    void contextLoadsFacade() {
        GymFacade facade = context.getBean(GymFacade.class);
        assertNotNull(facade);
    }

    @Test
    @DisplayName("BeanPostProcessor loads initial trainees from CSV file")
    void storageInitializerLoadsData() {
        GymFacade facade = context.getBean(GymFacade.class);
        assertFalse(facade.getAllTrainees().isEmpty());
    }
}