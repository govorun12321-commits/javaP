package coursehub.practice6.strings;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseCodeTest {

    private static final List<String> VALID = List.of(
            "JAVA-101",
            "SQL-001",
            "GO-999",
            "KOTLN-250"
    );

    private static final List<String> INVALID = List.of(
            "",
            " ",
            "java-101",
            "Java-101",
            "JAVA101",
            "JAVA-10",
            "JAVA-1010",
            "J-101",
            "JAVAXX-101",
            " JAVA-101",
            "JAVA-101 ",
            "JAVA-101\n",
            "JAVA -101",
            "JAVA- 101",
            "JAVA-101-X",
            "XX JAVA-101",
            "JAVA\u2013101",
            "JAVA_101",
            "\u0408AVA-101",
            "JAVA-\u0661\u0660\u0661",
            "\uFF2A\uFF21\uFF36\uFF21-101",
            "\u00A0JAVA-101",
            "JAVA-101\u200B"
    );

    @Test
    void validCodesAreParsed() {
        for (String text : VALID) {
            assertTrue(CourseCode.isValid(text), text);
        }
    }

    @Test
    void invalidCodesAreRejected() {
        for (String text : INVALID) {
            assertFalse(CourseCode.isValid(text), "must be invalid: " + text);
        }
    }

    @Test
    void nullIsInvalid() {
        assertFalse(CourseCode.isValid(null));
        assertThrows(IllegalArgumentException.class, () -> CourseCode.parse(null));
    }

    @Test
    void groupsAreExtracted() {
        CourseCode code = CourseCode.parse("JAVA-101");
        assertEquals("JAVA", code.prefix());
        assertEquals(101, code.number());
    }

    @Test
    void leadingZerosAreKeptInText() {
        CourseCode code = CourseCode.parse("SQL-007");
        assertEquals(7, code.number());
        assertEquals("SQL-007", code.text());
    }

    @Test
    void partialMatchIsNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> CourseCode.parse("JAVA-101 and more"));
        assertThrows(IllegalArgumentException.class, () -> CourseCode.parse("prefix JAVA-101"));
    }

    @Test
    void recordValidatesItsOwnState() {
        assertThrows(IllegalArgumentException.class, () -> new CourseCode("java", 101));
        assertThrows(IllegalArgumentException.class, () -> new CourseCode("JAVA", 1000));
        assertThrows(IllegalArgumentException.class, () -> new CourseCode("JAVA", -1));
        assertEquals(new CourseCode("JAVA", 101), CourseCode.parse("JAVA-101"));
    }
}
