/*
 * Задание 10. Параметризованный тест
 *
 * Используйте `@ParameterizedTest` и `@CsvSource` либо `@MethodSource` для нормализации
 * `CourseCode` и недопустимых кодов. Display name должен показывать вход и ожидаемый результат.
 *
 * Краткое объяснение:
 *  CourseCode: обрезает пробелы, переводит в верхний регистр (Locale.ROOT — без «турецкой i»)
 *  и принимает формат "ABC-123" (2-6 латинских букв, дефис, 3 цифры).
 *  - @CsvSource: таблица «вход -> ожидаемая нормализация»; значения с пробелами в одинарных кавычках.
 *  - @NullSource + @ValueSource: недопустимые коды, включая null.
 *  - name = "..." выводит в отчёте вход и ожидаемый результат, а не просто [1], [2].
 */

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class Task10_ParameterizedCourseCodeTest {

    static final class CourseCode {
        private static final Pattern FORMAT = Pattern.compile("[A-Z]{2,6}-\\d{3}");
        private final String value;

        private CourseCode(String value) {
            this.value = value;
        }

        static CourseCode of(String raw) {
            if (raw == null) {
                throw new IllegalArgumentException("course code must not be null");
            }
            String normalized = raw.trim().toUpperCase(Locale.ROOT);
            if (!FORMAT.matcher(normalized).matches()) {
                throw new IllegalArgumentException("course code has invalid format");
            }
            return new CourseCode(normalized);
        }

        String value() {
            return value;
        }
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" -> \"{1}\"")
    @CsvSource({
            "java-101, JAVA-101",
            "'  java-101  ', JAVA-101",
            "Math-300, MATH-300",
            "'dB-007 ', DB-007",
            "JAVA-101, JAVA-101"
    })
    void of_validInput_returnsNormalizedCode(String raw, String expected) {
        CourseCode code = CourseCode.of(raw);

        assertEquals(expected, code.value());
    }

    @ParameterizedTest(name = "\"{0}\" -> IllegalArgumentException")
    @NullSource
    @ValueSource(strings = {
            "", "   ", "JAVA101", "J-101", "TOOLONGX-101", "JAVA-10", "JAVA-1011",
            "JAVA_101", "ЯВА-101", "JA VA-101"
    })
    void of_invalidInput_isRejected(String raw) {
        assertThrows(IllegalArgumentException.class, () -> CourseCode.of(raw));
    }
}
