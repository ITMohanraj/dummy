package com.cropdeal.authservice.util;

import com.cropdeal.authservice.exception.WeakPasswordException;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    private PasswordValidator() {}

    public static void validate(String password) {
        if (password == null || !PATTERN.matcher(password).matches()) {
            throw new WeakPasswordException(
                "Password must contain at least 8 characters, including at least one uppercase letter, " +
                "one lowercase letter, one number, and one special character."
            );
        }
    }

    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }
}
