package coursehub.practice6.time;

public final class InvalidDateFieldException extends IllegalArgumentException {

    private final String fieldName;
    private final String rawValue;

    public InvalidDateFieldException(String fieldName, String rawValue, String reason) {
        super(message(fieldName, rawValue, reason));
        this.fieldName = fieldName;
        this.rawValue = rawValue;
    }

    public InvalidDateFieldException(String fieldName, String rawValue, Throwable cause) {
        super(message(fieldName, rawValue, cause.getMessage()), cause);
        this.fieldName = fieldName;
        this.rawValue = rawValue;
    }

    public String fieldName() {
        return fieldName;
    }

    public String rawValue() {
        return rawValue;
    }

    private static String message(String fieldName, String rawValue, String reason) {
        String shownValue = rawValue == null ? "null" : "\"" + rawValue + "\"";
        return "Invalid date in field '" + fieldName + "': " + shownValue + " (" + reason + ")";
    }
}
