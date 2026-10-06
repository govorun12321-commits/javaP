package coursehub;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Task10CourseCodeTest {
    @ParameterizedTest(name = "[{index}] input={0}, expected={1}")
    @CsvSource({
        "' java-101 ', JAVA-101",
        "CS-007, CS-007",
        " ' ab-999 ', AB-999 "
    })
    void normalizesValidCodes(String input, String expected) {
        assertEquals(expected.trim(), Task10CourseCode.normalize(input));
    }

    @ParameterizedTest(name = "[{index}] invalid input={0}")
    @CsvSource({"JAVA101", "A-123", "ABC-12", "''"})
    void rejectsInvalidCodes(String input) {
        assertThrows(IllegalArgumentException.class, () -> Task10CourseCode.normalize(input));
    }
}
