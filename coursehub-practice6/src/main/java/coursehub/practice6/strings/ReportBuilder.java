package coursehub.practice6.strings;

public final class ReportBuilder {

    private static final int DEFAULT_LINES = 10_000;

    public record Measurement(int lines, int length, long plusNanos, long builderNanos, boolean sameResult) {

        public double plusMillis() {
            return plusNanos / 1_000_000.0;
        }

        public double builderMillis() {
            return builderNanos / 1_000_000.0;
        }
    }

    public record Immutability(String original, String changed) {
    }

    private ReportBuilder() {
    }

    public static String buildWithPlus(int lines) {
        requireNonNegative(lines);
        String report = "";
        for (int number = 1; number <= lines; number++) {
            report = report + line(number);
        }
        return report;
    }

    public static String buildWithBuilder(int lines) {
        requireNonNegative(lines);
        StringBuilder builder = new StringBuilder(lines * 32);
        for (int number = 1; number <= lines; number++) {
            builder.append(line(number));
        }
        return builder.toString();
    }

    public static Measurement measure(int lines) {
        long plusStart = System.nanoTime();
        String viaPlus = buildWithPlus(lines);
        long plusNanos = System.nanoTime() - plusStart;

        long builderStart = System.nanoTime();
        String viaBuilder = buildWithBuilder(lines);
        long builderNanos = System.nanoTime() - builderStart;

        return new Measurement(lines, viaBuilder.length(), plusNanos, builderNanos, viaPlus.equals(viaBuilder));
    }

    public static Immutability immutability() {
        String original = "course";
        String changed = original.concat("-hub").toUpperCase();
        return new Immutability(original, changed);
    }

    static String line(int number) {
        return "Line " + number + ": JAVA-" + (100 + number % 900) + "\n";
    }

    private static void requireNonNegative(int lines) {
        if (lines < 0) {
            throw new IllegalArgumentException("lines must not be negative: " + lines);
        }
    }

    public static void main(String[] args) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            Measurement measurement = measure(DEFAULT_LINES);
            System.out.printf("attempt %d: plus=%.1f ms, builder=%.1f ms, same=%b, length=%d%n",
                    attempt, measurement.plusMillis(), measurement.builderMillis(),
                    measurement.sameResult(), measurement.length());
        }
        Immutability immutability = immutability();
        System.out.println("original=" + immutability.original() + ", changed=" + immutability.changed());
    }
}
