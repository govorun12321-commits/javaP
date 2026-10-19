package coursehub.practice6.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalDateTimeResolverTest {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    private static final LocalDateTime GAP = LocalDateTime.of(2026, 3, 29, 3, 30);
    private static final LocalDateTime OVERLAP = LocalDateTime.of(2026, 10, 25, 3, 30);
    private static final LocalDateTime ORDINARY = LocalDateTime.of(2026, 6, 10, 12, 0);

    @Test
    void gapAndOverlapAreDetected() {
        assertTrue(LocalDateTimeResolver.isGap(GAP, RIGA));
        assertFalse(LocalDateTimeResolver.isOverlap(GAP, RIGA));
        assertTrue(LocalDateTimeResolver.isOverlap(OVERLAP, RIGA));
        assertFalse(LocalDateTimeResolver.isGap(OVERLAP, RIGA));
        assertFalse(LocalDateTimeResolver.isGap(ORDINARY, RIGA));
        assertFalse(LocalDateTimeResolver.isOverlap(ORDINARY, RIGA));
    }

    @Test
    void transitionLengthIsOneHour() {
        assertEquals(Duration.ofHours(1), LocalDateTimeResolver.transitionLength(GAP, RIGA));
        assertEquals(Duration.ofHours(1), LocalDateTimeResolver.transitionLength(OVERLAP, RIGA));
        assertEquals(Duration.ZERO, LocalDateTimeResolver.transitionLength(ORDINARY, RIGA));
    }

    @Test
    void lenientCreationSilentlyShiftsNonexistentTime() {
        ZonedDateTime created = LocalDateTimeResolver.createLeniently(GAP, RIGA);
        assertEquals(LocalDateTime.of(2026, 3, 29, 4, 30), created.toLocalDateTime());
        assertEquals(ZoneOffset.ofHours(3), created.getOffset());
    }

    @Test
    void strictResolutionRejectsNonexistentTime() {
        InvalidLocalTimeException exception =
                assertThrows(InvalidLocalTimeException.class, () -> LocalDateTimeResolver.resolveStrict(GAP, RIGA));
        assertEquals(GAP, exception.localTime());
        assertEquals(RIGA, exception.zone());
        assertTrue(exception.getMessage().contains("2026-03-29T03:30"));
        assertTrue(exception.getMessage().contains("Europe/Riga"));
    }

    @Test
    void strictResolutionKeepsOrdinaryTimeUnchanged() {
        ZonedDateTime resolved = LocalDateTimeResolver.resolveStrict(ORDINARY, RIGA);
        assertEquals(ORDINARY, resolved.toLocalDateTime());
    }

    @Test
    void overlapDefaultsToTheEarlierOffset() {
        ZonedDateTime earlier = LocalDateTimeResolver.resolveStrict(OVERLAP, RIGA);
        ZonedDateTime later = LocalDateTimeResolver.resolveAtLaterOverlap(OVERLAP, RIGA);
        assertEquals(ZoneOffset.ofHours(3), earlier.getOffset());
        assertEquals(ZoneOffset.ofHours(2), later.getOffset());
        assertEquals(Duration.ofHours(1), Duration.between(earlier, later));
    }

    @Test
    void addingAnHourUsesExactTimeAcrossTheGap() {
        ZonedDateTime beforeGap = ZonedDateTime.of(2026, 3, 29, 2, 30, 0, 0, RIGA);
        ZonedDateTime result = LocalDateTimeResolver.plusOneHour(beforeGap);
        assertEquals(LocalDateTime.of(2026, 3, 29, 4, 30), result.toLocalDateTime());
        assertEquals(Duration.ofHours(1), Duration.between(beforeGap, result));
    }

    @Test
    void addingAnHourInsideOverlapMovesToTheSecondOccurrence() {
        ZonedDateTime first = LocalDateTimeResolver.resolveStrict(OVERLAP, RIGA);
        ZonedDateTime result = LocalDateTimeResolver.plusOneHour(first);
        assertEquals(OVERLAP, result.toLocalDateTime());
        assertEquals(ZoneOffset.ofHours(2), result.getOffset());
    }

    @Test
    void addingADayKeepsLocalTimeAndShiftsIntoGap() {
        ZonedDateTime dayBefore = ZonedDateTime.of(2026, 3, 28, 3, 30, 0, 0, RIGA);
        ZonedDateTime result = LocalDateTimeResolver.plusOneDay(dayBefore);
        assertEquals(LocalDateTime.of(2026, 3, 29, 4, 30), result.toLocalDateTime());
        assertEquals(Duration.ofHours(24), Duration.between(dayBefore, result));
    }

    @Test
    void addingADayAcrossFallBackTakesTwentyFiveHours() {
        ZonedDateTime start = ZonedDateTime.of(2026, 10, 24, 12, 0, 0, 0, RIGA);
        ZonedDateTime result = LocalDateTimeResolver.plusOneDay(start);
        assertEquals(LocalDateTime.of(2026, 10, 25, 12, 0), result.toLocalDateTime());
        assertEquals(Duration.ofHours(25), Duration.between(start, result));
    }
}
