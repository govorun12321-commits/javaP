/*
 * Задание 4. try-catch-finally
 *
 * Предскажите порядок выполнения для return и throw внутри `try`, `catch` и `finally`.
 * Не возвращайте значение из `finally`. Объясните, как такой return способен скрыть исключение.
 *
 * Краткое объяснение:
 *  1. finally выполняется ВСЕГДА (кроме System.exit / падения JVM), после try/catch и до выхода из метода.
 *  2. return в try/catch: возвращаемое значение вычисляется ДО finally. Примитив/ссылка уже
 *     «зафиксированы»; finally может изменить лишь содержимое изменяемого объекта.
 *  3. throw в finally заменяет исходное исключение — оригинал теряется.
 *  4. return в finally отменяет и ранее вычисленный return, и летящее исключение: метод
 *     завершается «нормально», и ошибка исчезает бесследно. Поэтому finally используется только
 *     для освобождения ресурсов (лучше — try-with-resources), без return и без throw.
 */
public final class Task04_TryCatchFinally {

    static String orderNoException() {
        StringBuilder log = new StringBuilder();
        try {
            log.append("try ");
        } catch (RuntimeException e) {
            log.append("catch ");
        } finally {
            log.append("finally");
        }
        return log.toString();               
    }

    static String orderWithException() {
        StringBuilder log = new StringBuilder();
        try {
            log.append("try ");
            throw new IllegalStateException("boom");
        } catch (IllegalStateException e) {
            log.append("catch ");
        } finally {
            log.append("finally");
        }
        return log.toString();              
    }

    static String returnInTry(StringBuilder log) {
        try {
            log.append("try-return ");
            return "value-from-try";
        } finally {
            log.append("finally");               
        }
    }

    static String returnInCatch(StringBuilder log) {
        try {
            log.append("try ");
            throw new IllegalStateException("boom");
        } catch (IllegalStateException e) {
            log.append("catch-return ");
            return "value-from-catch";
        } finally {
            log.append("finally");
        }
    }

    static int primitiveFixedBeforeFinally() {
        int x = 1;
        try {
            return x;                              
        } finally {
            x = 99;                               
        }
    }

    static int[] mutableObjectChangedInFinally() {
        int[] data = {1};
        try {
            return data;                         
        } finally {
            data[0] = 99;                         
        }
    }

    static void throwInFinallyReplacesOriginal() {
        try {
            throw new IllegalStateException("original");
        } finally {
            throw new IllegalArgumentException("from finally");  
        }
    }

    @SuppressWarnings("finally")
    static int returnInFinallyHidesException() {
        try {
            throw new IllegalStateException("important failure");
        } finally {
            return -1;                              
        }
    }

    static String correctCleanup(StringBuilder log) {
        try {
            log.append("work ");
            return "result";
        } finally {
            log.append("cleanup");                 
        }
    }

    public static void main(String[] args) {
        System.out.println("no exception:   " + orderNoException());
        System.out.println("with exception: " + orderWithException());

        StringBuilder l1 = new StringBuilder();
        System.out.println("return in try:   " + returnInTry(l1) + " | log: " + l1);

        StringBuilder l2 = new StringBuilder();
        System.out.println("return in catch: " + returnInCatch(l2) + " | log: " + l2);

        System.out.println("primitive:       " + primitiveFixedBeforeFinally());
        System.out.println("mutable array:   " + mutableObjectChangedInFinally()[0]);

        try {
            throwInFinallyReplacesOriginal();
        } catch (RuntimeException e) {
            System.out.println("throw in finally -> пойман: " + e.getMessage()
                    + ", suppressed=" + e.getSuppressed().length + " (original потерян)");
        }

        System.out.println("return in finally -> " + returnInFinallyHidesException()
                + " (исключение 'important failure' бесследно исчезло)");

        StringBuilder l3 = new StringBuilder();
        System.out.println("correct:         " + correctCleanup(l3) + " | log: " + l3);
    }
}
