/*
 * Задание 5. Try-with-resource
 *
 * Создайте тестовый `AutoCloseable`, логирующий open/use/close. Покажите закрытие при
 * успехе и исключении. Затем создайте ошибку и в работе, и в `close`, исследуйте
 * suppressed exceptions.
 *
 * Краткое объяснение:
 *  - Ресурсы закрываются автоматически, в ОБРАТНОМ порядке открытия, и при успехе, и при исключении.
 *  - Если упали и тело, и close(), "главным" становится исключение из тела, а исключение
 *    из close() добавляется через addSuppressed и доступно в getSuppressed().
 *  - Если упал только close() — оно и будет основным.
 *  - В ручном try/finally второе исключение затёрло бы первое (см. задание 4).
 */

import java.util.ArrayList;
import java.util.List;

public final class Task05_TryWithResources {

    static final class TestResource implements AutoCloseable {
        private final String name;
        private final List<String> log;
        private final boolean failOnUse;
        private final boolean failOnClose;

        TestResource(String name, List<String> log, boolean failOnUse, boolean failOnClose) {
            this.name = name;
            this.log = log;
            this.failOnUse = failOnUse;
            this.failOnClose = failOnClose;
            log.add("open " + name);
        }

        void use() {
            log.add("use " + name);
            if (failOnUse) {
                throw new IllegalStateException("use failed: " + name);
            }
        }

        @Override
        public void close() {
            log.add("close " + name);
            if (failOnClose) {
                throw new IllegalStateException("close failed: " + name);
            }
        }
    }

    static void scenario(String title, boolean failUse, boolean failClose) {
        List<String> log = new ArrayList<>();
        System.out.println("== " + title);
        try (TestResource r = new TestResource("R", log, failUse, failClose)) {
            r.use();
        } catch (IllegalStateException e) {
            System.out.println("основное: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) {
                System.out.println("  suppressed: " + s.getMessage());
            }
        }
        System.out.println("log: " + log + "\n");
    }

    static void twoResourcesAllFail() {
        List<String> log = new ArrayList<>();
        System.out.println("== два ресурса, падают и use, и оба close");
        try (TestResource a = new TestResource("A", log, false, true);
             TestResource b = new TestResource("B", log, false, true)) {
            b.use();
            throw new IllegalStateException("body failed");
        } catch (IllegalStateException e) {
            System.out.println("основное: " + e.getMessage() + ", suppressed=" + e.getSuppressed().length);
            for (Throwable s : e.getSuppressed()) {
                System.out.println("  suppressed: " + s.getMessage());
            }
        }
        System.out.println("log (close в обратном порядке: B, затем A): " + log);
    }

    public static void main(String[] args) {
        scenario("успех", false, false);
        scenario("исключение в работе (close всё равно вызван)", true, false);
        scenario("исключение только в close (оно основное)", false, true);
        scenario("ошибка и в работе, и в close", true, true);
        twoResourcesAllFail();
    }
}
