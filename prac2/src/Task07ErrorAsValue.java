package coursehub;

import java.math.BigDecimal;
import java.util.Optional;


public final class Task07ErrorAsValue {
    record ParseResult<T>(T value, String errorCode) {
        boolean ok() { return errorCode == null; }
        static <T> ParseResult<T> ok(T value) { return new ParseResult<>(value, null); }
        static <T> ParseResult<T> error(String code) { return new ParseResult<>(null, code); }
    }

    static Optional<Course> findCourse(long id) {
        return id == 1 ? Optional.of(new Course(1, "Java", 10)) : Optional.empty();
    }

    static ParseResult<Course> parseCourse(String text) {
        if (text == null || text.isBlank()) return ParseResult.error("EMPTY");
        try { return ParseResult.ok(new Course(1, text, 10)); }
        catch (IllegalArgumentException e) { return ParseResult.error("INVALID"); }
    }

    static void enroll(Course c, boolean alreadyEnrolled) throws EnrollmentRejectedException {
        if (alreadyEnrolled)
            throw new EnrollmentRejectedException(10, c.id(), EnrollmentRejectedException.ALREADY_ENROLLED);
    }

    static BigDecimal calculatePrice(Course c) {
        if (c == null) throw new IllegalArgumentException("course must not be null");
        return BigDecimal.valueOf(c.durationHours()).multiply(BigDecimal.TEN);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(findCourse(2));                  // нормальное отсутствие
        System.out.println(parseCourse("Java").ok());       // typed result
        try { enroll(new Course(1, "Java", 10), true); } catch (EnrollmentRejectedException e) { System.out.println(e.reasonCode()); }
        System.out.println(calculatePrice(new Course(1, "Java", 10)));
    }
}
