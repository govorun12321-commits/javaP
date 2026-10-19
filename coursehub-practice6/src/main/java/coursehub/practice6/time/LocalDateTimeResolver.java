package coursehub.practice6.time;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;

public final class LocalDateTimeResolver {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");

    private LocalDateTimeResolver() {
    }

    public static boolean isGap(LocalDateTime local, ZoneId zone) {
        return zone.getRules().getValidOffsets(local).isEmpty();
    }

    public static boolean isOverlap(LocalDateTime local, ZoneId zone) {
        return zone.getRules().getValidOffsets(local).size() == 2;
    }

    public static Duration transitionLength(LocalDateTime local, ZoneId zone) {
        ZoneOffsetTransition transition = zone.getRules().getTransition(local);
        return transition == null ? Duration.ZERO : transition.getDuration().abs();
    }

    public static ZonedDateTime createLeniently(LocalDateTime local, ZoneId zone) {
        return ZonedDateTime.of(local, zone);
    }

    public static ZonedDateTime resolveStrict(LocalDateTime local, ZoneId zone) {
        if (isGap(local, zone)) {
            throw new InvalidLocalTimeException(local, zone);
        }
        return ZonedDateTime.of(local, zone).withEarlierOffsetAtOverlap();
    }

    public static ZonedDateTime resolveAtLaterOverlap(LocalDateTime local, ZoneId zone) {
        return resolveStrict(local, zone).withLaterOffsetAtOverlap();
    }

    public static ZonedDateTime plusOneHour(ZonedDateTime moment) {
        return moment.plusHours(1);
    }

    public static ZonedDateTime plusOneDay(ZonedDateTime moment) {
        return moment.plusDays(1);
    }

    public static void main(String[] args) {
        LocalDateTime gap = LocalDateTime.of(2026, 3, 29, 3, 30);
        LocalDateTime overlap = LocalDateTime.of(2026, 10, 25, 3, 30);

        System.out.println("gap: " + isGap(gap, RIGA) + ", length " + transitionLength(gap, RIGA));
        System.out.println("lenient creation: " + createLeniently(gap, RIGA));
        try {
            resolveStrict(gap, RIGA);
        } catch (InvalidLocalTimeException e) {
            System.out.println("strict: " + e.getMessage());
        }

        System.out.println("overlap earlier: " + resolveStrict(overlap, RIGA));
        System.out.println("overlap later:   " + resolveAtLaterOverlap(overlap, RIGA));

        ZonedDateTime beforeGap = ZonedDateTime.of(2026, 3, 29, 2, 30, 0, 0, RIGA);
        System.out.println("02:30 + 1 hour: " + plusOneHour(beforeGap));
        ZonedDateTime dayBefore = ZonedDateTime.of(2026, 3, 28, 3, 30, 0, 0, RIGA);
        System.out.println("03:30 day before + 1 day: " + plusOneDay(dayBefore));

        ZonedDateTime firstOccurrence = resolveStrict(overlap, RIGA);
        System.out.println("overlap + 1 hour: " + plusOneHour(firstOccurrence));
    }
}
