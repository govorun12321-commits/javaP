package coursehub.practice6.numbers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DiscountAllocator {

    private static final int EXTRA_SCALE = 12;

    private DiscountAllocator() {
    }

    public static List<BigDecimal> allocate(BigDecimal totalDiscount,
                                            List<BigDecimal> prices,
                                            int scale,
                                            RoundingMode roundingMode) {
        Objects.requireNonNull(totalDiscount, "totalDiscount");
        Objects.requireNonNull(prices, "prices");
        Objects.requireNonNull(roundingMode, "roundingMode");
        if (prices.isEmpty()) {
            throw new IllegalArgumentException("prices must not be empty");
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale must not be negative: " + scale);
        }

        BigDecimal target = atScaleExactly(totalDiscount, scale);
        BigDecimal priceSum = sumOf(prices);
        if (priceSum.signum() == 0) {
            throw new IllegalArgumentException("sum of prices must be positive");
        }
        if (target.signum() < 0 || target.compareTo(priceSum) > 0) {
            throw new IllegalArgumentException("discount must be between zero and the sum of prices");
        }

        List<BigDecimal> shares = new ArrayList<>(prices.size());
        List<BigDecimal> residuals = new ArrayList<>(prices.size());
        BigDecimal roundedSum = BigDecimal.ZERO;

        for (BigDecimal price : prices) {
            BigDecimal exact = target.multiply(price).divide(priceSum, scale + EXTRA_SCALE, RoundingMode.HALF_EVEN);
            BigDecimal rounded = exact.setScale(scale, roundingMode);
            shares.add(rounded);
            residuals.add(exact.subtract(rounded));
            roundedSum = roundedSum.add(rounded);
        }

        long units = target.subtract(roundedSum).movePointRight(scale).longValueExact();
        distributeUnits(shares, residuals, units, scale);
        return List.copyOf(shares);
    }

    public static List<BigDecimal> netPrices(List<BigDecimal> prices, List<BigDecimal> discounts) {
        if (prices.size() != discounts.size()) {
            throw new IllegalArgumentException("prices and discounts must have the same size");
        }
        List<BigDecimal> result = new ArrayList<>(prices.size());
        for (int i = 0; i < prices.size(); i++) {
            result.add(prices.get(i).subtract(discounts.get(i)));
        }
        return List.copyOf(result);
    }

    private static void distributeUnits(List<BigDecimal> shares, List<BigDecimal> residuals, long units, int scale) {
        if (units == 0) {
            return;
        }
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < shares.size(); i++) {
            order.add(i);
        }

        Comparator<Integer> byResidual = Comparator.comparing(residuals::get);
        if (units > 0) {
            byResidual = byResidual.reversed();
        }
        order.sort(byResidual.thenComparingInt(Integer::intValue));

        BigDecimal unit = BigDecimal.ONE.movePointLeft(scale);
        BigDecimal step = units > 0 ? unit : unit.negate();
        long count = Math.abs(units);

        for (long k = 0; k < count; k++) {
            int index = order.get((int) (k % order.size()));
            shares.set(index, shares.get(index).add(step));
        }
    }

    private static BigDecimal atScaleExactly(BigDecimal value, int scale) {
        try {
            return value.setScale(scale, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Discount " + value.toPlainString() + " has more than " + scale + " decimal places", e);
        }
    }

    private static BigDecimal sumOf(List<BigDecimal> prices) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal price : prices) {
            Objects.requireNonNull(price, "price");
            if (price.signum() < 0) {
                throw new IllegalArgumentException("price must not be negative: " + price);
            }
            sum = sum.add(price);
        }
        return sum;
    }

    public static void main(String[] args) {
        List<BigDecimal> prices = List.of(new BigDecimal("10.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        List<BigDecimal> discounts = allocate(new BigDecimal("10.00"), prices, 2, RoundingMode.HALF_UP);
        System.out.println("discounts: " + discounts);
        System.out.println("net prices: " + netPrices(prices, discounts));
    }
}
