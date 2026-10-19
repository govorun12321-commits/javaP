package coursehub.practice6.domain;

import coursehub.practice6.time.LocalDateTimeResolver;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WebinarSeriesPlan {

    private final ZoneId organizerZone;
    private final LocalDate firstDate;
    private final LocalTime startTime;
    private final Period step;
    private final Duration lessonDuration;
    private final List<LocalDate> lessonDates;
    private final List<Instant> lessonStarts;

    public WebinarSeriesPlan(ZoneId organizerZone,
                             LocalDate firstDate,
                             LocalTime startTime,
                             Period step,
                             int lessonCount,
                             Duration lessonDuration) {
        this.organizerZone = Objects.requireNonNull(organizerZone, "organizerZone");
        this.firstDate = Objects.requireNonNull(firstDate, "firstDate");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.step = Objects.requireNonNull(step, "step");
        this.lessonDuration = Objects.requireNonNull(lessonDuration, "lessonDuration");
        if (lessonCount <= 0) {
            throw new IllegalArgumentException("lessonCount must be positive: " + lessonCount);
        }
        if (step.isZero() || step.isNegative()) {
            throw new IllegalArgumentException("step must be positive: " + step);
        }
        if (lessonDuration.isZero() || lessonDuration.isNegative()) {
            throw new IllegalArgumentException("lessonDuration must be positive: " + lessonDuration);
        }

        List<LocalDate> dates = new ArrayList<>(lessonCount);
        List<Instant> starts = new ArrayList<>(lessonCount);
        for (int index = 0; index < lessonCount; index++) {
            LocalDate date = firstDate.plus(step.multipliedBy(index));
            ZonedDateTime start = LocalDateTimeResolver.resolveStrict(LocalDateTime.of(date, startTime), organizerZone);
            dates.add(date);
            starts.add(start.toInstant());
        }
        this.lessonDates = List.copyOf(dates);
        this.lessonStarts = List.copyOf(starts);
    }

    public ZoneId organizerZone() {
        return organizerZone;
    }

    public Duration lessonDuration() {
        return lessonDuration;
    }

    public int lessonCount() {
        return lessonStarts.size();
    }

    public List<Instant> lessonStarts() {
        return lessonStarts;
    }

    public List<LocalDate> lessonDates() {
        return lessonDates;
    }

    public Instant lessonStart(int index) {
        return lessonStarts.get(index);
    }

    public Instant lessonEnd(int index) {
        return lessonStart(index).plus(lessonDuration);
    }

    public List<ZonedDateTime> lessonsIn(ZoneId viewerZone) {
        Objects.requireNonNull(viewerZone, "viewerZone");
        return lessonStarts.stream().map(start -> start.atZone(viewerZone)).toList();
    }

    public int remainingLessons(Instant moment) {
        Objects.requireNonNull(moment, "moment");
        int remaining = 0;
        for (Instant start : lessonStarts) {
            if (start.isAfter(moment)) {
                remaining++;
            }
        }
        return remaining;
    }

    public LocalDate lastLessonDate() {
        return lessonDates.get(lessonDates.size() - 1);
    }

    public boolean containsLeapDay() {
        return lessonDates.stream()
                .anyMatch(date -> date.getMonth() == Month.FEBRUARY && date.getDayOfMonth() == 29);
    }

    public LocalTime startTime() {
        return startTime;
    }

    public LocalDate firstDate() {
        return firstDate;
    }

    public Period step() {
        return step;
    }
}
