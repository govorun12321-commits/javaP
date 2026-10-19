package coursehub.practice6.numbers;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public record SampleStats(int count, int min, int max, double mean, double median) {

    public static SampleStats of(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("sample must not be empty");
        }

        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Integer value : values) {
            Objects.requireNonNull(value, "value");
            sum = Math.addExact(sum, value);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        List<Integer> sorted = values.stream().sorted().toList();
        int size = sorted.size();
        double median;
        if (size % 2 == 1) {
            median = sorted.get(size / 2);
        } else {
            median = ((long) sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        }

        return new SampleStats(size, min, max, (double) sum / size, median);
    }

    public long roundedMean() {
        return Math.round(mean);
    }

    public long floorMean() {
        return (long) Math.floor(mean);
    }

    public long ceilMean() {
        return (long) Math.ceil(mean);
    }

    public int clamp(int value) {
        return Math.clamp(value, min, max);
    }

    public static int bucketIndex(int value, int bucketSize) {
        requirePositive(bucketSize);
        return Math.floorDiv(value, bucketSize);
    }

    public static int bucketStart(int value, int bucketSize) {
        return Math.multiplyExact(bucketIndex(value, bucketSize), bucketSize);
    }

    public static int positionInBucket(int value, int bucketSize) {
        requirePositive(bucketSize);
        return Math.floorMod(value, bucketSize);
    }

    public static SortedMap<Integer, Long> histogram(List<Integer> values, int bucketSize) {
        requirePositive(bucketSize);
        SortedMap<Integer, Long> histogram = new TreeMap<>();
        for (Integer value : values) {
            histogram.merge(bucketStart(value, bucketSize), 1L, Long::sum);
        }
        return histogram;
    }

    private static void requirePositive(int bucketSize) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive: " + bucketSize);
        }
    }
}
