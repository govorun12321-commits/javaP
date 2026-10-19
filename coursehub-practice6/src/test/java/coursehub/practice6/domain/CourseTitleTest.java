package coursehub.practice6.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseTitleTest {

    @Test
    void displayValueIsStrippedAndCollapsedButKeepsCase() {
        CourseTitle title = CourseTitle.of("  Java \t  Basics\n");
        assertEquals("Java Basics", title.displayValue());
        assertEquals("java basics", title.searchKey());
    }

    @Test
    void unicodeWhitespaceIsCollapsedToo() {
        CourseTitle title = CourseTitle.of("\u00A0Java\u2003\u2003Basics\u3000");
        assertEquals("Java Basics", title.displayValue());
        assertEquals("java basics", title.searchKey());
    }

    @Test
    void differentSourcesCanShareOneSearchKey() {
        CourseTitle first = CourseTitle.of("Java Basics");
        CourseTitle second = CourseTitle.of("  JAVA    basics ");
        assertNotEquals(first, second);
        assertNotEquals(first.displayValue(), second.displayValue());
        assertEquals(first.searchKey(), second.searchKey());
        assertTrue(first.sameSearchKey(second));
    }

    @Test
    void conflictsListEveryKeyWithSeveralDisplayValues() {
        Map<String, Set<String>> conflicts = CourseTitle.conflicts(
                List.of("Java Basics", "JAVA  basics", "SQL", "Stra\u00DFe", "STRASSE"));
        assertEquals(2, conflicts.size());
        assertEquals(Set.of("Java Basics", "JAVA basics"), conflicts.get("java basics"));
        assertEquals(Set.of("Stra\u00DFe", "STRASSE"), conflicts.get("strasse"));
    }

    @Test
    void identicalDisplayValuesAreNotConflicts() {
        assertTrue(CourseTitle.conflicts(List.of("Java Basics", " Java   Basics ")).isEmpty());
    }

    @Test
    void composedAndDecomposedLettersMatch() {
        CourseTitle composed = CourseTitle.of("Caf\u00E9");
        CourseTitle decomposed = CourseTitle.of("Cafe\u0301");
        assertTrue(composed.sameSearchKey(decomposed));
        assertNotEquals(composed.displayValue(), decomposed.displayValue());
    }

    @Test
    void fullWidthLettersMatchAsciiLetters() {
        assertTrue(CourseTitle.of("\uFF2A\uFF41\uFF56\uFF41").matches("java"));
    }

    @Test
    void searchUsesFixedLocaleSoTurkishDottedIDoesNotLeak() {
        java.util.Locale original = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
            assertEquals("title", CourseTitle.searchKey("TITLE"));
            assertEquals("title", CourseTitle.of("TITLE").searchKey());
        } finally {
            java.util.Locale.setDefault(original);
        }
    }

    @Test
    void queryMatchingUsesTheSamePipeline() {
        CourseTitle title = CourseTitle.of("Advanced Java Basics");
        assertTrue(title.matches("  ADVANCED   java basics "));
        assertTrue(title.containsQuery("JAVA"));
        assertFalse(title.containsQuery("Kotlin"));
    }

    @Test
    void blankTitleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> CourseTitle.of(""));
        assertThrows(IllegalArgumentException.class, () -> CourseTitle.of(" \u00A0\t "));
        assertThrows(NullPointerException.class, () -> CourseTitle.of(null));
    }
}
