package com.sda.carrental.global;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import java.util.Random;

@Component
public class Utility {
    public String generateRandomString(int length) {
        final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!#$%^&*()-_=+`~";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    public double valueToDouble(String value) {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMANY);
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        format.setDecimalFormatSymbols(symbols);

        try {
            return format.parse(value).doubleValue();
        } catch (ParseException ignored) {
            return 0.0;
        }
    }
}
