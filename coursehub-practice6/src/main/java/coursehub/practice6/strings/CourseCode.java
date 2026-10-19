package coursehub.practice6.strings;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CourseCode(String prefix, int number) {

    private static final Pattern PREFIX_PATTERN = Pattern.compile("[A-Z]{2,5}");
    private static final Pattern CODE_PATTERN = Pattern.compile("(?<prefix>[A-Z]{2,5})-(?<number>[0-9]{3})");

    public CourseCode {
        Objects.requireNonNull(prefix, "prefix");
        if (!PREFIX_PATTERN.matcher(prefix).matches()) {
            throw new IllegalArgumentException("Invalid course prefix: " + prefix);
        }
        if (number < 0 || number > 999) {
            throw new IllegalArgumentException("Course number must be in 0..999: " + number);
        }
    }

    public static Optional<CourseCode> tryParse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher matcher = CODE_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String prefix = matcher.group("prefix");
        int number = Integer.parseInt(matcher.group("number"));
        return Optional.of(new CourseCode(prefix, number));
    }

    public static CourseCode parse(String text) {
        return tryParse(text)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course code: \"" + text + "\""));
    }

    public static boolean isValid(String text) {
        return tryParse(text).isPresent();
    }

    public String text() {
        return String.format(Locale.ROOT, "%s-%03d", prefix, number);
    }

    @Override
    public String toString() {
        return text();
    }
}
