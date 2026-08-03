package com.gym.crm;

import com.gym.crm.facade.GymFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GymCrmApplicationTests {

    @Autowired
    private GymFacade facade;

    @Test
    @DisplayName("Spring Boot context loads and GymFacade bean is available")
    void contextLoads() {
        assertNotNull(facade);
    }

    @Test
    @DisplayName("Facade returns trainees list (storage initialized)")
    void facadeReturnsTrainees() {
        assertNotNull(facade.getAllTrainees());
    }
}