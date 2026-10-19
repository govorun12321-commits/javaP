package coursehub.practice6.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Objects;

public final class PricingService {

    public Money priceAt(CourseOffering offering, Currency currency, Instant purchasedAt) {
        Money listPrice = offering.listPrice(currency);
        return offering.earlyBird().priceAfterDiscount(listPrice, purchasedAt);
    }

    public Purchase purchase(CourseOffering offering, Currency currency, Instant purchasedAt, ZoneId buyerZone) {
        Objects.requireNonNull(offering, "offering");
        Objects.requireNonNull(purchasedAt, "purchasedAt");
        Objects.requireNonNull(buyerZone, "buyerZone");

        if (offering.plan().remainingLessons(purchasedAt) == 0) {
            throw new IllegalStateException("All lessons of " + offering.code() + " have already started");
        }

        Money listPrice = offering.listPrice(currency);
        Money discount = offering.earlyBird().discountFor(listPrice, purchasedAt);
        Money paid = listPrice.subtract(discount);
        return new Purchase(offering.code(), purchasedAt, buyerZone, listPrice, discount, paid,
                offering.plan().lessonCount());
    }
}
