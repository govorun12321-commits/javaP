package prac1;

abstract class AbstractNotificationSender {
    public final void send(String recipient, String message) {
        validateRecipient(recipient);
        doSend(recipient, message);
    }

    protected void validateRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient must not be blank");
        }
    }

    protected abstract void doSend(String recipient, String message);
}

class EmailNotificationSender extends AbstractNotificationSender {
    @Override
    protected void doSend(String recipient, String message) {
        System.out.println("EMAIL -> " + recipient + ": " + message);
    }
}

class ConsoleNotificationSender extends AbstractNotificationSender {
    @Override
    protected void doSend(String recipient, String message) {
        System.out.println("CONSOLE -> " + recipient + ": " + message);
    }
}

public class Task04_AbstractSender {
    public static void main(String[] args) {
        AbstractNotificationSender email = new EmailNotificationSender();
        AbstractNotificationSender console = new ConsoleNotificationSender();

        email.send("student@example.com", "Course started");
        console.send("terminal", "Course started");
    }
}
