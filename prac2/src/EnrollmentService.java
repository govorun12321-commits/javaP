package coursehub;

import java.util.HashSet;
import java.util.Set;


public final class EnrollmentService {
    private final Set<Long> enrolled = new HashSet<>();

    public void enroll(long studentId, Course course, boolean prerequisite, int seats)
            throws EnrollmentRejectedException {
        if (course == null) throw new IllegalArgumentException("course must not be null");
        if (enrolled.contains(studentId))
            throw new EnrollmentRejectedException(studentId, course.id(), EnrollmentRejectedException.ALREADY_ENROLLED);
        if (seats <= 0)
            throw new EnrollmentRejectedException(studentId, course.id(), EnrollmentRejectedException.NO_SEATS);
        if (!prerequisite)
            throw new EnrollmentRejectedException(studentId, course.id(), EnrollmentRejectedException.PREREQUISITE);
        enrolled.add(studentId);
    }
}
