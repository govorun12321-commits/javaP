package coursehub.practice6.numbers;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SampleStatsTest {

    @Test
    void basicStatisticsAreCalculated() {
        SampleStats stats = SampleStats.of(List.of(10, 20, 30, 40));
        assertEquals(4, stats.count());
        assertEquals(10, stats.min());
        assertEquals(40, stats.max());
        assertEquals(25.0, stats.mean());
        assertEquals(25.0, stats.median());
    }

    @Test
    void medianOfOddSizeIsTheMiddleValue() {
        assertEquals(30.0, SampleStats.of(List.of(50, 10, 30)).median());
    }

    @Test
    void meanRoundingDiffersByMethod() {
        SampleStats stats = SampleStats.of(List.of(1, 2, 2));
        assertEquals(1, stats.floorMean());
        assertEquals(2, stats.ceilMean());
        assertEquals(2, stats.roundedMean());
    }

    @Test
    void mathRoundGoesTowardPositiveInfinityOnHalves() {
        assertEquals(3, SampleStats.of(List.of(2, 3)).roundedMean());
        assertEquals(-2, SampleStats.of(List.of(-2, -3)).roundedMean());
    }

    @Test
    void valuesAreClampedToObservedBounds() {
        SampleStats stats = SampleStats.of(List.of(10, 50, 90));
        assertEquals(10, stats.clamp(-5));
        assertEquals(50, stats.clamp(50));
        assertEquals(90, stats.clamp(1000));
    }

    @Test
    void bucketsUseFloorSemanticsForNegativeValues() {
        assertEquals(0, SampleStats.bucketIndex(9, 10));
        assertEquals(1, SampleStats.bucketIndex(10, 10));
        assertEquals(-1, SampleStats.bucketIndex(-1, 10));
        assertEquals(-10, SampleStats.bucketStart(-1, 10));
        assertEquals(9, SampleStats.positionInBucket(-1, 10));
    }

    @Test
    void histogramCountsValuesPerBucket() {
        Map<Integer, Long> histogram = SampleStats.histogram(List.of(1, 5, 9, 10, 19, 25, -3), 10);
        assertEquals(Map.of(-10, 1L, 0, 3L, 10, 2L, 20, 1L), histogram);
    }

    @Test
    void largeValuesDoNotOverflowTheSum() {
        SampleStats stats = SampleStats.of(List.of(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals((double) Integer.MAX_VALUE, stats.mean());
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> SampleStats.of(List.of()));
        assertThrows(IllegalArgumentException.class, () -> SampleStats.of(null));
        assertThrows(IllegalArgumentException.class, () -> SampleStats.bucketIndex(1, 0));
    }
}
