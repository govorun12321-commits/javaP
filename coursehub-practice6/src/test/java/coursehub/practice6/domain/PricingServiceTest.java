package coursehub.practice6.domain;

import coursehub.practice6.strings.CourseCode;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PricingServiceTest {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    private static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency JPY = Currency.getInstance("JPY");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Instant DEADLINE = Instant.parse("2026-03-01T00:00:00Z");

    private final PricingService service = new PricingService();

    static CourseOffering offering(Map<Currency, Money> prices) {
        WebinarSeriesPlan plan = new WebinarSeriesPlan(RIGA, LocalDate.of(2026, 3, 24), LocalTime.of(18, 0),
                Period.ofWeeks(1), 4, Duration.ofMinutes(90));
        EarlyBirdPolicy earlyBird = new EarlyBirdPolicy(DEADLINE, new BigDecimal("10"), RoundingMode.HALF_UP);
        return new CourseOffering(CourseCode.parse("JAVA-101"), CourseTitle.of("Java Time"), plan, prices, earlyBird);
    }

    static CourseOffering standardOffering() {
        return offering(Map.of(
                EUR, Money.of("100.00", "EUR"),
                USD, Money.of("110.00", "USD"),
                JPY, Money.of("15000", "JPY")));
    }

    @Test
    void earlyBirdPriceIsCalculatedInEachCurrency() {
        CourseOffering offering = standardOffering();
        Instant early = DEADLINE.minusSeconds(60);
        assertEquals(Money.of("90.00", "EUR"), service.priceAt(offering, EUR, early));
        assertEquals(Money.of("99.00", "USD"), service.priceAt(offering, USD, early));
        assertEquals(Money.of("13500", "JPY"), service.priceAt(offering, JPY, early));
    }

    @Test
    void fullPriceStartsRightAfterTheDeadline() {
        CourseOffering offering = standardOffering();
        assertEquals(Money.of("90.00", "EUR"), service.priceAt(offering, EUR, DEADLINE));
        assertEquals(Money.of("100.00", "EUR"), service.priceAt(offering, EUR, DEADLINE.plusNanos(1)));
    }

    @Test
    void deadlineIsTheSameMomentForBuyersInEveryZone() {
        CourseOffering offering = standardOffering();
        Instant atDeadline = DEADLINE;
        for (ZoneId zone : new ZoneId[]{RIGA, NEW_YORK, TOKYO}) {
            Purchase purchase = service.purchase(offering, EUR, atDeadline, zone);
            assertTrue(purchase.earlyBirdApplied(), zone.getId());
        }
        for (ZoneId zone : new ZoneId[]{RIGA, NEW_YORK, TOKYO}) {
            Purchase purchase = service.purchase(offering, EUR, atDeadline.plusNanos(1), zone);
            assertFalse(purchase.earlyBirdApplied(), zone.getId());
        }
    }

    @Test
    void buyerLocalDateDependsOnBuyerZoneOnly() {
        CourseOffering offering = standardOffering();
        Instant moment = Instant.parse("2026-02-28T23:30:00Z");
        Purchase fromRiga = service.purchase(offering, EUR, moment, RIGA);
        Purchase fromNewYork = service.purchase(offering, EUR, moment, NEW_YORK);
        Purchase fromTokyo = service.purchase(offering, EUR, moment, TOKYO);
        assertEquals(LocalDate.of(2026, 3, 1), fromRiga.purchaseDateInBuyerZone());
        assertEquals(LocalDate.of(2026, 2, 28), fromNewYork.purchaseDateInBuyerZone());
        assertEquals(LocalDate.of(2026, 3, 1), fromTokyo.purchaseDateInBuyerZone());
        assertEquals(fromRiga.paid(), fromNewYork.paid());
        assertEquals(fromRiga.paid(), fromTokyo.paid());
    }

    @Test
    void purchaseKeepsListPriceDiscountAndPaidConsistent() {
        Purchase purchase = service.purchase(standardOffering(), USD, DEADLINE, NEW_YORK);
        assertEquals(Money.of("110.00", "USD"), purchase.listPrice());
        assertEquals(Money.of("11.00", "USD"), purchase.discount());
        assertEquals(Money.of("99.00", "USD"), purchase.paid());
        assertEquals(4, purchase.lessonCount());
    }

    @Test
    void discountRoundingIsExplicitAndHalfUp() {
        CourseOffering offering = offering(Map.of(EUR, Money.of("19.99", "EUR")));
        Purchase purchase = service.purchase(offering, EUR, DEADLINE, RIGA);
        assertEquals(Money.of("2.00", "EUR"), purchase.discount());
        assertEquals(Money.of("17.99", "EUR"), purchase.paid());
    }

    @Test
    void zeroDecimalCurrencyIsRoundedToWholeUnits() {
        CourseOffering offering = offering(Map.of(JPY, Money.of("10005", "JPY")));
        Purchase purchase = service.purchase(offering, JPY, DEADLINE, TOKYO);
        assertEquals(Money.of("1001", "JPY"), purchase.discount());
        assertEquals(Money.of("9004", "JPY"), purchase.paid());
    }

    @Test
    void currencyWithoutPriceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.priceAt(standardOffering(), GBP, DEADLINE));
        assertThrows(IllegalArgumentException.class, () -> service.purchase(standardOffering(), GBP, DEADLINE, RIGA));
    }

    @Test
    void priceListMustMatchItsCurrencies() {
        assertThrows(IllegalArgumentException.class, () -> offering(Map.of(EUR, Money.of("10.00", "USD"))));
        assertThrows(IllegalArgumentException.class, () -> offering(Map.of(EUR, Money.zero(EUR))));
        assertThrows(IllegalArgumentException.class, () -> offering(Map.of()));
    }

    @Test
    void purchaseDuringSeriesIsAllowedUntilTheLastLessonStarts() {
        CourseOffering offering = standardOffering();
        Instant lastStart = offering.plan().lessonStart(3);
        assertEquals(Money.of("100.00", "EUR"),
                service.purchase(offering, EUR, lastStart.minusNanos(1), RIGA).paid());
        assertThrows(IllegalStateException.class, () -> service.purchase(offering, EUR, lastStart, RIGA));
    }
}
