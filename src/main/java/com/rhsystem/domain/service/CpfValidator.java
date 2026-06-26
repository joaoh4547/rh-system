package com.rhsystem.domain.service;

/**
 * Domain service for CPF validation (check-digit algorithm).
 */
public final class CpfValidator {

    private CpfValidator() {
    }

    /** Removes everything that is not a digit. */
    public static String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static boolean isValid(String cpf) {
        String c = digitsOnly(cpf);
        if (c.length() != 11) {
            return false;
        }
        // Reject sequences of identical digits (e.g. 00000000000)
        if (c.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int d1 = calculateDigit(c, 9, 10);
            int d2 = calculateDigit(c, 10, 11);
            return d1 == (c.charAt(9) - '0') && d2 == (c.charAt(10) - '0');
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static int calculateDigit(String cpf, int count, int initialWeight) {
        int sum = 0;
        int weight = initialWeight;
        for (int i = 0; i < count; i++) {
            sum += (cpf.charAt(i) - '0') * weight;
            weight--;
        }
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : 11 - remainder;
    }
}
