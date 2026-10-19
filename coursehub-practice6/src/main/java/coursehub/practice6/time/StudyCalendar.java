package coursehub.practice6.time;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StudyCalendar {

    private StudyCalendar() {
    }

    public static List<LocalDate> moduleStartDates(LocalDate courseStart, int moduleCount, Period interval) {
        Objects.requireNonNull(courseStart, "courseStart");
        Objects.requireNonNull(interval, "interval");
        if (moduleCount <= 0) {
            throw new IllegalArgumentException("moduleCount must be positive: " + moduleCount);
        }
        if (interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("interval must be positive: " + interval);
        }

        List<LocalDate> starts = new ArrayList<>(moduleCount);
        for (int index = 0; index < moduleCount; index++) {
            starts.add(courseStart.plus(interval.multipliedBy(index)));
        }
        return List.copyOf(starts);
    }

    public static LocalDate lastStudyDay(LocalDate courseStart,
                                         int moduleCount,
                                         Period interval,
                                         int moduleLengthDays) {
        if (moduleLengthDays <= 0) {
            throw new IllegalArgumentException("moduleLengthDays must be positive: " + moduleLengthDays);
        }
        List<LocalDate> starts = moduleStartDates(courseStart, moduleCount, interval);
        LocalDate lastModuleStart = starts.get(starts.size() - 1);
        LocalDate nominalEnd = lastModuleStart.plusDays(moduleLengthDays - 1L);
        return previousWorkingDay(nominalEnd);
    }

    public static LocalDate previousWorkingDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.minusDays(1);
            case SUNDAY -> date.minusDays(2);
            default -> date;
        };
    }

    public static boolean startsInLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    public static boolean containsLeapDay(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must not be before from");
        }
        for (int year = from.getYear(); year <= to.getYear(); year++) {
            if (!Year.isLeap(year)) {
                continue;
            }
            LocalDate leapDay = LocalDate.of(year, Month.FEBRUARY, 29);
            if (!leapDay.isBefore(from) && !leapDay.isAfter(to)) {
                return true;
            }
        }
        return false;
    }

    public static LocalDate addMonthsIgnoringResult(LocalDate date, long months) {
        date.plusMonths(months);
        return date;
    }

    public static LocalDate addMonthsAssigningResult(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    public static void main(String[] args) {
        LocalDate start = LocalDate.of(2028, 1, 31);
        Period interval = Period.ofMonths(1);
        System.out.println("module starts: " + moduleStartDates(start, 4, interval));
        System.out.println("last study day: " + lastStudyDay(start, 4, interval, 14));
        System.out.println("leap day inside: " + containsLeapDay(start, LocalDate.of(2028, 4, 30)));
        System.out.println("ignored result: " + addMonthsIgnoringResult(start, 1));
        System.out.println("assigned result: " + addMonthsAssigningResult(start, 1));
    }
}
