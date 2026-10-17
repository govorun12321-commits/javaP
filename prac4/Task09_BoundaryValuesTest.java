/*
 * Задание 9. Граничные значения
 *
 * Покройте продолжительность курса значениями вокруг границ: ниже минимума, минимум,
 * максимум, выше максимума. Объясните, почему только «обычный» пример не фиксирует контракт.
 *
 * Краткое объяснение:
 *  Допустимая длительность: 1..300 часов. Ошибки в условиях (< вместо <=, 100 вместо 300 —
 *  именно так было в исходном Course.java) проявляются только НА границе. «Обычный» пример
 *  (скажем, 32 часа) проходит и при >= 1 && <= 100, и при > 0 && < 300 — он не отличает
 *  правильный контракт от неправильного. Значения 0, 1, 300, 301 фиксируют, где ровно
 *  проходит граница. Аналогично проверена граница completedHours (0 .. durationHours).
 */

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Task09_BoundaryValuesTest {

    @Test
    void constructor_durationBelowMinimum_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "Java", Course.MIN_DURATION_HOURS - 1));
    }

    @Test
    void constructor_durationEqualToMinimum_isAccepted() {
        Course course = new Course(1, "Java", Course.MIN_DURATION_HOURS);

        assertEquals(1, course.durationHours());
    }

    @Test
    void constructor_durationEqualToMaximum_isAccepted() {
        Course course = new Course(1, "Java", Course.MAX_DURATION_HOURS);

        assertEquals(300, course.durationHours());
    }

    @Test
    void constructor_durationAboveMaximum_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "Java", Course.MAX_DURATION_HOURS + 1));
    }

    @Test
    void constructor_completedBelowZero_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "Java", 10, -1));
    }

    @Test
    void constructor_completedEqualToZero_isAccepted() {
        Course course = new Course(1, "Java", 10, 0);

        assertEquals(10, course.remainingHours());
    }

    @Test
    void constructor_completedEqualToDuration_meansCourseIsCompleted() {
        Course course = new Course(1, "Java", 10, 10);

        assertTrue(course.isCompleted());
    }

    @Test
    void constructor_completedAboveDuration_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "Java", 10, 11));
    }
}
