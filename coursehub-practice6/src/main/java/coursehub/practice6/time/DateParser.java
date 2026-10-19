package coursehub.practice6.time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

public final class DateParser {

    public static final String PATTERN = "dd.MM.uuuu";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(PATTERN).withResolverStyle(ResolverStyle.STRICT);

    private DateParser() {
    }

    public static LocalDate parse(String fieldName, String rawValue) {
        Objects.requireNonNull(fieldName, "fieldName");
        if (rawValue == null) {
            throw new InvalidDateFieldException(fieldName, null, "value is required");
        }
        try {
            return LocalDate.parse(rawValue, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFieldException(fieldName, rawValue, e);
        }
    }

    public static String format(LocalDate date) {
        return FORMATTER.format(date);
    }

    public static void main(String[] args) {
        String[] values = {"29.02.2024", "29.02.2025", "31.04.2026", "01.01.2026 ", "1.1.2026", "01.01.2026abc"};
        for (String value : values) {
            try {
                System.out.println("\"" + value + "\" -> " + parse("startDate", value));
            } catch (InvalidDateFieldException e) {
                System.out.println(e.getMessage() + " | cause: " + e.getCause().getClass().getSimpleName());
            }
        }
    }
}
