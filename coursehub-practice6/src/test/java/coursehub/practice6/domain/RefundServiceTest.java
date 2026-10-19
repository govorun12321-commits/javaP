package coursehub.practice6.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefundServiceTest {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");
    private static final Instant EARLY = Instant.parse("2026-02-20T10:00:00Z");
    private static final Instant LATE = Instant.parse("2026-03-10T10:00:00Z");

    private final PricingService pricing = new PricingService();
    private final RefundService refunds = new RefundService();

    @Test
    void refundIsProportionalToRemainingLessons() {
        CourseOffering offering = PricingServiceTest.standardOffering();
        Purchase purchase = pricing.purchase(offering, EUR, EARLY, RIGA);
        assertEquals(Money.of("90.00", "EUR"), purchase.paid());

        Instant firstStart = offering.plan().lessonStart(0);
        assertEquals(Money.of("90.00", "EUR"), refunds.refund(purchase, offering, firstStart.minusNanos(1)));
        assertEquals(Money.of("67.50", "EUR"), refunds.refund(purchase, offering, firstStart));
        assertEquals(Money.of("45.00", "EUR"), refunds.refund(purchase, offering, offering.plan().lessonStart(1)));
        assertEquals(Money.of("22.50", "EUR"), refunds.refund(purchase, offering, offering.plan().lessonStart(2)));
        assertEquals(Money.of("0.00", "EUR"), refunds.refund(purchase, offering, offering.plan().lessonStart(3)));
    }

    @Test
    void refundPlusUsedShareAlwaysEqualsPaidWhenCentsDoNotDivideEvenly() {
        CourseOffering offering = PricingServiceTest.offering(Map.of(EUR, Money.of("99.99", "EUR")));
        Purchase purchase = pricing.purchase(offering, EUR, LATE, RIGA);
        assertEquals(Money.of("99.99", "EUR"), purchase.paid());

        for (int lessonIndex = 0; lessonIndex < 4; lessonIndex++) {
            Instant requestedAt = offering.plan().lessonStart(lessonIndex);
            Money refund = refunds.refund(purchase, offering, requestedAt);
            Money used = purchase.paid().subtract(refund);
            assertEquals(purchase.paid(), refund.add(used));
            assertEquals(false, refund.isNegative());
        }
        assertEquals(Money.of("74.99", "EUR"), refunds.refund(purchase, offering, offering.plan().lessonStart(0)));
        assertEquals(Money.of("49.99", "EUR"), refunds.refund(purchase, offering, offering.plan().lessonStart(1)));
        assertEquals(Money.of("24.99", "EUR"), refunds.refund(purchase, offering, offering.plan().lessonStart(2)));
    }

    @Test
    void refundIsKeptInThePurchaseCurrency() {
        CourseOffering offering = PricingServiceTest.standardOffering();
        Purchase usd = pricing.purchase(offering, USD, EARLY, RIGA);
        Money refund = refunds.refund(usd, offering, offering.plan().lessonStart(0));
        assertEquals(Money.of("74.25", "USD"), refund);
        assertEquals(USD, refund.currency());
    }

    @Test
    void zeroDecimalCurrencyKeepsEveryYen() {
        CourseOffering offering = PricingServiceTest.offering(Map.of(JPY, Money.of("10001", "JPY")));
        Purchase purchase = pricing.purchase(offering, JPY, LATE, RIGA);
        assertEquals(Money.of("10001", "JPY"), purchase.paid());
        Money refund = refunds.refund(purchase, offering, offering.plan().lessonStart(0));
        assertEquals(Money.of("7500", "JPY"), refund);
        assertEquals(Money.of("2501", "JPY"), purchase.paid().subtract(refund));
    }

    @Test
    void refundFollowsRealLessonInstantsAcrossDstChange() {
        CourseOffering offering = PricingServiceTest.standardOffering();
        Purchase purchase = pricing.purchase(offering, EUR, EARLY, RIGA);

        Instant beforeSecondLesson = Instant.parse("2026-03-31T14:59:59Z");
        Instant afterSecondLessonStarted = Instant.parse("2026-03-31T15:30:00Z");
        assertEquals(Money.of("67.50", "EUR"), refunds.refund(purchase, offering, beforeSecondLesson));
        assertEquals(Money.of("45.00", "EUR"), refunds.refund(purchase, offering, afterSecondLessonStarted));
    }

    @Test
    void requestAfterTheWholeSeriesGivesNothing() {
        CourseOffering offering = PricingServiceTest.standardOffering();
        Purchase purchase = pricing.purchase(offering, EUR, EARLY, RIGA);
        Money refund = refunds.refund(purchase, offering, Instant.parse("2026-06-01T00:00:00Z"));
        assertEquals(Money.of("0.00", "EUR"), refund);
        assertEquals(true, refund.isZero());
    }

    @Test
    void invalidRequestsAreRejected() {
        CourseOffering offering = PricingServiceTest.standardOffering();
        Purchase purchase = pricing.purchase(offering, EUR, EARLY, RIGA);
        assertThrows(IllegalArgumentException.class, () -> refunds.refund(purchase, offering, EARLY.minusSeconds(1)));

        CourseOffering other = PricingServiceTest.offering(Map.of(EUR, Money.of("50.00", "EUR")));
        CourseOffering differentCode = new CourseOffering(
                coursehub.practice6.strings.CourseCode.parse("SQL-001"), other.title(), other.plan(),
                other.priceList(), other.earlyBird());
        assertThrows(IllegalArgumentException.class, () -> refunds.refund(purchase, differentCode, LATE));
    }
}
