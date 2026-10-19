package coursehub.practice6.time;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class PeriodDurationDemo {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");

    public record DayComparison(ZonedDateTime start,
                                ZonedDateTime afterOneDay,
                                ZonedDateTime afterTwentyFourHours,
                                Duration elapsedForOneDay) {

        public boolean diverges() {
            return !afterOneDay.equals(afterTwentyFourHours);
        }
    }

    private PeriodDurationDemo() {
    }

    public static DayComparison compare(ZonedDateTime start) {
        ZonedDateTime afterOneDay = start.plus(Period.ofDays(1));
        ZonedDateTime afterTwentyFourHours = start.plus(Duration.ofHours(24));
        return new DayComparison(start, afterOneDay, afterTwentyFourHours, Duration.between(start, afterOneDay));
    }

    public static DayComparison springForward() {
        return compare(ZonedDateTime.of(2026, 3, 28, 12, 0, 0, 0, RIGA));
    }

    public static DayComparison fallBack() {
        return compare(ZonedDateTime.of(2026, 10, 24, 12, 0, 0, 0, RIGA));
    }

    public static DayComparison ordinaryDay() {
        return compare(ZonedDateTime.of(2026, 6, 10, 12, 0, 0, 0, RIGA));
    }

    public static DayComparison fixedOffsetZone() {
        return compare(ZonedDateTime.of(2026, 3, 28, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    public static boolean sameWithoutZone() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 28, 12, 0);
        return start.plus(Period.ofDays(1)).equals(start.plus(Duration.ofHours(24)));
    }

    public static LocalDate subscriptionEnd(LocalDate startDate, Period term) {
        return startDate.plus(term);
    }

    public static Instant timeoutDeadline(Instant startedAt, Duration timeout) {
        return startedAt.plus(timeout);
    }

    public static boolean timedOut(Instant startedAt, Duration timeout, Instant now) {
        return !now.isBefore(timeoutDeadline(startedAt, timeout));
    }

    public static void main(String[] args) {
        print("spring forward", springForward());
        print("fall back", fallBack());
        print("ordinary day", ordinaryDay());
        System.out.println("same without zone: " + sameWithoutZone());
        System.out.println("subscription end: " + subscriptionEnd(LocalDate.of(2026, 1, 31), Period.ofMonths(1)));
    }

    private static void print(String title, DayComparison comparison) {
        System.out.println(title + ":");
        System.out.println("  start       " + comparison.start());
        System.out.println("  +P1D        " + comparison.afterOneDay());
        System.out.println("  +PT24H      " + comparison.afterTwentyFourHours());
        System.out.println("  elapsed(P1D) " + comparison.elapsedForOneDay());
    }
}
