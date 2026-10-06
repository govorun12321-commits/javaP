package prac1;

interface NotificationStrategy {
    void send(String recipient, String message);
}

class EmailStrategy implements NotificationStrategy {
    @Override
    public void send(String recipient, String message) {
        System.out.println("EMAIL -> " + recipient + ": " + message);
    }
}

class ConsoleStrategy implements NotificationStrategy {
    @Override
    public void send(String recipient, String message) {
        System.out.println("CONSOLE -> " + recipient + ": " + message);
    }
}

class SmsStrategy implements NotificationStrategy {
    @Override
    public void send(String recipient, String message) {
        System.out.println("SMS -> " + recipient + ": " + message);
    }
}

class NotificationService13 {
    private final NotificationStrategy strategy;

    NotificationService13(NotificationStrategy strategy) {
        this.strategy = strategy;
    }

    void notify(String recipient, String message) {
        strategy.send(recipient, message);
    }
}

public class Task13_RemoveTypeCode {
    public static void main(String[] args) {
        NotificationService13 service =
                new NotificationService13(new EmailStrategy());
        service.notify("user@example.com", "Course updated");

        NotificationService13 console =
                new NotificationService13(new ConsoleStrategy());
        console.notify("terminal", "Course updated");
    }
}
