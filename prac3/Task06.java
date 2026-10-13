import java.util.List;

/*
 * Задание 6. Переопределение без случайной перегрузки
 *
 * Исправьте набор методов с ошибками в параметрах, доступе и возвращаемых типах. Везде, где
 * ожидается переопределение, используйте @Override. Продемонстрируйте случай, когда код
 * компилировался без аннотации, но вызывался не тот метод.
 *
 * Ошибочный вариант (в комментарии, т.к. с @Override он не компилируется):
 *
 *   class Child extends Base {
 *       @Override public Number value(Object key)   { ... }  // ОШИБКА: параметр Object != String -> это перегрузка
 *       @Override void notifyUser(String msg)       { ... }  // ОШИБКА: доступ слабее (package vs public)
 *       @Override public String label()             { ... }  // ОШИБКА: возврат String несовместим с int
 *   }
 *
 * Пояснение:
 * @Override просит компилятор проверить, что метод действительно переопределяет. Без него
 * ошибка сигнатуры молча превращается в перегрузку. Классический случай - equals(Money)
 * вместо equals(Object): List.contains вызывает equals(Object) и "не находит" элемент.
 * Правила переопределения: те же параметры, доступ не уже, возврат - тот же или ковариантный.
 */
public class Task06 {

    // ---- Исправленный набор ----
    static class Base {
        public Number value(String key) { return 0; }
        public void notifyUser(String msg) { System.out.println("Base: " + msg); }
        public int label() { return 1; }
    }

    static class Child extends Base {
        @Override public Integer value(String key) { return key.length(); }   
        @Override public void notifyUser(String msg) { System.out.println("Child: " + msg); }
        @Override public int label() { return 2; }
    }

    static class BadMoney {
        final int amount;
        BadMoney(int amount) { this.amount = amount; }
        public boolean equals(BadMoney other) { return other != null && amount == other.amount; } 
    }

    static class GoodMoney {
        final int amount;
        GoodMoney(int amount) { this.amount = amount; }
        @Override public boolean equals(Object o) { return o instanceof GoodMoney m && amount == m.amount; }
        @Override public int hashCode() { return Integer.hashCode(amount); }
    }

    public static void main(String[] args) {
        Base b = new Child();
        System.out.println(b.value("abcd") + " " + b.label());
        b.notifyUser("hello");

        System.out.println("BadMoney.equals(BadMoney): " + new BadMoney(5).equals(new BadMoney(5)));            
        System.out.println("BadMoney в List.contains:   " + List.of(new BadMoney(5)).contains(new BadMoney(5))); 
        System.out.println("GoodMoney в List.contains:  " + List.of(new GoodMoney(5)).contains(new GoodMoney(5))); 
    }
}
