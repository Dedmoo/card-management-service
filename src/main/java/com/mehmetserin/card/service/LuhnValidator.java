package com.mehmetserin.card.service;

public final class LuhnValidator {

    private LuhnValidator() {
    }

    public static boolean isValid(String pan) {
        if (pan == null) {
            return false;
        }
        String digits = pan.replaceAll("\\s", "");
        if (digits.length() < 12 || !digits.chars().allMatch(Character::isDigit)) {
            return false;
        }
        return checksum(digits) % 10 == 0;
    }

    /**
     * Computes the Luhn check digit for a partial PAN (without the check digit).
     */
    public static int checkDigit(String partialPan) {
        String digits = partialPan.replaceAll("\\s", "");
        if (!digits.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Partial PAN must contain digits only.");
        }
        int sum = checksumFromRight(digits, true);
        return (10 - (sum % 10)) % 10;
    }

    private static int checksum(String digits) {
        return checksumFromRight(digits, false);
    }

    private static int checksumFromRight(String digits, boolean doubleRightmost) {
        int sum = 0;
        boolean doubleIt = doubleRightmost;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum;
    }
}
