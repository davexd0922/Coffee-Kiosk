package coffeeshopkiosk.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtil {

    private static final NumberFormat PESO = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    private MoneyUtil() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return PESO.format(BigDecimal.ZERO);
        }
        return PESO.format(amount);
    }
}
