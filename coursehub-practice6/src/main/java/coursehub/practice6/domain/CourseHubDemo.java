package coursehub.practice6.domain;

import coursehub.practice6.strings.CourseCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Map;

public final class CourseHubDemo {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency JPY = Currency.getInstance("JPY");

    private CourseHubDemo() {
    }

    public static void main(String[] args) {
        WebinarSeriesPlan plan = new WebinarSeriesPlan(RIGA, LocalDate.of(2026, 3, 24), LocalTime.of(18, 0),
                Period.ofWeeks(1), 4, Duration.ofMinutes(90));

        System.out.println("lessons in Riga:");
        plan.lessonsIn(RIGA).forEach(lesson -> System.out.println("  " + lesson));
        System.out.println("lessons in New York:");
        plan.lessonsIn(NEW_YORK).forEach(lesson -> System.out.println("  " + lesson));
        System.out.println("lessons in Tokyo:");
        plan.lessonsIn(TOKYO).forEach(lesson -> System.out.println("  " + lesson));
        System.out.println("first week length: " + Duration.between(plan.lessonStart(0), plan.lessonStart(1)));

        WebinarSeriesPlan monthly = new WebinarSeriesPlan(RIGA, LocalDate.of(2028, 1, 31), LocalTime.of(10, 0),
                Period.ofMonths(1), 3, Duration.ofHours(1));
        System.out.println("monthly dates: " + monthly.lessonDates() + ", leap day: " + monthly.containsLeapDay());

        Instant deadline = Instant.parse("2026-03-01T00:00:00Z");
        EarlyBirdPolicy earlyBird = new EarlyBirdPolicy(deadline, new BigDecimal("10"), RoundingMode.HALF_UP);
        CourseOffering offering = new CourseOffering(CourseCode.parse("JAVA-101"), CourseTitle.of("Java Time"), plan,
                Map.of(EUR, Money.of("99.99", "EUR"), JPY, Money.of("15000", "JPY")), earlyBird);

        PricingService pricing = new PricingService();
        RefundService refunds = new RefundService();

        System.out.println("price at deadline (EUR): " + pricing.priceAt(offering, EUR, deadline));
        System.out.println("price after deadline (EUR): " + pricing.priceAt(offering, EUR, deadline.plusNanos(1)));
        System.out.println("price at deadline (JPY): " + pricing.priceAt(offering, JPY, deadline));

        Instant purchasedAt = Instant.parse("2026-02-28T23:30:00Z");
        for (ZoneId zone : new ZoneId[]{RIGA, NEW_YORK, TOKYO}) {
            Purchase purchase = pricing.purchase(offering, EUR, purchasedAt, zone);
            System.out.println("purchase from " + zone + ": local date " + purchase.purchaseDateInBuyerZone()
                    + ", paid " + purchase.paid() + ", early bird " + purchase.earlyBirdApplied());
        }

        Purchase purchase = pricing.purchase(offering, EUR, purchasedAt, RIGA);
        System.out.println("paid: " + purchase.paid() + ", discount: " + purchase.discount());
        for (int index = 0; index < plan.lessonCount(); index++) {
            Instant requestedAt = plan.lessonStart(index);
            Money refund = refunds.refund(purchase, offering, requestedAt);
            System.out.println("refund at start of lesson " + (index + 1) + ": " + refund
                    + ", used: " + purchase.paid().subtract(refund));
        }

        Instant beforeSecond = Instant.parse("2026-03-31T14:59:59Z");
        Instant afterSecond = Instant.parse("2026-03-31T15:30:00Z");
        System.out.println("refund before second lesson: " + refunds.refund(purchase, offering, beforeSecond));
        System.out.println("refund after second lesson started: " + refunds.refund(purchase, offering, afterSecond));
    }
}
