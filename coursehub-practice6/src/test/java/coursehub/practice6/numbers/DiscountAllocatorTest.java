package coursehub.practice6.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscountAllocatorTest {

    private static List<BigDecimal> decimals(String... values) {
        return Arrays.stream(values).map(BigDecimal::new).toList();
    }

    private static BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void sumMatchesTheExpectedDiscountExactly() {
        List<BigDecimal> prices = decimals("10.00", "10.00", "10.00");
        List<BigDecimal> discounts = DiscountAllocator.allocate(new BigDecimal("10.00"), prices, 2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("10.00"), sum(discounts));
    }

    @Test
    void remainderGoesToTheEarliestItemWhenResidualsAreEqual() {
        List<BigDecimal> prices = decimals("10.00", "10.00", "10.00");
        List<BigDecimal> discounts = DiscountAllocator.allocate(new BigDecimal("10.00"), prices, 2, RoundingMode.HALF_UP);
        assertEquals(decimals("3.34", "3.33", "3.33"), discounts);
    }

    @Test
    void remainderGoesToTheLargestResidual() {
        List<BigDecimal> prices = decimals("1.00", "2.00", "4.00");
        List<BigDecimal> discounts = DiscountAllocator.allocate(new BigDecimal("1.00"), prices, 2, RoundingMode.DOWN);
        assertEquals(new BigDecimal("1.00"), sum(discounts));
        assertEquals(decimals("0.14", "0.29", "0.57"), discounts);
    }

    @Test
    void roundingUpIsCorrectedDownwards() {
        List<BigDecimal> prices = decimals("10.00", "10.00", "10.00");
        List<BigDecimal> discounts = DiscountAllocator.allocate(new BigDecimal("10.00"), prices, 2, RoundingMode.UP);
        assertEquals(new BigDecimal("10.00"), sum(discounts));
        assertEquals(decimals("3.33", "3.33", "3.34"), discounts);
    }

    @Test
    void resultIsDeterministicForEveryRoundingMode() {
        List<BigDecimal> prices = decimals("19.99", "29.90", "49.50", "5.00");
        BigDecimal discount = new BigDecimal("12.34");
        RoundingMode[] modes = {RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING, RoundingMode.FLOOR,
                RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN};
        for (RoundingMode mode : modes) {
            List<BigDecimal> first = DiscountAllocator.allocate(discount, prices, 2, mode);
            List<BigDecimal> second = DiscountAllocator.allocate(discount, prices, 2, mode);
            assertEquals(first, second);
            assertEquals(discount, sum(first), mode.name());
        }
    }

    @Test
    void zeroDiscountGivesZeroShares() {
        List<BigDecimal> discounts = DiscountAllocator.allocate(
                BigDecimal.ZERO, decimals("5.00", "7.00"), 2, RoundingMode.HALF_EVEN);
        assertEquals(decimals("0.00", "0.00"), discounts);
    }

    @Test
    void zeroScaleWorksForWholeCurrencyUnits() {
        List<BigDecimal> discounts = DiscountAllocator.allocate(
                new BigDecimal("100"), decimals("1000", "1000", "1000"), 0, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("100"), sum(discounts));
    }

    @Test
    void netPricesSubtractDiscounts() {
        List<BigDecimal> prices = decimals("10.00", "10.00", "10.00");
        List<BigDecimal> discounts = decimals("3.34", "3.33", "3.33");
        assertEquals(decimals("6.66", "6.67", "6.67"), DiscountAllocator.netPrices(prices, discounts));
    }

    @Test
    void invalidArgumentsAreRejected() {
        List<BigDecimal> prices = decimals("10.00", "10.00");
        assertThrows(IllegalArgumentException.class,
                () -> DiscountAllocator.allocate(new BigDecimal("1.001"), prices, 2, RoundingMode.HALF_UP));
        assertThrows(IllegalArgumentException.class,
                () -> DiscountAllocator.allocate(new BigDecimal("20.01"), prices, 2, RoundingMode.HALF_UP));
        assertThrows(IllegalArgumentException.class,
                () -> DiscountAllocator.allocate(new BigDecimal("-1.00"), prices, 2, RoundingMode.HALF_UP));
        assertThrows(IllegalArgumentException.class,
                () -> DiscountAllocator.allocate(BigDecimal.ONE, List.of(), 2, RoundingMode.HALF_UP));
        assertThrows(ArithmeticException.class,
                () -> DiscountAllocator.allocate(new BigDecimal("10.00"), decimals("10.00", "10.00", "10.00"), 2,
                        RoundingMode.UNNECESSARY));
    }
}
