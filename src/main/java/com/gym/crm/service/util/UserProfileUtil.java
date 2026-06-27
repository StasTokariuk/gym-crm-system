package com.gym.crm.service.util;

import java.security.SecureRandom;
import java.util.Set;

public final class UserProfileUtil {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private UserProfileUtil() {
    }


    public static String buildUsername(String firstName, String lastName, Set<String> existingUsernames) {
        String base = firstName + "." + lastName;
        if (!existingUsernames.contains(base)) {
            return base;
        }
        int serial = 1;
        String candidate;
        do {
            candidate = base + serial;
            serial++;
        } while (existingUsernames.contains(candidate));
        return candidate;
    }

    public static String generatePassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}