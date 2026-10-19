package coursehub.practice6.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class RefundService {

    public Money refund(Purchase purchase, CourseOffering offering, Instant requestedAt) {
        Objects.requireNonNull(purchase, "purchase");
        Objects.requireNonNull(offering, "offering");
        Objects.requireNonNull(requestedAt, "requestedAt");

        if (!purchase.courseCode().equals(offering.code())) {
            throw new IllegalArgumentException("Purchase belongs to " + purchase.courseCode()
                    + " but offering is " + offering.code());
        }
        if (purchase.lessonCount() != offering.plan().lessonCount()) {
            throw new IllegalArgumentException("Lesson count differs between purchase and offering");
        }
        if (requestedAt.isBefore(purchase.purchasedAt())) {
            throw new IllegalArgumentException("Refund cannot be requested before the purchase");
        }

        int total = purchase.lessonCount();
        int remaining = offering.plan().remainingLessons(requestedAt);
        List<Money> lessonShares = purchase.paid().split(total);

        Money refund = Money.zero(purchase.paid().currency());
        for (int index = total - remaining; index < total; index++) {
            refund = refund.add(lessonShares.get(index));
        }
        return refund;
    }
}
