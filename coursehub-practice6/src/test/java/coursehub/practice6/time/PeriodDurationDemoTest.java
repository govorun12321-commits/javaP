package coursehub.practice6.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodDurationDemoTest {

    @Test
    void springForwardDayIsTwentyThreeHours() {
        PeriodDurationDemo.DayComparison comparison = PeriodDurationDemo.springForward();
        assertTrue(comparison.diverges());
        assertEquals(Duration.ofHours(23), comparison.elapsedForOneDay());
        assertEquals(12, comparison.afterOneDay().getHour());
        assertEquals(13, comparison.afterTwentyFourHours().getHour());
    }

    @Test
    void fallBackDayIsTwentyFiveHours() {
        PeriodDurationDemo.DayComparison comparison = PeriodDurationDemo.fallBack();
        assertTrue(comparison.diverges());
        assertEquals(Duration.ofHours(25), comparison.elapsedForOneDay());
        assertEquals(12, comparison.afterOneDay().getHour());
        assertEquals(11, comparison.afterTwentyFourHours().getHour());
    }

    @Test
    void ordinaryDayAndFixedOffsetDoNotDiverge() {
        assertFalse(PeriodDurationDemo.ordinaryDay().diverges());
        assertFalse(PeriodDurationDemo.fixedOffsetZone().diverges());
        assertEquals(Duration.ofHours(24), PeriodDurationDemo.ordinaryDay().elapsedForOneDay());
    }

    @Test
    void withoutZoneTheyAreIndistinguishable() {
        assertTrue(PeriodDurationDemo.sameWithoutZone());
    }

    @Test
    void bothResultsAreTheSameLocalOrInstantMeaning() {
        PeriodDurationDemo.DayComparison comparison = PeriodDurationDemo.springForward();
        ZonedDateTime start = comparison.start();
        assertEquals(start.plusDays(1), comparison.afterOneDay());
        assertEquals(start.toInstant().plus(Duration.ofHours(24)), comparison.afterTwentyFourHours().toInstant());
    }

    @Test
    void subscriptionTermIsCalendarBased() {
        assertEquals(LocalDate.of(2026, 2, 28),
                PeriodDurationDemo.subscriptionEnd(LocalDate.of(2026, 1, 31), Period.ofMonths(1)));
        assertEquals(LocalDate.of(2028, 2, 29),
                PeriodDurationDemo.subscriptionEnd(LocalDate.of(2027, 2, 28), Period.ofYears(1).plusDays(1)));
        assertEquals(LocalDate.of(2027, 2, 28),
                PeriodDurationDemo.subscriptionEnd(LocalDate.of(2024, 2, 29), Period.ofYears(3)));
    }

    @Test
    void timeoutIsMeasuredInExactTime() {
        Instant started = Instant.parse("2026-03-28T10:00:00Z");
        Duration timeout = Duration.ofHours(24);
        assertEquals(Instant.parse("2026-03-29T10:00:00Z"), PeriodDurationDemo.timeoutDeadline(started, timeout));
        assertFalse(PeriodDurationDemo.timedOut(started, timeout, Instant.parse("2026-03-29T09:59:59Z")));
        assertTrue(PeriodDurationDemo.timedOut(started, timeout, Instant.parse("2026-03-29T10:00:00Z")));
    }
}
