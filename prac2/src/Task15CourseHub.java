package coursehub;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public final class Task15CourseHub {
    public enum ErrorCode { NOT_FOUND, INVALID_INPUT, ENROLLMENT_REJECTED, IMPORT_FAILED, INTERNAL }

    public record ApiError(ErrorCode code, String message) {}

    static Optional<Course> findCourse(long id) {
        return id > 0 ? Optional.of(new Course(id, "Java", 10)) : Optional.empty();
    }

    static ApiError importAndEnroll(Reader reader, long studentId) {
        try {
            Task14Importer.ImportResult result = Task14Importer.importCourses(reader);
            if (result.courses().isEmpty()) return new ApiError(ErrorCode.NOT_FOUND, "No courses");
            EnrollmentService service = new EnrollmentService();
            service.enroll(studentId, result.courses().get(0), true, 1);
            return null;
        } catch (CourseImportException | EnrollmentRejectedException e) {
            return new ApiError(
                e instanceof EnrollmentRejectedException ? ErrorCode.ENROLLMENT_REJECTED : ErrorCode.IMPORT_FAILED,
                e.getMessage()
            );
        }
    }

    public static void main(String[] args) {
        System.out.println(findCourse(0).orElse(null));
        System.out.println("Public codes: " + List.of(ErrorCode.values()));
    }
}
