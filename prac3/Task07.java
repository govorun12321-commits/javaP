/*
 * Задание 7. Композиция вместо наследования
 *
 * Дан LoggingEmailSender extends EmailSender, затем требуется retry и metrics.
 * Перепроектируйте logging и metrics как декораторы вокруг NotificationSender.
 * Покажите два порядка композиции и объясните разницу наблюдаемого поведения.
 *
 * Проблема наследования: LoggingEmailSender привязан к email; для retry и metrics пришлось бы
 * плодить RetryLoggingEmailSender, MetricsRetryEmailSender... (комбинаторный взрыв).
 *
 * Пояснение:
 * Каждая возможность - декоратор, реализующий NotificationSender и хранящий делегата.
 * Их можно комбинировать в любом порядке и вокруг любой реализации.
 *  - Retry(Metrics(sender)): метрики видят каждую ПОПЫТКУ (3 вызова, 2 ошибки).
 *  - Metrics(Retry(sender)): метрики видят одну ЛОГИЧЕСКУЮ операцию (1 вызов, 0 ошибок),
 *    повторы скрыты внутри.
 */
public class Task07 {

    interface NotificationSender {
        void send(String to, String msg);
    }

    static class FlakyEmailSender implements NotificationSender {
        private int remainingFailures;
        FlakyEmailSender(int failures) { this.remainingFailures = failures; }
        @Override public void send(String to, String msg) {
            if (remainingFailures-- > 0) throw new IllegalStateException("SMTP недоступен");
            System.out.println("  [email] доставлено " + to + ": " + msg);
        }
    }

    static class LoggingSender implements NotificationSender {
        private final NotificationSender delegate;
        LoggingSender(NotificationSender delegate) { this.delegate = delegate; }
        @Override public void send(String to, String msg) {
            System.out.println("  [log] send -> " + to);
            delegate.send(to, msg);
        }
    }

    static class MetricsSender implements NotificationSender {
        private final NotificationSender delegate;
        int calls, failures;
        MetricsSender(NotificationSender delegate) { this.delegate = delegate; }
        @Override public void send(String to, String msg) {
            calls++;
            try { delegate.send(to, msg); }
            catch (RuntimeException e) { failures++; throw e; }
        }
    }

    static class RetrySender implements NotificationSender {
        private final NotificationSender delegate;
        private final int maxAttempts;
        RetrySender(NotificationSender delegate, int maxAttempts) { this.delegate = delegate; this.maxAttempts = maxAttempts; }
        @Override public void send(String to, String msg) {
            RuntimeException last = null;
            for (int i = 1; i <= maxAttempts; i++) {
                try { delegate.send(to, msg); return; }
                catch (RuntimeException e) { last = e; System.out.println("  [retry] попытка " + i + " не удалась"); }
            }
            throw last;
        }
    }

    public static void main(String[] args) {
        System.out.println("Порядок 1: Retry(Metrics(Flaky))");
        MetricsSender m1 = new MetricsSender(new FlakyEmailSender(2));
        new LoggingSender(new RetrySender(m1, 3)).send("a@mail.com", "Привет");
        System.out.println("  метрики: calls=" + m1.calls + ", failures=" + m1.failures);

        System.out.println("Порядок 2: Metrics(Retry(Flaky))");
        MetricsSender[] holder = new MetricsSender[1];
        holder[0] = new MetricsSender(new RetrySender(new FlakyEmailSender(2), 3));
        new LoggingSender(holder[0]).send("a@mail.com", "Привет");
        System.out.println("  метрики: calls=" + holder[0].calls + ", failures=" + holder[0].failures);
    }
}
