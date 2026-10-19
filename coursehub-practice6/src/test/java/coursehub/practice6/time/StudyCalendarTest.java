package coursehub.practice6.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyCalendarTest {

    @Test
    void monthlyModulesDoNotDriftAfterShortMonth() {
        List<LocalDate> starts = StudyCalendar.moduleStartDates(LocalDate.of(2027, 1, 31), 4, Period.ofMonths(1));
        assertEquals(List.of(
                LocalDate.of(2027, 1, 31),
                LocalDate.of(2027, 2, 28),
                LocalDate.of(2027, 3, 31),
                LocalDate.of(2027, 4, 30)), starts);
    }

    @Test
    void leapYearFebruaryHasTwentyNinthDay() {
        List<LocalDate> starts = StudyCalendar.moduleStartDates(LocalDate.of(2028, 1, 31), 3, Period.ofMonths(1));
        assertEquals(LocalDate.of(2028, 2, 29), starts.get(1));
        assertEquals(LocalDate.of(2028, 3, 31), starts.get(2));
    }

    @Test
    void weeklyModulesUseIntervalMultiples() {
        List<LocalDate> starts = StudyCalendar.moduleStartDates(LocalDate.of(2026, 9, 7), 3, Period.ofWeeks(2));
        assertEquals(List.of(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 21), LocalDate.of(2026, 10, 5)), starts);
    }

    @Test
    void lastStudyDayMovesFromWeekendToFriday() {
        LocalDate lastDay = StudyCalendar.lastStudyDay(LocalDate.of(2026, 9, 7), 3, Period.ofWeeks(2), 7);
        assertEquals(LocalDate.of(2026, 10, 9), lastDay);
    }

    @Test
    void lastStudyDayStaysOnWorkingDay() {
        LocalDate lastDay = StudyCalendar.lastStudyDay(LocalDate.of(2026, 9, 7), 1, Period.ofWeeks(1), 5);
        assertEquals(LocalDate.of(2026, 9, 11), lastDay);
    }

    @Test
    void previousWorkingDayHandlesSaturdayAndSunday() {
        assertEquals(LocalDate.of(2026, 10, 16), StudyCalendar.previousWorkingDay(LocalDate.of(2026, 10, 17)));
        assertEquals(LocalDate.of(2026, 10, 16), StudyCalendar.previousWorkingDay(LocalDate.of(2026, 10, 18)));
        assertEquals(LocalDate.of(2026, 10, 19), StudyCalendar.previousWorkingDay(LocalDate.of(2026, 10, 19)));
    }

    @Test
    void leapDayDetectionRespectsBoundaries() {
        assertTrue(StudyCalendar.containsLeapDay(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 2, 29)));
        assertFalse(StudyCalendar.containsLeapDay(LocalDate.of(2028, 3, 1), LocalDate.of(2028, 12, 31)));
        assertFalse(StudyCalendar.containsLeapDay(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31)));
        assertTrue(StudyCalendar.containsLeapDay(LocalDate.of(2027, 12, 1), LocalDate.of(2028, 3, 1)));
        assertFalse(StudyCalendar.containsLeapDay(LocalDate.of(2100, 1, 1), LocalDate.of(2100, 12, 31)));
        assertTrue(StudyCalendar.containsLeapDay(LocalDate.of(2000, 1, 1), LocalDate.of(2000, 12, 31)));
    }

    @Test
    void leapYearRulesAreCorrect() {
        assertTrue(StudyCalendar.startsInLeapYear(LocalDate.of(2028, 6, 1)));
        assertFalse(StudyCalendar.startsInLeapYear(LocalDate.of(2027, 6, 1)));
        assertFalse(StudyCalendar.startsInLeapYear(LocalDate.of(2100, 6, 1)));
        assertTrue(StudyCalendar.startsInLeapYear(LocalDate.of(2000, 6, 1)));
    }

    @Test
    void ignoredResultLeavesDateUnchanged() {
        LocalDate date = LocalDate.of(2028, 1, 31);
        assertEquals(date, StudyCalendar.addMonthsIgnoringResult(date, 1));
        assertEquals(LocalDate.of(2028, 2, 29), StudyCalendar.addMonthsAssigningResult(date, 1));
        assertEquals(LocalDate.of(2028, 1, 31), date);
    }

    @Test
    void invalidArgumentsAreRejected() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        assertThrows(IllegalArgumentException.class, () -> StudyCalendar.moduleStartDates(start, 0, Period.ofDays(7)));
        assertThrows(IllegalArgumentException.class, () -> StudyCalendar.moduleStartDates(start, 2, Period.ZERO));
        assertThrows(IllegalArgumentException.class, () -> StudyCalendar.moduleStartDates(start, 2, Period.ofDays(-7)));
        assertThrows(IllegalArgumentException.class, () -> StudyCalendar.lastStudyDay(start, 2, Period.ofDays(7), 0));
        assertThrows(IllegalArgumentException.class, () -> StudyCalendar.containsLeapDay(start, start.minusDays(1)));
    }
}
