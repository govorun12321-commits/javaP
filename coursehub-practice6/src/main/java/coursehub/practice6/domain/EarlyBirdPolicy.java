package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

public record EarlyBirdPolicy(Instant deadline, BigDecimal percent, RoundingMode roundingMode) {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public EarlyBirdPolicy {
        Objects.requireNonNull(deadline, "deadline");
        Objects.requireNonNull(percent, "percent");
        Objects.requireNonNull(roundingMode, "roundingMode");
        if (percent.signum() < 0 || percent.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("percent must be between 0 and 100: " + percent);
        }
    }

    public static EarlyBirdPolicy none() {
        return new EarlyBirdPolicy(Instant.EPOCH, BigDecimal.ZERO, RoundingMode.HALF_UP);
    }

    public boolean applies(Instant purchasedAt) {
        Objects.requireNonNull(purchasedAt, "purchasedAt");
        return percent.signum() > 0 && !purchasedAt.isAfter(deadline);
    }

    public Money discountFor(Money price, Instant purchasedAt) {
        if (!applies(purchasedAt)) {
            return Money.zero(price.currency());
        }
        return price.percentage(percent, roundingMode);
    }

    public Money priceAfterDiscount(Money price, Instant purchasedAt) {
        return price.subtract(discountFor(price, purchasedAt));
    }
}
