package coursehub.practice6.numbers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public final class IntegerPolicy {

    private IntegerPolicy() {
    }

    public static int unboxUnsafely(Integer value) {
        return value;
    }

    public static int sumUnsafely(Integer left, Integer right) {
        return left + right;
    }

    public static int quantityUnsafely(Map<String, Integer> quantities, String key) {
        return quantities.get(key);
    }

    public static boolean sameReference(Integer left, Integer right) {
        return left == right;
    }

    public static boolean sameValue(Integer left, Integer right) {
        return Objects.equals(left, right);
    }

    public static int requireValue(Integer value, String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    public static boolean sameRequiredValue(Integer left, Integer right) {
        return requireValue(left, "left") == requireValue(right, "right");
    }

    public static int valueOrDefault(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    public static OptionalInt toOptional(Integer value) {
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    public static int quantityOrZero(Map<String, Integer> quantities, String key) {
        return valueOrDefault(quantities.get(key), 0);
    }

    public static Map<Integer, Boolean> identityResults(int... values) {
        Map<Integer, Boolean> results = new LinkedHashMap<>();
        for (int value : values) {
            Integer first = value;
            Integer second = value;
            results.put(value, sameReference(first, second));
        }
        return results;
    }

    public static void main(String[] args) {
        identityResults(100, 127, 128, 1000, -129)
                .forEach((value, same) -> System.out.println(value + ": == -> " + same));
        try {
            unboxUnsafely(null);
        } catch (NullPointerException e) {
            System.out.println("NPE on unboxing: " + e.getClass().getSimpleName());
        }
    }
}
