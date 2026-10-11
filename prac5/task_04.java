/*
 * Задание 4.
 * Реализуйте FIFO waitlist через `Queue`: добавить, посмотреть следующего,
 * извлечь, обработать пустую очередь.
 * Объясните пары `add/offer`, `remove/poll`, `element/peek`.
 *
 * Пояснения:
 * add/offer:
 *   - при переполнении ограниченной очереди add бросает IllegalStateException,
 *     offer возвращает false (у ArrayDeque различий нет).
 * remove/poll:
 *   - на пустой очереди remove бросает NoSuchElementException,
 *     poll возвращает null.
 * element/peek:
 *   - то же, но без извлечения элемента.
 *
 * Пустая очередь — штатный случай, поэтому Waitlist возвращает Optional.
 * null в очередь класть нельзя.
 */

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class task_04 {

    static final class Waitlist {
        private final Queue<String> queue = new ArrayDeque<>();

        void join(String student) {
            Objects.requireNonNull(student, "student");
            queue.add(student);
        }

        Optional<String> peekNext() {
            return Optional.ofNullable(queue.peek());
        }

        Optional<String> serveNext() {
            return Optional.ofNullable(queue.poll());
        }

        int size() {
            return queue.size();
        }
    }

    public static void main(String[] args) {
        Waitlist waitlist = new Waitlist();

        waitlist.join("Anna");
        waitlist.join("Boris");
        waitlist.join("Clara");

        System.out.println(
                "Следующий (без извлечения): "
                        + waitlist.peekNext().orElse("-")
                        + ", размер="
                        + waitlist.size()
        );

        System.out.println("Обслужен: " + waitlist.serveNext().orElse("-"));
        System.out.println("Обслужен: " + waitlist.serveNext().orElse("-"));
        System.out.println("Обслужен: " + waitlist.serveNext().orElse("-"));

        System.out.println(
                "Пустая очередь -> peekNext="
                        + waitlist.peekNext()
                        + ", serveNext="
                        + waitlist.serveNext()
        );

        Queue<String> empty = new ArrayDeque<>();

        System.out.println(
                "peek() = " + empty.peek()
                        + ", poll() = " + empty.poll()
        );

        try {
            empty.remove();
        } catch (NoSuchElementException e) {
            System.out.println("remove() -> NoSuchElementException");
        }

        try {
            empty.element();
        } catch (NoSuchElementException e) {
            System.out.println("element() -> NoSuchElementException");
        }

        Queue<String> bounded = new ArrayBlockingQueue<>(2);

        System.out.println(
                "offer 1: " + bounded.offer("a")
                        + ", offer 2: " + bounded.offer("b")
                        + ", offer 3 (переполнение): " + bounded.offer("c")
        );

        try {
            bounded.add("c");
        } catch (IllegalStateException e) {
            System.out.println(
                    "add 3 -> IllegalStateException: " + e.getMessage()
            );
        }
    }
}