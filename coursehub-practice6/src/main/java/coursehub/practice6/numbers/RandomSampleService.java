package coursehub.practice6.numbers;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class RandomSampleService {

    private static final String SEEDED_ALGORITHM = "L64X128MixRandom";

    private final RandomGenerator generator;

    public RandomSampleService(RandomGenerator generator) {
        this.generator = Objects.requireNonNull(generator, "generator");
    }

    public static RandomSampleService production() {
        return new RandomSampleService(new SecureRandom());
    }

    public static RandomSampleService seeded(long seed) {
        return new RandomSampleService(RandomGeneratorFactory.of(SEEDED_ALGORITHM).create(seed));
    }

    public List<Integer> scores(int count, int lowerInclusive, int upperExclusive) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative: " + count);
        }
        if (lowerInclusive >= upperExclusive) {
            throw new IllegalArgumentException("lowerInclusive must be less than upperExclusive");
        }

        List<Integer> scores = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            scores.add(generator.nextInt(lowerInclusive, upperExclusive));
        }
        return List.copyOf(scores);
    }

    public SampleStats statsFor(int count, int lowerInclusive, int upperExclusive) {
        return SampleStats.of(scores(count, lowerInclusive, upperExclusive));
    }

    public static void main(String[] args) {
        List<Integer> first = seeded(42).scores(10, 0, 101);
        List<Integer> second = seeded(42).scores(10, 0, 101);
        System.out.println("seed 42, run 1: " + first);
        System.out.println("seed 42, run 2: " + second);
        System.out.println("same: " + first.equals(second));
        SampleStats stats = SampleStats.of(first);
        System.out.println(stats + ", roundedMean=" + stats.roundedMean());
        System.out.println("production: " + production().scores(10, 0, 101));
    }
}
