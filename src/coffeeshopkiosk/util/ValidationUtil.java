package coffeeshopkiosk.util;

import java.math.BigDecimal;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static BigDecimal parseMoney(String value) {
        if (isBlank(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    public static int parseInt(String value) {
        if (isBlank(value)) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }
}
