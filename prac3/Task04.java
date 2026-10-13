/*
 * Задание 4. Абстрактный базовый класс
 *
 * Создайте AbstractNotificationSender с общей проверкой адресата и абстрактным doSend.
 * Реализуйте email и console варианты. Объясните, почему публичный алгоритм можно сделать
 * final и где остаётся точка расширения.
 *
 * Пояснение:
 * Это паттерн Template Method. Публичный send() - final: он задаёт неизменяемый порядок
 * (проверка адресата -> отправка), и подкласс не может случайно обойти проверку или
 * сломать инвариант. Точка расширения - protected abstract doSend(): подклассы меняют
 * только то, что действительно отличается (способ доставки). Базовый класс хранит общее
 * состояние/логику, чего у интерфейса быть не может.
 */
public class Task04 {

    static abstract class AbstractNotificationSender {
        public final void send(String recipient, String message) {   
            validateRecipient(recipient);
            doSend(recipient, message);
        }

        protected void validateRecipient(String recipient) {
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalArgumentException("Адресат не задан");
            }
        }

        protected abstract void doSend(String recipient, String message);  
    }

    static class EmailSender extends AbstractNotificationSender {
        @Override
        protected void validateRecipient(String recipient) {
            super.validateRecipient(recipient);                  
            if (!recipient.contains("@")) throw new IllegalArgumentException("Некорректный email: " + recipient);
        }

        @Override
        protected void doSend(String recipient, String message) {
            System.out.println("[EMAIL] to=" + recipient + " : " + message);
        }
    }

    static class ConsoleSender extends AbstractNotificationSender {
        @Override
        protected void doSend(String recipient, String message) {
            System.out.println("[CONSOLE] " + recipient + " -> " + message);
        }
    }

    public static void main(String[] args) {
        AbstractNotificationSender[] senders = { new EmailSender(), new ConsoleSender() };
        for (AbstractNotificationSender s : senders) {
            s.send("student@mail.com", "Вы записаны на курс");
        }
        try {
            senders[1].send("  ", "x");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
