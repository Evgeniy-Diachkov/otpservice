package util;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String DIGITS = "0123456789";

    public static String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return code.toString();
    }
}
