package coursehub.practice6.domain;

import coursehub.practice6.time.InvalidLocalTimeException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebinarSeriesPlanTest {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private static WebinarSeriesPlan weeklyEveningPlan() {
        return new WebinarSeriesPlan(RIGA, LocalDate.of(2026, 3, 24), LocalTime.of(18, 0),
                Period.ofWeeks(1), 4, Duration.ofMinutes(90));
    }

    @Test
    void lessonsKeepOrganizerLocalTimeAcrossDst() {
        WebinarSeriesPlan plan = weeklyEveningPlan();
        List<ZonedDateTime> inRiga = plan.lessonsIn(RIGA);
        for (ZonedDateTime lesson : inRiga) {
            assertEquals(LocalTime.of(18, 0), lesson.toLocalTime());
        }
        assertEquals(Instant.parse("2026-03-24T16:00:00Z"), plan.lessonStart(0));
        assertEquals(Instant.parse("2026-03-31T15:00:00Z"), plan.lessonStart(1));
        assertEquals(Instant.parse("2026-04-07T15:00:00Z"), plan.lessonStart(2));
    }

    @Test
    void weekAcrossSpringForwardIsOneHourShorterInExactTime() {
        WebinarSeriesPlan plan = weeklyEveningPlan();
        assertEquals(Duration.ofHours(167), Duration.between(plan.lessonStart(0), plan.lessonStart(1)));
        assertEquals(Duration.ofHours(168), Duration.between(plan.lessonStart(1), plan.lessonStart(2)));
    }

    @Test
    void viewerInAnotherZoneSeesDifferentLocalTimesAfterOrganizerDst() {
        WebinarSeriesPlan plan = weeklyEveningPlan();
        List<ZonedDateTime> inNewYork = plan.lessonsIn(NEW_YORK);
        assertEquals(LocalTime.of(12, 0), inNewYork.get(0).toLocalTime());
        assertEquals(LocalTime.of(11, 0), inNewYork.get(1).toLocalTime());
        List<ZonedDateTime> inTokyo = plan.lessonsIn(TOKYO);
        assertEquals(LocalTime.of(1, 0), inTokyo.get(0).toLocalTime());
        assertEquals(LocalTime.of(0, 0), inTokyo.get(1).toLocalTime());
        assertEquals(LocalDate.of(2026, 3, 25), inTokyo.get(0).toLocalDate());
    }

    @Test
    void lessonEndUsesExactDuration() {
        WebinarSeriesPlan plan = weeklyEveningPlan();
        assertEquals(Instant.parse("2026-03-24T17:30:00Z"), plan.lessonEnd(0));
    }

    @Test
    void lessonThatAlreadyStartedIsNotRemaining() {
        WebinarSeriesPlan plan = weeklyEveningPlan();
        Instant firstStart = plan.lessonStart(0);
        assertEquals(4, plan.remainingLessons(firstStart.minusNanos(1)));
        assertEquals(3, plan.remainingLessons(firstStart));
        assertEquals(3, plan.remainingLessons(firstStart.plusNanos(1)));
        assertEquals(0, plan.remainingLessons(plan.lessonStart(3)));
        assertEquals(0, plan.remainingLessons(plan.lessonStart(3).plusSeconds(3600)));
    }

    @Test
    void monthlySeriesHitsLeapDayWithoutDrift() {
        WebinarSeriesPlan plan = new WebinarSeriesPlan(RIGA, LocalDate.of(2028, 1, 31), LocalTime.of(10, 0),
                Period.ofMonths(1), 3, Duration.ofHours(1));
        assertEquals(List.of(LocalDate.of(2028, 1, 31), LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 31)),
                plan.lessonDates());
        assertTrue(plan.containsLeapDay());
        assertEquals(LocalDate.of(2028, 3, 31), plan.lastLessonDate());
    }

    @Test
    void sameMonthlySeriesInCommonYearUsesFebruaryTwentyEighth() {
        WebinarSeriesPlan plan = new WebinarSeriesPlan(RIGA, LocalDate.of(2027, 1, 31), LocalTime.of(10, 0),
                Period.ofMonths(1), 3, Duration.ofHours(1));
        assertEquals(LocalDate.of(2027, 2, 28), plan.lessonDates().get(1));
        assertFalse(plan.containsLeapDay());
    }

    @Test
    void weeklySeriesCanIncludeLeapDay() {
        WebinarSeriesPlan plan = new WebinarSeriesPlan(RIGA, LocalDate.of(2028, 2, 22), LocalTime.of(10, 0),
                Period.ofDays(7), 3, Duration.ofHours(1));
        assertEquals(List.of(LocalDate.of(2028, 2, 22), LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 7)),
                plan.lessonDates());
        assertTrue(plan.containsLeapDay());
    }

    @Test
    void lessonInsideSpringForwardGapIsRejected() {
        InvalidLocalTimeException exception = assertThrows(InvalidLocalTimeException.class,
                () -> new WebinarSeriesPlan(RIGA, LocalDate.of(2026, 3, 22), LocalTime.of(3, 30),
                        Period.ofWeeks(1), 3, Duration.ofHours(1)));
        assertEquals(LocalDateTime.of(2026, 3, 29, 3, 30), exception.localTime());
    }

    @Test
    void lessonInsideFallBackOverlapUsesTheFirstOccurrence() {
        WebinarSeriesPlan plan = new WebinarSeriesPlan(RIGA, LocalDate.of(2026, 10, 18), LocalTime.of(3, 30),
                Period.ofWeeks(1), 2, Duration.ofHours(1));
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), plan.lessonStart(1));
    }

    @Test
    void invalidArgumentsAreRejected() {
        LocalDate date = LocalDate.of(2026, 3, 24);
        LocalTime time = LocalTime.of(18, 0);
        Duration duration = Duration.ofHours(1);
        assertThrows(IllegalArgumentException.class,
                () -> new WebinarSeriesPlan(RIGA, date, time, Period.ZERO, 3, duration));
        assertThrows(IllegalArgumentException.class,
                () -> new WebinarSeriesPlan(RIGA, date, time, Period.ofDays(-7), 3, duration));
        assertThrows(IllegalArgumentException.class,
                () -> new WebinarSeriesPlan(RIGA, date, time, Period.ofDays(7), 0, duration));
        assertThrows(IllegalArgumentException.class,
                () -> new WebinarSeriesPlan(RIGA, date, time, Period.ofDays(7), 3, Duration.ZERO));
    }
}
