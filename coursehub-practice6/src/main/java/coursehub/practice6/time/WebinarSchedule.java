package coursehub.practice6.time;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record WebinarSchedule(String title, Instant startsAt, Duration duration) {

    public WebinarSchedule {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(startsAt, "startsAt");
        Objects.requireNonNull(duration, "duration");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be positive: " + duration);
        }
    }

    public static WebinarSchedule ofOrganizerLocalTime(String title,
                                                       LocalDateTime localStart,
                                                       ZoneId organizerZone,
                                                       Duration duration) {
        Instant startsAt = LocalDateTimeResolver.resolveStrict(localStart, organizerZone).toInstant();
        return new WebinarSchedule(title, startsAt, duration);
    }

    public Instant endsAt() {
        return startsAt.plus(duration);
    }

    public ZonedDateTime startsIn(ZoneId zone) {
        return startsAt.atZone(zone);
    }

    public ZonedDateTime endsIn(ZoneId zone) {
        return endsAt().atZone(zone);
    }

    public LocalDate dateIn(ZoneId zone) {
        return startsIn(zone).toLocalDate();
    }

    public LocalDate dateInSystemDefaultZone() {
        return dateIn(ZoneId.systemDefault());
    }

    public Map<ZoneId, ZonedDateTime> startsInZones(List<ZoneId> zones) {
        Map<ZoneId, ZonedDateTime> result = new LinkedHashMap<>();
        for (ZoneId zone : zones) {
            result.put(zone, startsIn(zone));
        }
        return result;
    }

    public static void main(String[] args) {
        WebinarSchedule schedule = new WebinarSchedule(
                "Java Time", Instant.parse("2026-11-05T22:30:00Z"), Duration.ofMinutes(90));
        List<ZoneId> zones = List.of(ZoneId.of("Europe/Riga"), ZoneId.of("America/New_York"), ZoneId.of("Asia/Tokyo"));
        schedule.startsInZones(zones).forEach((zone, moment) -> System.out.println(zone + " -> " + moment));
        System.out.println("UTC date: " + schedule.dateIn(ZoneId.of("UTC")));
        System.out.println("Riga date: " + schedule.dateIn(ZoneId.of("Europe/Riga")));
    }
}
