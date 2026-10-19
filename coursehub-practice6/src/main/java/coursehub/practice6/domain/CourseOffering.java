package coursehub.practice6.domain;

import coursehub.practice6.strings.CourseCode;

import java.util.Currency;
import java.util.Map;
import java.util.Objects;

public record CourseOffering(CourseCode code,
                             CourseTitle title,
                             WebinarSeriesPlan plan,
                             Map<Currency, Money> priceList,
                             EarlyBirdPolicy earlyBird) {

    public CourseOffering {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(priceList, "priceList");
        Objects.requireNonNull(earlyBird, "earlyBird");
        priceList = Map.copyOf(priceList);
        if (priceList.isEmpty()) {
            throw new IllegalArgumentException("priceList must not be empty");
        }
        priceList.forEach((currency, price) -> {
            if (!price.currency().equals(currency)) {
                throw new IllegalArgumentException("Price " + price + " is listed under " + currency);
            }
            if (!price.isPositive()) {
                throw new IllegalArgumentException("Price must be positive: " + price);
            }
        });
    }

    public Money listPrice(Currency currency) {
        Money price = priceList.get(Objects.requireNonNull(currency, "currency"));
        if (price == null) {
            throw new IllegalArgumentException("Course " + code + " is not sold in " + currency);
        }
        return price;
    }
}
