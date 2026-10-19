package coursehub.practice6.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency JPY = Currency.getInstance("JPY");

    @Test
    void amountIsNormalizedToCurrencyScale() {
        assertEquals("10.00 EUR", Money.of("10", "EUR").toString());
        assertEquals("10.50 EUR", Money.of("10.5", "EUR").toString());
        assertEquals("1000 JPY", Money.of("1000", "JPY").toString());
        assertEquals(2, Money.of("10", "EUR").amount().scale());
        assertEquals(0, Money.of("10", "JPY").amount().scale());
    }

    @Test
    void equalityIsByValueAndCurrency() {
        assertEquals(Money.of("10", "EUR"), Money.of("10.00", "EUR"));
        assertEquals(Money.of("10", "EUR").hashCode(), Money.of("10.00", "EUR").hashCode());
        assertNotEquals(Money.of("10", "EUR"), Money.of("10", "USD"));
        assertNotEquals(Money.of("10", "EUR"), Money.of("10.01", "EUR"));
    }

    @Test
    void tooManyDecimalsRequireExplicitRounding() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("10.005", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> Money.of("10.5", "JPY"));
        assertEquals("10.01 EUR", Money.of(new BigDecimal("10.005"), EUR, RoundingMode.HALF_UP).toString());
        assertEquals("10.00 EUR", Money.of(new BigDecimal("10.005"), EUR, RoundingMode.HALF_EVEN).toString());
        assertEquals("10.00 EUR", Money.of(new BigDecimal("10.009"), EUR, RoundingMode.DOWN).toString());
    }

    @Test
    void addAndSubtractWorkInsideOneCurrency() {
        Money sum = Money.of("0.10", "EUR").add(Money.of("0.20", "EUR"));
        assertEquals(Money.of("0.30", "EUR"), sum);
        assertEquals(Money.of("-4.50", "EUR"), Money.of("5.50", "EUR").subtract(Money.of("10.00", "EUR")));
        assertTrue(Money.of("-4.50", "EUR").isNegative());
        assertTrue(Money.zero(EUR).isZero());
        assertTrue(Money.of("0.01", "EUR").isPositive());
        assertEquals(Money.of("-5.50", "EUR"), Money.of("5.50", "EUR").negate());
    }

    @Test
    void currenciesCannotBeMixed() {
        Money euro = Money.of("1", "EUR");
        Money dollar = Money.of("1", "USD");
        assertThrows(IllegalArgumentException.class, () -> euro.add(dollar));
        assertThrows(IllegalArgumentException.class, () -> euro.subtract(dollar));
        assertThrows(IllegalArgumentException.class, () -> euro.compareTo(dollar));
    }

    @Test
    void percentageUsesExplicitRounding() {
        Money price = Money.of("19.99", "EUR");
        assertEquals(Money.of("2.00", "EUR"), price.percentage(new BigDecimal("10"), RoundingMode.HALF_UP));
        assertEquals(Money.of("1.99", "EUR"), price.percentage(new BigDecimal("10"), RoundingMode.DOWN));
        assertEquals(Money.of("2.00", "EUR"), price.percentage(new BigDecimal("10"), RoundingMode.UP));
        assertEquals(Money.of("0.25", "EUR"), Money.of("10.00", "EUR").percentage(new BigDecimal("2.5"), RoundingMode.HALF_UP));
        assertEquals(Money.of("1500", "JPY"), Money.of("10000", "JPY").percentage(new BigDecimal("15"), RoundingMode.HALF_UP));
        assertEquals(Money.of("1", "JPY"), Money.of("15", "JPY").percentage(new BigDecimal("5"), RoundingMode.HALF_EVEN));
    }

    @Test
    void multiplyRoundsOnlyOnce() {
        assertEquals(Money.of("3.33", "EUR"),
                Money.of("10.00", "EUR").multiply(new BigDecimal("0.3333"), RoundingMode.HALF_UP));
    }

    @Test
    void allocationKeepsEveryMinorUnit() {
        List<Money> parts = Money.of("100.00", "EUR").allocate(List.of(1, 1, 1));
        assertEquals(List.of(Money.of("33.34", "EUR"), Money.of("33.33", "EUR"), Money.of("33.33", "EUR")), parts);
        assertEquals(Money.of("100.00", "EUR"), parts.stream().reduce(Money.zero(EUR), Money::add));
    }

    @Test
    void allocationFollowsRatios() {
        List<Money> parts = Money.of("0.05", "EUR").allocate(List.of(3, 7));
        assertEquals(List.of(Money.of("0.02", "EUR"), Money.of("0.03", "EUR")), parts);
        List<Money> uneven = Money.of("10.00", "EUR").allocate(List.of(70, 20, 10));
        assertEquals(List.of(Money.of("7.00", "EUR"), Money.of("2.00", "EUR"), Money.of("1.00", "EUR")), uneven);
    }

    @Test
    void zeroRatioPartsGetNothing() {
        List<Money> parts = Money.of("0.05", "EUR").allocate(List.of(0, 1, 1));
        assertEquals(List.of(Money.zero(EUR), Money.of("0.03", "EUR"), Money.of("0.02", "EUR")), parts);
    }

    @Test
    void allocationWorksForNegativeAndZeroDecimalCurrencies() {
        List<Money> negative = Money.of("-0.05", "EUR").allocate(List.of(1, 1));
        assertEquals(List.of(Money.of("-0.03", "EUR"), Money.of("-0.02", "EUR")), negative);
        List<Money> yen = Money.of("100", "JPY").split(3);
        assertEquals(List.of(Money.of("34", "JPY"), Money.of("33", "JPY"), Money.of("33", "JPY")), yen);
        assertEquals(Money.of("100", "JPY"), yen.stream().reduce(Money.zero(JPY), Money::add));
    }

    @Test
    void splitRejectsNonPositiveParts() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "EUR").split(0));
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "EUR").allocate(List.of()));
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "EUR").allocate(List.of(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "EUR").allocate(List.of(1, -1)));
    }

    @Test
    void comparisonOrdersByAmount() {
        assertTrue(Money.of("1.00", "EUR").compareTo(Money.of("2.00", "EUR")) < 0);
        assertEquals(0, Money.of("1.0", "EUR").compareTo(Money.of("1.00", "EUR")));
    }

    @Test
    void minorUnitsFactoryMatchesCurrencyScale() {
        assertEquals(Money.of("12.34", "EUR"), Money.ofMinorUnits(1234, EUR));
        assertEquals(Money.of("1234", "JPY"), Money.ofMinorUnits(1234, JPY));
        assertFalse(Money.ofMinorUnits(1, EUR).isZero());
    }

    @Test
    void unknownCurrencyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1", "ZZZ"));
        assertThrows(IllegalArgumentException.class, () -> Money.of("abc", "EUR"));
    }
}
