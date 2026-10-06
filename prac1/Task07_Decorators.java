package prac1;

interface NotificationSender7 {
    void send(String recipient, String message);
}

class EmailSender7 implements NotificationSender7 {
    @Override
    public void send(String recipient, String message) {
        System.out.println("EMAIL -> " + recipient + ": " + message);
    }
}

abstract class SenderDecorator implements NotificationSender7 {
    protected final NotificationSender7 delegate;

    protected SenderDecorator(NotificationSender7 delegate) {
        this.delegate = delegate;
    }
}

class LoggingSenderDecorator extends SenderDecorator {
    LoggingSenderDecorator(NotificationSender7 delegate) {
        super(delegate);
    }

    @Override
    public void send(String recipient, String message) {
        System.out.println("LOG before");
        delegate.send(recipient, message);
        System.out.println("LOG after");
    }
}

class MetricsSenderDecorator extends SenderDecorator {
    MetricsSenderDecorator(NotificationSender7 delegate) {
        super(delegate);
    }

    @Override
    public void send(String recipient, String message) {
        long start = System.nanoTime();
        delegate.send(recipient, message);
        long elapsed = System.nanoTime() - start;
        System.out.println("METRICS nanos=" + elapsed);
    }
}

class RetrySenderDecorator extends SenderDecorator {
    private final int attempts;

    RetrySenderDecorator(NotificationSender7 delegate, int attempts) {
        super(delegate);
        this.attempts = attempts;
    }

    @Override
    public void send(String recipient, String message) {
        RuntimeException last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                delegate.send(recipient, message);
                return;
            } catch (RuntimeException ex) {
                last = ex;
                System.out.println("RETRY " + i);
            }
        }
        throw last;
    }
}

public class Task07_Decorators {
    public static void main(String[] args) {
        NotificationSender7 order1 =
                new LoggingSenderDecorator(
                        new MetricsSenderDecorator(
                                new EmailSender7()));

        NotificationSender7 order2 =
                new MetricsSenderDecorator(
                        new LoggingSenderDecorator(
                                new EmailSender7()));

        System.out.println("Order 1:");
        order1.send("a@b.com", "Hello");

        System.out.println("Order 2:");
        order2.send("a@b.com", "Hello");
    }
}
