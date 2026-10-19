package coursehub.practice6.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;

public final class MoneyDemo {

    private MoneyDemo() {
    }

    public static void main(String[] args) {
        Money price = Money.of("19.99", "EUR");
        System.out.println("price: " + price);
        System.out.println("add 0.10 + 0.20: " + Money.of("0.10", "EUR").add(Money.of("0.20", "EUR")));
        System.out.println("subtract 5.50 - 10.00: " + Money.of("5.50", "EUR").subtract(Money.of("10.00", "EUR")));

        BigDecimal ten = new BigDecimal("10");
        System.out.println("10% HALF_UP: " + price.percentage(ten, RoundingMode.HALF_UP));
        System.out.println("10% DOWN: " + price.percentage(ten, RoundingMode.DOWN));

        System.out.println("100.00 EUR allocate [1,1,1]: " + Money.of("100.00", "EUR").allocate(List.of(1, 1, 1)));
        System.out.println("0.05 EUR allocate [3,7]: " + Money.of("0.05", "EUR").allocate(List.of(3, 7)));
        System.out.println("100 JPY split 3: " + Money.of("100", "JPY").split(3));
        System.out.println("-0.05 EUR split 2: " + Money.of("-0.05", "EUR").split(2));

        System.out.println("equals 10 and 10.00 EUR: " + Money.of("10", "EUR").equals(Money.of("10.00", "EUR")));
        System.out.println("equals 10 EUR and 10 USD: " + Money.of("10", "EUR").equals(Money.of("10", "USD")));
        System.out.println("rounded 10.005 HALF_UP: "
                + Money.of(new BigDecimal("10.005"), Currency.getInstance("EUR"), RoundingMode.HALF_UP));

        try {
            Money.of("1", "EUR").add(Money.of("1", "USD"));
        } catch (IllegalArgumentException e) {
            System.out.println("mixing currencies: " + e.getMessage());
        }
        try {
            Money.of("10.005", "EUR");
        } catch (IllegalArgumentException e) {
            System.out.println("no implicit rounding: " + e.getMessage());
        }
    }
}
