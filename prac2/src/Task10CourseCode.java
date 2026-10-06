package coursehub;

import java.util.Locale;


public final class Task10CourseCode {
    public static String normalize(String code) {
        if (code == null) throw new IllegalArgumentException("code must not be null");
        String value = code.trim().toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z]{2}-\\d{3}")) throw new IllegalArgumentException("invalid course code");
        return value;
    }
}
