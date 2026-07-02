package com.gym.crm.config;

import com.gym.crm.facade.GymFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
class AppConfigIntegrationTest {

    @Autowired
    private GymFacade facade;

    @Test
    @DisplayName("Spring context loads the GymFacade bean")
    void contextLoadsFacade() {
        assertNotNull(facade);
    }

    @Test
    @DisplayName("StorageInitializer loads initial trainees from CSV file")
    void storageInitializerLoadsData() {
        assertFalse(facade.getAllTrainees().isEmpty());
    }
}