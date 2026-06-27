package com.gym.crm.service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileUtilTest {

    @Test
    @DisplayName("Username without collisions = firstName.lastName")
    void buildUsername_noCollision() {
        String username = UserProfileUtil.buildUsername("John", "Smith", Set.of());
        assertEquals("John.Smith", username);
    }

    @Test
    @DisplayName("Username with a single collision gets suffix 1")
    void buildUsername_withCollision() {
        String username = UserProfileUtil.buildUsername("John", "Smith", Set.of("John.Smith"));
        assertEquals("John.Smith1", username);
    }

    @Test
    @DisplayName("Username with multiple collisions gets the next serial number")
    void buildUsername_multipleCollisions() {
        String username = UserProfileUtil.buildUsername("John", "Smith",
                Set.of("John.Smith", "John.Smith1", "John.Smith2"));
        assertEquals("John.Smith3", username);
    }

    @Test
    @DisplayName("Different last names are not treated as a collision")
    void buildUsername_differentLastName() {
        String username = UserProfileUtil.buildUsername("John", "Doe", Set.of("John.Smith"));
        assertEquals("John.Doe", username);
    }

    @Test
    @DisplayName("Password is exactly 10 characters long")
    void generatePassword_hasCorrectLength() {
        String password = UserProfileUtil.generatePassword();
        assertEquals(10, password.length());
    }

    @Test
    @DisplayName("Password contains only allowed characters")
    void generatePassword_containsOnlyAllowedChars() {
        String password = UserProfileUtil.generatePassword();
        assertTrue(Pattern.matches("[A-Za-z0-9]{10}", password));
    }

    @Test
    @DisplayName("Two generated passwords are most likely different")
    void generatePassword_isRandom() {
        String p1 = UserProfileUtil.generatePassword();
        String p2 = UserProfileUtil.generatePassword();
        // collision probability is negligible
        assertNotEquals(p1, p2);
    }
}