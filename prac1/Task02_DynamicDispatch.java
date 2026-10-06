package prac1;

class Notification {
    final String text;

    Notification(String text) {
        this.text = text;
    }
}

class NotificationSender {
    public void send(Notification notification) {
        System.out.println("NotificationSender.send: " + notification.text);
    }

    public void send(String text) {
        System.out.println("NotificationSender.send(String): " + text);
    }
}

class EmailSender extends NotificationSender {
    @Override
    public void send(Notification notification) {
        System.out.println("EmailSender.send: " + notification.text);
    }

    public void send(EmailSender sender) {
        System.out.println("EmailSender.send(EmailSender)");
    }
}

class ConsoleSender extends NotificationSender {
    @Override
    public void send(Notification notification) {
        System.out.println("ConsoleSender.send: " + notification.text);
    }
}

public class Task02_DynamicDispatch {
    public static void main(String[] args) {
        Notification notification = new Notification("Hello");
        EmailSender email = new EmailSender();
        NotificationSender base = email;
        NotificationSender console = new ConsoleSender();

        base.send(notification);       
        console.send(notification);    

        base.send("text");             
        email.send(email);             
    }
}
