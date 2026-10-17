/*
 * Задание 1. Прочитать stack trace
 *
 * Получите исключение в цепочке из четырёх методов. Найдите тип, сообщение, место
 * возникновения и путь вызовов. Исправьте причину, а не последнюю строку stack trace.
 *
 * Краткое объяснение:
 *  Цепочка: main -> totalPrice -> lineTotal -> parsePrice -> Double.parseDouble.
 *  Тип:      java.lang.NumberFormatException
 *  Сообщение: For input string: "39,90"
 *  Место:    верхний кадр trace — внутри JDK (FloatingDecimal), но это лишь «где упало».
 *            Первый кадр НАШЕГО кода — parsePrice, путь вызовов читается снизу вверх.
 *  Причина:  не «parseDouble плохо работает», а нарушенный контракт данных: источник
 *            присылает десятичную запятую, а код ждёт точку. Заплатка вида
 *            try { ... } catch (NumberFormatException e) { return 0; } скрыла бы баг.
 *            Исправление — в одном месте, где внешний текст превращается в число:
 *            явная нормализация формата + понятное сообщение с контекстом.
 */
public final class Task01_StackTrace {

    static final class Broken {
        static double totalPrice(String feed) {              
            double sum = 0;
            for (String line : feed.split("\n")) {
                sum += lineTotal(line);
            }
            return sum;
        }

        static double lineTotal(String line) {                
            String[] parts = line.split(";");
            return Integer.parseInt(parts[1]) * parsePrice(parts[2]);
        }

        static double parsePrice(String raw) {                  
            return Double.parseDouble(raw);                     
        }
    }

    static final class Fixed {
        static double totalPrice(String feed) {
            double sum = 0;
            for (String line : feed.split("\n")) {
                sum += lineTotal(line);
            }
            return sum;
        }

        static double lineTotal(String line) {
            String[] parts = line.split(";");
            return Integer.parseInt(parts[1]) * parsePrice(parts[2]);
        }

        static double parsePrice(String raw) {
            String normalized = raw.trim().replace(',', '.');
            try {
                return Double.parseDouble(normalized);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Price has unsupported format", e);
            }
        }
    }

    static void analyze(Throwable e) {
        StackTraceElement[] trace = e.getStackTrace();
        System.out.println("Тип:        " + e.getClass().getName());
        System.out.println("Сообщение:  " + e.getMessage());
        System.out.println("Упало в:    " + trace[0]);
        System.out.println("Путь вызовов нашего кода (от места ошибки к main):");
        for (StackTraceElement el : trace) {
            if (el.getClassName().startsWith("Task01_StackTrace")) {
                System.out.println("  " + el.getMethodName() + "  (строка " + el.getLineNumber() + ")");
            }
        }
    }

    public static void main(String[] args) {
        String feed = "Java;2;49.90\nPython;1;39,90";

        try {
            Broken.totalPrice(feed);
        } catch (NumberFormatException e) {
            analyze(e);
        }

        System.out.println("После исправления: " + Fixed.totalPrice(feed));
    }
}
