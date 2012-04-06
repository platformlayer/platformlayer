package org.platformlayer.crypto;

public class SecureComparison {
    private static final int NOT_A_BYTE = 666;

    public static boolean startsWith(byte[] data, byte[] prefix) {
        // Avoid basic timing attacks
        int dataLength = data.length;
        int prefixLength = prefix.length;

        int score = 0;

        for (int i = 0; i < prefixLength; i++) {
            int dataItem = (i < dataLength) ? data[i] : NOT_A_BYTE;
            // byte prefixItem = (i < prefixLength) ? prefix[i] : 0;
            int prefixItem = prefix[i];

            if (dataItem != prefixItem) {
                score--;
            } else {
                score++;
            }
        }

        return score == prefixLength;
    }

    public static boolean equal(byte[] l, byte[] r) {
        // Avoid basic timing attacks
        int lLength = l.length;
        int rLength = r.length;

        int maxLength = Math.max(lLength, rLength);

        int score = 0;
        for (int i = 0; i < maxLength; i++) {
            byte lItem = (i < lLength) ? l[i] : 0;
            byte rItem = (i < rLength) ? r[i] : 0;

            if (lItem != rItem) {
                score--;
            } else {
                score++;
            }
        }

        return score == maxLength;
    }

    public static boolean equal(char[] l, char[] r) {
        // Avoid basic timing attacks
        int lLength = l.length;
        int rLength = r.length;

        int maxLength = Math.max(lLength, rLength);

        int score = 0;
        for (int i = 0; i < maxLength; i++) {
            char lItem = (i < lLength) ? l[i] : 0;
            char rItem = (i < rLength) ? r[i] : 0;

            if (lItem != rItem) {
                score--;
            } else {
                score++;
            }
        }

        return score == maxLength;
    }

    public static boolean equal(String l, String r) {
        return equal(l.toCharArray(), r.toCharArray());
    }
}
