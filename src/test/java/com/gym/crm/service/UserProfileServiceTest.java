package com.gym.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileServiceTest {

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService();
        ReflectionTestUtils.setField(userProfileService, "passwordLength", 10);
    }

    @Test
    void generatePassword_ShouldReturnStringOfConfiguredLength() {
        String password = userProfileService.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length(), "Password length should match the configured value");
    }

    @Test
    void generatePassword_ShouldContainOnlyAlphanumericCharacters() {
        String password = userProfileService.generatePassword();

        assertTrue(password.matches("^[a-zA-Z0-9]+$"), "Password should only contain letters and numbers");
    }

    @Test
    void buildUsername_ShouldReturnStandardUsername_WhenNoCollisions() {
        String firstName = "John";
        String lastName = "Doe";
        Set<String> existingUsernames = Set.of("Alice.Smith", "Bob.Johnson");

        String username = userProfileService.buildUsername(firstName, lastName, existingUsernames);

        assertEquals("John.Doe", username, "Should return base username when it does not exist");
    }

    @Test
    void buildUsername_ShouldAppendNumber_WhenBaseUsernameExists() {
        String firstName = "John";
        String lastName = "Doe";
        Set<String> existingUsernames = Set.of("John.Doe");

        String username = userProfileService.buildUsername(firstName, lastName, existingUsernames);

        assertEquals("John.Doe1", username, "Should append 1 when base username is taken");
    }

    @Test
    void buildUsername_ShouldAppendNextAvailableNumber_WhenMultipleCollisionsExist() {
        String firstName = "John";
        String lastName = "Doe";
        Set<String> existingUsernames = Set.of("John.Doe", "John.Doe1", "John.Doe2");

        String username = userProfileService.buildUsername(firstName, lastName, existingUsernames);

        assertEquals("John.Doe3", username, "Should find the next available sequential number");
    }
}