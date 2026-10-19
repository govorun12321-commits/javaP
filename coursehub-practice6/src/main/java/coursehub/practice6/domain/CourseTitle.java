package coursehub.practice6.domain;

import java.text.Normalizer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class CourseTitle {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Locale SEARCH_LOCALE = Locale.ROOT;

    private final String displayValue;
    private final String searchKey;

    private CourseTitle(String displayValue, String searchKey) {
        this.displayValue = displayValue;
        this.searchKey = searchKey;
    }

    public static CourseTitle of(String rawTitle) {
        Objects.requireNonNull(rawTitle, "rawTitle");
        String display = cleanWhitespace(rawTitle);
        if (display.isEmpty()) {
            throw new IllegalArgumentException("Course title must not be blank");
        }
        return new CourseTitle(display, searchKey(rawTitle));
    }

    public static String searchKey(String text) {
        Objects.requireNonNull(text, "text");
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC);
        String cleaned = cleanWhitespace(normalized);
        return cleaned.toUpperCase(SEARCH_LOCALE).toLowerCase(SEARCH_LOCALE);
    }

    public static Map<String, Set<String>> conflicts(Collection<String> rawTitles) {
        Map<String, Set<String>> grouped = new LinkedHashMap<>();
        for (String rawTitle : rawTitles) {
            CourseTitle title = of(rawTitle);
            grouped.computeIfAbsent(title.searchKey, key -> new LinkedHashSet<>()).add(title.displayValue);
        }
        grouped.values().removeIf(displayValues -> displayValues.size() < 2);
        return grouped;
    }

    public String displayValue() {
        return displayValue;
    }

    public String searchKey() {
        return searchKey;
    }

    public boolean matches(String query) {
        return searchKey.equals(searchKey(query));
    }

    public boolean containsQuery(String query) {
        return searchKey.contains(searchKey(query));
    }

    public boolean sameSearchKey(CourseTitle other) {
        return searchKey.equals(other.searchKey);
    }

    private static String cleanWhitespace(String text) {
        return WHITESPACE.matcher(text.strip()).replaceAll(" ").strip();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CourseTitle title)) {
            return false;
        }
        return displayValue.equals(title.displayValue);
    }

    @Override
    public int hashCode() {
        return displayValue.hashCode();
    }

    @Override
    public String toString() {
        return displayValue;
    }

    public static void main(String[] args) {
        CourseTitle first = of("  Java   Basics ");
        CourseTitle second = of("JAVA\u00A0basics");
        System.out.println("display 1: \"" + first.displayValue() + "\", key: \"" + first.searchKey() + "\"");
        System.out.println("display 2: \"" + second.displayValue() + "\", key: \"" + second.searchKey() + "\"");
        System.out.println("equal as titles: " + first.equals(second));
        System.out.println("same search key: " + first.sameSearchKey(second));
        System.out.println("conflicts: " + conflicts(java.util.List.of("Java Basics", "JAVA  basics", "Stra\u00DFe", "STRASSE")));
    }
}
