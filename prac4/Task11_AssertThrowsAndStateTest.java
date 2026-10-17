import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/*
 * Задание 11. assertThrows и состояние после отказа
 *
 * Проверьте не только тип исключения, но сообщение/поля и неизменность объекта после
 * неуспешной команды. Не помещайте в lambda больше одного потенциально падающего действия.
 *
 * Краткое объяснение:
 *  - assertThrows возвращает исключение: проверяем сообщение (на ключевые факты, а не на весь текст)
 *    или поля собственного исключения (QuotaExceededException.requested/available).
 *  - После отказа проверяем, что объект НЕ изменился (отказ должен быть атомарным).
 *  - Arrange выполняется вне lambda; в lambda ровно один вызов — иначе непонятно, какое
 *    именно действие бросило исключение, и тест может «пройти» по неправильной причине.
 */

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Task11_AssertThrowsAndStateTest {

    static final class QuotaExceededException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final int requested;
        private final int available;

        QuotaExceededException(int requested, int available) {
            super("Quota exceeded");
            this.requested = requested;
            this.available = available;
        }

        int requested() { return requested; }
        int available() { return available; }
    }

    static final class SeatQuota {
        private int available;

        SeatQuota(int available) {
            this.available = available;
        }

        void reserve(int seats) {
            if (seats > available) {
                throw new QuotaExceededException(seats, available);
            }
            available -= seats;
        }

        int available() { return available; }
    }

    @Test
    void completeHours_moreThanRemaining_throwsAndKeepsCourseUnchanged() {
        Course course = new Course(1, "Java", 10, 4);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> course.completeHours(7));

        assertTrue(ex.getMessage().contains("7"), "message should mention requested hours");
        assertTrue(ex.getMessage().contains("6"), "message should mention remaining hours");
        assertEquals(4, course.completedHours());
        assertEquals(6, course.remainingHours());
        assertFalse(course.isCompleted());
    }

    @ParameterizedTest(name = "completeHours({0}) -> IllegalArgumentException, состояние не меняется")
    @ValueSource(ints = {0, -1, -100})
    void completeHours_nonPositiveHours_throwsAndKeepsCourseUnchanged(int hours) {
        Course course = new Course(1, "Java", 10, 4);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> course.completeHours(hours));

        assertEquals("hours must be positive", ex.getMessage());
        assertEquals(4, course.completedHours());
    }

    @Test
    void reserve_moreThanAvailable_exposesFieldsAndKeepsQuotaUnchanged() {
        SeatQuota quota = new SeatQuota(3);

        QuotaExceededException ex = assertThrows(QuotaExceededException.class, () -> quota.reserve(5));

        assertEquals(5, ex.requested());
        assertEquals(3, ex.available());
        assertEquals(3, quota.available());
    }
}
