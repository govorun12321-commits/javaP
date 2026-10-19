package coursehub.practice6.numbers;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomSampleServiceTest {

    @Test
    void sameSeedGivesTheSameSample() {
        List<Integer> first = RandomSampleService.seeded(42).scores(50, 0, 101);
        List<Integer> second = RandomSampleService.seeded(42).scores(50, 0, 101);
        assertEquals(first, second);
    }

    @Test
    void differentSeedsGiveDifferentSamples() {
        List<Integer> first = RandomSampleService.seeded(1).scores(50, 0, 101);
        List<Integer> second = RandomSampleService.seeded(2).scores(50, 0, 101);
        assertNotEquals(first, second);
    }

    @Test
    void valuesStayInsideHalfOpenBounds() {
        List<Integer> scores = RandomSampleService.seeded(7).scores(1000, 10, 20);
        assertEquals(1000, scores.size());
        for (int score : scores) {
            assertTrue(score >= 10 && score < 20, "out of bounds: " + score);
        }
    }

    @Test
    void anyRandomGeneratorCanBeInjected() {
        RandomSampleService first = new RandomSampleService(new Random(5));
        RandomSampleService second = new RandomSampleService(new Random(5));
        assertEquals(first.scores(20, 0, 10), second.scores(20, 0, 10));
    }

    @Test
    void statsAreReproducibleWithSeed() {
        SampleStats first = RandomSampleService.seeded(100).statsFor(200, 0, 101);
        SampleStats second = RandomSampleService.seeded(100).statsFor(200, 0, 101);
        assertEquals(first, second);
        assertTrue(first.min() >= 0);
        assertTrue(first.max() <= 100);
    }

    @Test
    void productionServiceRespectsBounds() {
        List<Integer> scores = RandomSampleService.production().scores(500, 0, 5);
        for (int score : scores) {
            assertTrue(score >= 0 && score < 5);
        }
    }

    @Test
    void invalidArgumentsAreRejected() {
        RandomSampleService service = RandomSampleService.seeded(1);
        assertThrows(IllegalArgumentException.class, () -> service.scores(-1, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.scores(5, 10, 10));
        assertThrows(NullPointerException.class, () -> new RandomSampleService(null));
    }
}
