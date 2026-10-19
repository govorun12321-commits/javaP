package coursehub.practice6.domain;

import coursehub.practice6.strings.CourseCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public record Purchase(CourseCode courseCode,
                       Instant purchasedAt,
                       ZoneId buyerZone,
                       Money listPrice,
                       Money discount,
                       Money paid,
                       int lessonCount) {

    public Purchase {
        Objects.requireNonNull(courseCode, "courseCode");
        Objects.requireNonNull(purchasedAt, "purchasedAt");
        Objects.requireNonNull(buyerZone, "buyerZone");
        Objects.requireNonNull(listPrice, "listPrice");
        Objects.requireNonNull(discount, "discount");
        Objects.requireNonNull(paid, "paid");
        if (lessonCount <= 0) {
            throw new IllegalArgumentException("lessonCount must be positive: " + lessonCount);
        }
        if (!listPrice.subtract(discount).equals(paid)) {
            throw new IllegalArgumentException("paid must equal listPrice minus discount");
        }
    }

    public LocalDate purchaseDateInBuyerZone() {
        return purchasedAt.atZone(buyerZone).toLocalDate();
    }

    public boolean earlyBirdApplied() {
        return discount.isPositive();
    }
}
