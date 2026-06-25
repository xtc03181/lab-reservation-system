package com.gltu.labreservation.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtil {

    private static final String PREFIX = "SHA256:";
    private static final int MIN_LENGTH = 8;

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(PREFIX);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    public static boolean matches(String rawPassword, String savedPassword) {
        if (savedPassword == null) {
            return false;
        }
        if (savedPassword.startsWith(PREFIX)) {
            return hash(rawPassword).equals(savedPassword);
        }
        return savedPassword.equals(rawPassword);
    }

    public static boolean needsUpgrade(String savedPassword) {
        return savedPassword != null && !savedPassword.startsWith(PREFIX);
    }

    public static boolean isStrongEnough(String rawPassword) {
        return rawPassword != null
                && rawPassword.length() >= MIN_LENGTH
                && rawPassword.matches(".*[A-Z].*")
                && rawPassword.matches(".*[a-z].*")
                && rawPassword.matches(".*\\d.*")
                && rawPassword.matches(".*[^A-Za-z0-9].*");
    }

    public static String strengthMessage() {
        return "密码至少 8 位，需包含大写字母、小写字母、数字和特殊字符";
    }
}
