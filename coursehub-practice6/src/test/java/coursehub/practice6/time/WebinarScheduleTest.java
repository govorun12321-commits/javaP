package coursehub.practice6.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebinarScheduleTest {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private final WebinarSchedule schedule = new WebinarSchedule(
            "Java Time", Instant.parse("2026-11-05T22:30:00Z"), Duration.ofMinutes(90));

    @Test
    void sameInstantIsShownInThreeZones() {
        Map<ZoneId, ZonedDateTime> shown = schedule.startsInZones(List.of(RIGA, NEW_YORK, TOKYO));
        assertEquals(LocalDateTime.of(2026, 11, 6, 0, 30), shown.get(RIGA).toLocalDateTime());
        assertEquals(LocalDateTime.of(2026, 11, 5, 17, 30), shown.get(NEW_YORK).toLocalDateTime());
        assertEquals(LocalDateTime.of(2026, 11, 6, 7, 30), shown.get(TOKYO).toLocalDateTime());
        for (ZonedDateTime moment : shown.values()) {
            assertEquals(schedule.startsAt(), moment.toInstant());
        }
    }

    @Test
    void zonedValuesAreNotEqualButRepresentTheSameMoment() {
        ZonedDateTime riga = schedule.startsIn(RIGA);
        ZonedDateTime tokyo = schedule.startsIn(TOKYO);
        assertNotEquals(riga, tokyo);
        assertTrue(riga.isEqual(tokyo));
    }

    @Test
    void localDateDependsOnTheChosenZone() {
        assertEquals(LocalDate.of(2026, 11, 5), schedule.dateIn(ZoneId.of("UTC")));
        assertEquals(LocalDate.of(2026, 11, 6), schedule.dateIn(RIGA));
        assertEquals(LocalDate.of(2026, 11, 5), schedule.dateIn(NEW_YORK));
    }

    @Test
    void endIsExactDurationAfterStartInEveryZone() {
        assertEquals(Instant.parse("2026-11-06T00:00:00Z"), schedule.endsAt());
        assertEquals(Duration.ofMinutes(90), Duration.between(schedule.startsIn(TOKYO), schedule.endsIn(TOKYO)));
    }

    @Test
    void implicitDefaultZoneChangesTheBusinessDate() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));
            LocalDate inAuckland = schedule.dateInSystemDefaultZone();
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            LocalDate inLosAngeles = schedule.dateInSystemDefaultZone();
            assertEquals(LocalDate.of(2026, 11, 6), inAuckland);
            assertEquals(LocalDate.of(2026, 11, 5), inLosAngeles);
            assertNotEquals(inAuckland, inLosAngeles);
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void explicitOrganizerZoneGivesStableResult() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));
            LocalDate first = schedule.dateIn(RIGA);
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            LocalDate second = schedule.dateIn(RIGA);
            assertEquals(first, second);
        } finally {
            TimeZone.setDefault(original);
        }
    }

    @Test
    void organizerLocalTimeIsConvertedToInstant() {
        WebinarSchedule created = WebinarSchedule.ofOrganizerLocalTime(
                "Evening", LocalDateTime.of(2026, 11, 5, 18, 0), RIGA, Duration.ofHours(1));
        assertEquals(Instant.parse("2026-11-05T16:00:00Z"), created.startsAt());
    }

    @Test
    void nonexistentOrganizerTimeIsRejected() {
        assertThrows(InvalidLocalTimeException.class, () -> WebinarSchedule.ofOrganizerLocalTime(
                "Night", LocalDateTime.of(2026, 3, 29, 3, 30), RIGA, Duration.ofHours(1)));
    }

    @Test
    void invalidDurationIsRejected() {
        Instant moment = Instant.parse("2026-11-05T22:30:00Z");
        assertThrows(IllegalArgumentException.class, () -> new WebinarSchedule("x", moment, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new WebinarSchedule("x", moment, Duration.ofMinutes(-5)));
        assertFalse(schedule.duration().isNegative());
    }
}
