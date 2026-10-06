package coursehub;

import java.util.Locale;


public final class Task06ExceptionTranslation {
    static int parseDuration(String text) {
        return Integer.parseInt(text);
    }

    static int importDuration(int recordNumber, String text) throws CourseImportException {
        try {
            return parseDuration(text);
        } catch (NumberFormatException e) {
            throw new CourseImportException(recordNumber, "Invalid duration in record " + recordNumber, e);
        }
    }

    public static void main(String[] args) {
        try { importDuration(7, "abc"); }
        catch (CourseImportException e) {
            System.out.println(e.getMessage());
            System.out.println("cause = " + e.getCause().getClass().getSimpleName());
        }
    }
}
