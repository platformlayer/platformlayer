package org.platformlayer.ops.crypto;

import java.security.SecureRandom;

import org.platformlayer.core.model.Secret;

public class Passwords {
    final SecureRandom random = new SecureRandom();

    public static final char[] ALPHANUMERIC_CASE_SENSITIVE;
    public static final char[] ALPHANUMERIC_CASE_INSENSITIVE;

    static {
        ALPHANUMERIC_CASE_INSENSITIVE = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        ALPHANUMERIC_CASE_SENSITIVE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    }

    public Secret generateRandomPassword(int length) {
        return generateRandomPassword(length, ALPHANUMERIC_CASE_SENSITIVE);
    }

    public synchronized Secret generateRandomPassword(int length, char[] universe) {
        char[] password = new char[length];
        for (int i = 0; i < length; i++) {
            password[i] = universe[random.nextInt(universe.length)];
        }
        return Secret.build(new String(password));
    }
}
