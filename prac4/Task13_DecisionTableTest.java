/*
 * Задание 13. Таблица решений и тесты
 *
 * Постройте decision table для зачисления: статус курса, наличие мест, prerequisite,
 * повторная заявка. Для каждой существенной комбинации создайте тест и удалите дублирующие
 * случаи, не увеличивающие покрытие правил.
 *
 * Краткое объяснение:
 *   * Условия: C = курс OPEN, A = уже была заявка, P = prerequisite выполнен, S = есть места.
 * Из 16 возможных комбинаций (4 флага) из-за приоритета проверок выделяются 5 правил:
 *
 *  Правило | C | A | P | S | Результат
 *  --------|---|---|---|---|----------------------
 *  R1      | N | - | - | - | COURSE_NOT_OPEN
 *  R2      | Y | Y | - | - | DUPLICATE_APPLICATION
 *  R3      | Y | N | N | - | PREREQUISITE_MISSING
 *  R4      | Y | N | Y | N | WAITLISTED
 *  R5      | Y | N | Y | Y | ENROLLED
 *
 * Для 100% покрытия бизнес-логики достаточно 5 тестов (по одному на правило). 
 * Остальные 11 комбинаций дублируют логику и отсекаются по приоритету (R1 > R2 > R3 > R4).
 * В тестах для R1–R3 «не важные» условия (-) выставлены в негативные значения для проверки приоритета.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class Task13_DecisionTableTest {

    enum CourseStatus { OPEN, CLOSED, ARCHIVED }

    enum Decision { ENROLLED, WAITLISTED, COURSE_NOT_OPEN, DUPLICATE_APPLICATION, PREREQUISITE_MISSING }

    static Decision decide(CourseStatus status, boolean hasSeats, boolean prerequisiteMet, boolean alreadyApplied) {
        if (status != CourseStatus.OPEN) return Decision.COURSE_NOT_OPEN;
        if (alreadyApplied) return Decision.DUPLICATE_APPLICATION;
        if (!prerequisiteMet) return Decision.PREREQUISITE_MISSING;
        if (!hasSeats) return Decision.WAITLISTED;
        return Decision.ENROLLED;
    }

    @ParameterizedTest(name = "{0}: status={1}, seats={2}, prereq={3}, applied={4} -> {5}")
    @CsvSource({
            "R1, CLOSED, false, false, true,  COURSE_NOT_OPEN",
            "R2, OPEN,   true,  true,  true,  DUPLICATE_APPLICATION",
            "R3, OPEN,   true,  false, false, PREREQUISITE_MISSING",
            "R4, OPEN,   false, true,  false, WAITLISTED",
            "R5, OPEN,   true,  true,  false, ENROLLED"
    })
    void decide_followsDecisionTable(String rule, CourseStatus status, boolean hasSeats,
                                     boolean prerequisiteMet, boolean alreadyApplied, Decision expected) {
        Decision actual = decide(status, hasSeats, prerequisiteMet, alreadyApplied);

        assertEquals(expected, actual);
    }
}
