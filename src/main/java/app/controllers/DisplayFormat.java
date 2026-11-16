package app.controllers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

final class DisplayFormat {

    private DisplayFormat() {
    }

    static String number(double value) {
        DecimalFormat format = new DecimalFormat(
                "0.####", DecimalFormatSymbols.getInstance(Locale.ROOT));
        return format.format(value);
    }
}
