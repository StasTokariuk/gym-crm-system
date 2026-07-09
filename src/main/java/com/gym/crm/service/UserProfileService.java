package com.gym.crm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Set;
import java.util.function.Predicate;

@Component
public class UserProfileService {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${security.password.length:10}")
    private int passwordLength;

    public String buildUsername(String firstName, String lastName, Set<String> existingUsernames) {
        return buildUsername(firstName, lastName, existingUsernames::contains);
    }

    public String buildUsername(String firstName, String lastName, Predicate<String> existsChecker) {
        String base = firstName + "." + lastName;
        if (!existsChecker.test(base)) {
            return base;
        }
        int serial = 1;
        String candidate;
        do {
            candidate = base + serial;
            serial++;
        } while (existsChecker.test(candidate));
        return candidate;
    }

    public String generatePassword() {
        StringBuilder sb = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}