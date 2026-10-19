package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public final class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, Currency currency, RoundingMode roundingMode) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(roundingMode, "roundingMode");
        return new Money(amount.setScale(fractionDigits(currency), roundingMode), currency);
    }

    public static Money of(String amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount");
        Currency currency = Currency.getInstance(Objects.requireNonNull(currencyCode, "currencyCode"));
        try {
            BigDecimal exact = new BigDecimal(amount).setScale(fractionDigits(currency), RoundingMode.UNNECESSARY);
            return new Money(exact, currency);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Amount " + amount + " has more decimal places than " + currencyCode + " allows", e);
        }
    }

    public static Money ofMinorUnits(long minorUnits, Currency currency) {
        Objects.requireNonNull(currency, "currency");
        return new Money(BigDecimal.valueOf(minorUnits, fractionDigits(currency)), currency);
    }

    public static Money zero(Currency currency) {
        return ofMinorUnits(0, currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public Money multiply(BigDecimal factor, RoundingMode roundingMode) {
        Objects.requireNonNull(factor, "factor");
        return of(amount.multiply(factor), currency, roundingMode);
    }

    public Money percentage(BigDecimal percent, RoundingMode roundingMode) {
        Objects.requireNonNull(percent, "percent");
        return of(amount.multiply(percent).movePointLeft(2), currency, roundingMode);
    }

    public List<Money> allocate(List<Integer> ratios) {
        Objects.requireNonNull(ratios, "ratios");
        if (ratios.isEmpty()) {
            throw new IllegalArgumentException("ratios must not be empty");
        }

        BigInteger ratioSum = BigInteger.ZERO;
        for (Integer ratio : ratios) {
            if (ratio == null || ratio < 0) {
                throw new IllegalArgumentException("ratio must not be negative: " + ratio);
            }
            ratioSum = ratioSum.add(BigInteger.valueOf(ratio));
        }
        if (ratioSum.signum() == 0) {
            throw new IllegalArgumentException("sum of ratios must be positive");
        }

        BigInteger total = amount.unscaledValue();
        BigInteger[] shares = new BigInteger[ratios.size()];
        BigInteger allocated = BigInteger.ZERO;
        for (int i = 0; i < shares.length; i++) {
            shares[i] = total.multiply(BigInteger.valueOf(ratios.get(i))).divide(ratioSum);
            allocated = allocated.add(shares[i]);
        }

        BigInteger leftover = total.subtract(allocated);
        BigInteger unit = BigInteger.valueOf(total.signum());
        int index = 0;
        while (leftover.signum() != 0) {
            if (ratios.get(index) > 0) {
                shares[index] = shares[index].add(unit);
                leftover = leftover.subtract(unit);
            }
            index = (index + 1) % shares.length;
        }

        int digits = fractionDigits(currency);
        List<Money> result = new ArrayList<>(shares.length);
        for (BigInteger share : shares) {
            result.add(new Money(new BigDecimal(share, digits), currency));
        }
        return Collections.unmodifiableList(result);
    }

    public List<Money> split(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be positive: " + parts);
        }
        return allocate(Collections.nCopies(parts, 1));
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money money)) {
            return false;
        }
        return currency.equals(money.currency) && amount.equals(money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency.getCurrencyCode();
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " and " + other.currency);
        }
    }

    private static int fractionDigits(Currency currency) {
        int digits = currency.getDefaultFractionDigits();
        if (digits < 0) {
            throw new IllegalArgumentException("Currency has no fixed fraction digits: " + currency);
        }
        return digits;
    }
}
