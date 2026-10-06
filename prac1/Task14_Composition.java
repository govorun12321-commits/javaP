package prac1;

interface CourseFormat {
    String name();
}

class OnlineFormat implements CourseFormat {
    public String name() { return "Online"; }
}

class ClassroomFormat implements CourseFormat {
    public String name() { return "Classroom"; }
}

class HybridFormat implements CourseFormat {
    public String name() { return "Hybrid"; }
}

interface PaymentPolicy {
    String describe(double price);
}

class FreePayment implements PaymentPolicy {
    public String describe(double price) { return "Free"; }
}

class FixedPayment implements PaymentPolicy {
    public String describe(double price) { return "Fixed: " + price; }
}

class SubscriptionPayment implements PaymentPolicy {
    public String describe(double price) { return "Subscription: " + price + "/month"; }
}

class CourseOffering {
    private final CourseFormat format;
    private final PaymentPolicy payment;

    CourseOffering(CourseFormat format, PaymentPolicy payment) {
        this.format = format;
        this.payment = payment;
    }

    String describe(double price) {
        return format.name() + " + " + payment.describe(price);
    }
}

class RecordedFormat implements CourseFormat {
    public String name() { return "Recorded"; }
}

class PayPerUsePayment implements PaymentPolicy {
    public String describe(double price) { return "Pay-per-use: " + price; }
}

public class Task14_Composition {
    public static void main(String[] args) {
        CourseOffering a =
                new CourseOffering(new OnlineFormat(), new FreePayment());

        CourseOffering b =
                new CourseOffering(new HybridFormat(), new SubscriptionPayment());

        CourseOffering c =
                new CourseOffering(new RecordedFormat(), new PayPerUsePayment());

        System.out.println(a.describe(0));
        System.out.println(b.describe(20));
        System.out.println(c.describe(7));
    }
}
