/*
 * Задание 7. Ошибка как значение или исключение
 *
 * Для `findCourse`, `parseCourse`, `enroll`, `calculatePrice` выберите `Optional`,
 * типизированный result, checked или unchecked exception. Реализуйте два контрастных
 * случая и защитите границу ожидаемого результата.
 *
 * Краткое объяснение (выбор):
 *
 * - findCourse -> Optional<Course> — «не найден» является штатным исходом, причина не требуется.
 * - parseCourse -> ParseResult (Ok / Invalid) — ошибка ввода ожидаема, вызывающему нужна причина.
 * - enroll -> Result / Checked Exception — возможны несколько разных ожидаемых бизнес-отказов.
 * - calculatePrice -> Unchecked Exception — неверная ставка является багом в коде, а не исходом.
 *
 * Реализованы два контрастных подхода: findCourse (значение) и calculatePrice (исключение).
 * Тесты проверяют: отсутствие курса возвращает Optional.empty(), а баг вызывающего сразу падает 
 * вместо возврата «магических» значений (null или -1).

 */

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Task07_ErrorAsValueOrExceptionTest {

    static final class CourseCatalog {
        private final Map<Long, Course> byId = new HashMap<>();

        void add(Course course) {
            byId.put(course.id(), course);
        }

        Optional<Course> findCourse(long id) {
            return Optional.ofNullable(byId.get(id));
        }
    }

    static final class PriceCalculator {
        static long calculatePrice(Course course, int ratePerHour) {
            java.util.Objects.requireNonNull(course, "course");
            if (ratePerHour <= 0) {
                throw new IllegalArgumentException("ratePerHour must be positive");
            }
            return (long) course.durationHours() * ratePerHour;
        }
    }

    @Test
    void findCourse_existingId_returnsCourse() {
        CourseCatalog catalog = new CourseCatalog();
        catalog.add(new Course(10, "Java", 32));

        Optional<Course> found = catalog.findCourse(10);

        assertTrue(found.isPresent());
        assertEquals("Java", found.get().title());
    }

    @ParameterizedTest(name = "findCourse({0}) -> empty, без исключения")
    @ValueSource(longs = {-1, 0, 999})
    void findCourse_unknownId_returnsEmptyInsteadOfThrowing(long id) {
        CourseCatalog catalog = new CourseCatalog();
        catalog.add(new Course(10, "Java", 32));

        Optional<Course> found = assertDoesNotThrow(() -> catalog.findCourse(id));

        assertTrue(found.isEmpty());
    }

    @Test
    void calculatePrice_validArguments_returnsDurationTimesRate() {
        Course course = new Course(10, "Java", 32);

        long price = PriceCalculator.calculatePrice(course, 10);

        assertEquals(320, price);
    }

    @ParameterizedTest(name = "calculatePrice(rate={0}) -> IllegalArgumentException")
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void calculatePrice_nonPositiveRate_throwsInsteadOfReturningMagicValue(int rate) {
        Course course = new Course(10, "Java", 32);

        assertThrows(IllegalArgumentException.class, () -> PriceCalculator.calculatePrice(course, rate));
    }

    @Test
    void calculatePrice_nullCourse_isProgrammingError() {
        assertThrows(NullPointerException.class, () -> PriceCalculator.calculatePrice(null, 10));
    }
}
