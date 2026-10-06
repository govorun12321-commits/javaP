package coursehub;


public class EnrollmentRejectedException extends Exception {
    public static final String ALREADY_ENROLLED = "ALREADY_ENROLLED";
    public static final String NO_SEATS = "NO_SEATS";
    public static final String PREREQUISITE = "PREREQUISITE";

    private final long studentId;
    private final long courseId;
    private final String reasonCode;

    public EnrollmentRejectedException(long studentId, long courseId, String reasonCode) {
        super("Enrollment rejected: " + reasonCode);
        this.studentId = studentId;
        this.courseId = courseId;
        this.reasonCode = reasonCode;
    }

    public EnrollmentRejectedException(long studentId, long courseId, String reasonCode, Throwable cause) {
        super("Enrollment rejected: " + reasonCode, cause);
        this.studentId = studentId;
        this.courseId = courseId;
        this.reasonCode = reasonCode;
    }

    public long studentId() { return studentId; }
    public long courseId() { return courseId; }
    public String reasonCode() { return reasonCode; }

    @Override public String toString() {
        return getClass().getSimpleName() + "{reasonCode='" + reasonCode + "'}";
    }
}
