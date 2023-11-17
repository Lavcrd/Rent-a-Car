package com.sda.carrental.global;

import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    public void retrieveSessionMessage(ModelMap map, HttpServletRequest req) {
        HttpSession session = req.getSession();
        String message = (String) session.getAttribute("message");
        if (message != null) {
            session.removeAttribute("message");
            map.addAttribute("message", message);
        }
    }
}
