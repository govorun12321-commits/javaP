/*
 * Задание 2. Dynamic dispatch на бумаге
 *
 * Для иерархии NotificationSender -> EmailSender, ConsoleSender предскажите результат вызовов
 * через ссылки разных compile-time типов. Добавьте перегруженный метод и отделите
 * overload resolution от override dispatch.
 *
 * Пояснение:
 * 1) Overload resolution (какая СИГНАТУРА) происходит при компиляции по статическим типам
 *    аргументов и типу ссылки: send(Object) или send(String) или send(String,int).
 * 2) Override dispatch (какая РЕАЛИЗАЦИЯ этой сигнатуры) происходит в runtime по фактическому
 *    классу объекта-получателя.
 * Метод send(String,int) есть только в EmailSender, поэтому через ссылку типа
 * NotificationSender его вызвать нельзя - это решает компилятор.
 */
public class Task02 {

    static class NotificationSender {
        String send(String m) { return "NotificationSender.send(String): " + m; }
        String send(Object o) { return "NotificationSender.send(Object): " + o; }
    }

    static class EmailSender extends NotificationSender {
        @Override String send(String m) { return "EmailSender.send(String): " + m; }
        @Override String send(Object o) { return "EmailSender.send(Object): " + o; }
        String send(String m, int priority) { return "EmailSender.send(String,int) prio=" + priority; }
    }

    static class ConsoleSender extends NotificationSender {
        @Override String send(String m) { return "ConsoleSender.send(String): " + m; }
    }

    public static void main(String[] args) {
        NotificationSender a = new EmailSender();    
        NotificationSender b = new ConsoleSender();  
        EmailSender c = new EmailSender();
        Object o = "hello";                          


        System.out.println(a.send("hi")); 
        System.out.println(a.send(o));    
        System.out.println(b.send("hi")); 
        System.out.println(b.send(o));    
        System.out.println(c.send("hi", 5)); 
    }
}
