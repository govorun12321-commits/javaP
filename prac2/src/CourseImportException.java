package coursehub;


public class CourseImportException extends Exception {
    private final int recordNumber;
    public CourseImportException(int recordNumber, String message, Throwable cause) {
        super(message, cause);
        this.recordNumber = recordNumber;
    }
    public int recordNumber() { return recordNumber; }
}
