package coursehub.practice6.numbers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class DecimalDemo {

    private DecimalDemo() {
    }

    public static BigDecimal fromDoubleConstructor() {
        return new BigDecimal(0.1);
    }

    public static BigDecimal fromValueOf() {
        return BigDecimal.valueOf(0.1);
    }

    public static BigDecimal fromString() {
        return new BigDecimal("0.1");
    }

    public static List<BigDecimal> coursePrices() {
        return List.of(new BigDecimal("49.90"), new BigDecimal("29.90"), new BigDecimal("19.90"));
    }

    public static BigDecimal totalOfThreeCourses() {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal price : coursePrices()) {
            total = total.add(price);
        }
        return total;
    }

    public static double totalOfThreeCoursesAsDouble() {
        return 49.90 + 29.90 + 19.90;
    }

    public static BigDecimal totalFromDoubleConstructor() {
        return new BigDecimal(49.90).add(new BigDecimal(29.90)).add(new BigDecimal(19.90));
    }

    public static BigDecimal totalFromValueOf() {
        return BigDecimal.valueOf(49.90).add(BigDecimal.valueOf(29.90)).add(BigDecimal.valueOf(19.90));
    }

    public static String describe(BigDecimal value) {
        return String.format(Locale.ROOT, "value=%s unscaled=%s scale=%d precision=%d",
                value.toPlainString(), value.unscaledValue(), value.scale(), value.precision());
    }

    public static boolean sameByEquals(String left, String right) {
        return new BigDecimal(left).equals(new BigDecimal(right));
    }

    public static boolean sameByCompareTo(String left, String right) {
        return new BigDecimal(left).compareTo(new BigDecimal(right)) == 0;
    }

    public static void main(String[] args) {
        System.out.println("new BigDecimal(0.1)        -> " + fromDoubleConstructor());
        System.out.println("BigDecimal.valueOf(0.1)    -> " + fromValueOf());
        System.out.println("new BigDecimal(\"0.1\")      -> " + fromString());
        System.out.println(describe(fromDoubleConstructor()));
        System.out.println(describe(fromValueOf()));
        System.out.println(describe(fromString()));
        System.out.println("double total               -> " + totalOfThreeCoursesAsDouble());
        System.out.println("BigDecimal(String) total   -> " + totalOfThreeCourses());
        System.out.println("BigDecimal(double) total   -> " + totalFromDoubleConstructor());
        System.out.println("BigDecimal.valueOf total   -> " + totalFromValueOf());
        System.out.println("2.0 equals 2.00            -> " + sameByEquals("2.0", "2.00"));
        System.out.println("2.0 compareTo 2.00         -> " + sameByCompareTo("2.0", "2.00"));
    }
}
