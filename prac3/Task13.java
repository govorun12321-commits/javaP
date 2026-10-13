/*
 * Задание 13. Удаление type code
 *
 * Дан класс с String notificationType и большим switch. Переведите различия поведения в
 * реализации интерфейса. Не создавайте класс на каждую строку конфигурации: объясните
 * границу оправданного полиморфизма.
 *
 * Было (type code):
 *   String notificationType; // "EMAIL", "SMS", "PUSH"
 *   void send(String to, String msg) {
 *       switch (notificationType) { case "EMAIL": ...; case "SMS": ...; case "PUSH": ...; }
 *   }
 * Каждое новое значение требует правки switch во всех местах, опечатка в строке не ловится.
 *
 * Пояснение:
 * Различающееся ПОВЕДЕНИЕ (как доставлять) выносится в реализации NotificationChannel.
 * Граница полиморфизма: класс нужен, когда меняется алгоритм. Различия в ДАННЫХ
 * (тема письма, имя отправителя, язык, адрес сервера) - это поля/параметры, а не подклассы.
 * Поэтому "EMAIL_RU" и "EMAIL_EN" - не два класса, а один EmailChannel с параметром locale.
 * Строка из конфигурации преобразуется в объект один раз в фабрике.
 */
public class Task13 {

    static class LegacyNotifier {
        private final String notificationType;
        LegacyNotifier(String type) { this.notificationType = type; }
        void send(String to, String msg) {
            switch (notificationType) {
                case "EMAIL" -> System.out.println("[email] " + to + ": " + msg);
                case "SMS" -> System.out.println("[sms] " + to + ": " + msg);
                case "PUSH" -> System.out.println("[push] " + to + ": " + msg);
                default -> throw new IllegalArgumentException(notificationType);
            }
        }
    }

    interface NotificationChannel {
        void send(String to, String msg);
    }

    static class EmailChannel implements NotificationChannel {
        private final String subject;                      
        EmailChannel(String subject) { this.subject = subject; }
        @Override public void send(String to, String msg) { System.out.println("[email] " + to + " | " + subject + " | " + msg); }
    }

    static class SmsChannel implements NotificationChannel {
        @Override public void send(String to, String msg) { System.out.println("[sms] " + to + ": " + msg); }
    }

    static class PushChannel implements NotificationChannel {
        @Override public void send(String to, String msg) { System.out.println("[push] " + to + ": " + msg); }
    }

    static NotificationChannel fromConfig(String type) {
        return switch (type) {
            case "EMAIL" -> new EmailChannel("Уведомление CourseHub");
            case "SMS" -> new SmsChannel();
            case "PUSH" -> new PushChannel();
            default -> throw new IllegalArgumentException("Неизвестный канал: " + type);
        };
    }

    static class Notifier {
        private final NotificationChannel channel;
        Notifier(NotificationChannel channel) { this.channel = channel; }
        void send(String to, String msg) { channel.send(to, msg); }     
    }

    public static void main(String[] args) {
        new LegacyNotifier("SMS").send("+37120000000", "старый способ");
        for (String type : new String[]{"EMAIL", "SMS", "PUSH"}) {
            new Notifier(fromConfig(type)).send("user", "новый способ");
        }
    }
}
