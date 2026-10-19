package coursehub.practice6.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarlyBirdPolicyTest {

    private static final Instant DEADLINE = Instant.parse("2026-03-01T00:00:00Z");

    private final EarlyBirdPolicy policy = new EarlyBirdPolicy(DEADLINE, new BigDecimal("10"), RoundingMode.HALF_UP);

    @Test
    void discountIncludesTheDeadlineMoment() {
        assertTrue(policy.applies(DEADLINE));
        assertTrue(policy.applies(DEADLINE.minusNanos(1)));
    }

    @Test
    void discountEndsImmediatelyAfterTheDeadline() {
        assertFalse(policy.applies(DEADLINE.plusNanos(1)));
        assertFalse(policy.applies(DEADLINE.plusSeconds(1)));
    }

    @Test
    void discountIsAppliedAndRounded() {
        Money price = Money.of("19.99", "EUR");
        assertEquals(Money.of("2.00", "EUR"), policy.discountFor(price, DEADLINE));
        assertEquals(Money.of("17.99", "EUR"), policy.priceAfterDiscount(price, DEADLINE));
    }

    @Test
    void fullPriceAfterTheDeadline() {
        Money price = Money.of("19.99", "EUR");
        assertEquals(Money.zero(price.currency()), policy.discountFor(price, DEADLINE.plusNanos(1)));
        assertEquals(price, policy.priceAfterDiscount(price, DEADLINE.plusNanos(1)));
    }

    @Test
    void disabledPolicyNeverApplies() {
        EarlyBirdPolicy none = EarlyBirdPolicy.none();
        assertFalse(none.applies(Instant.EPOCH));
        assertFalse(none.applies(Instant.parse("2026-01-01T00:00:00Z")));
        assertEquals(Money.of("5.00", "USD"), none.priceAfterDiscount(Money.of("5.00", "USD"), Instant.EPOCH));
    }

    @Test
    void roundingModeIsPartOfThePolicy() {
        EarlyBirdPolicy down = new EarlyBirdPolicy(DEADLINE, new BigDecimal("10"), RoundingMode.DOWN);
        assertEquals(Money.of("1.99", "EUR"), down.discountFor(Money.of("19.99", "EUR"), DEADLINE));
    }

    @Test
    void percentMustBeBetweenZeroAndHundred() {
        assertThrows(IllegalArgumentException.class,
                () -> new EarlyBirdPolicy(DEADLINE, new BigDecimal("-1"), RoundingMode.HALF_UP));
        assertThrows(IllegalArgumentException.class,
                () -> new EarlyBirdPolicy(DEADLINE, new BigDecimal("100.01"), RoundingMode.HALF_UP));
    }
}
