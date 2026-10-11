/*
 * Задание 9.
 * Создайте методы копирования из producer-списка в consumer-список.
 * Объясните, почему нельзя добавлять конкретный объект
 * в `List<? extends Course` и что разрешено читать
 * из `List<? super Course>`.
 *
 * Пояснения:
 * PECS: Producer Extends, Consumer Super.
 *   - src читаем -> ? extends T, dst пишем -> ? super T.
 *
 * В List<? extends Course> нельзя add (кроме null):
 *   реально там может быть List<ProgrammingCourse> или другой подтип,
 *   компилятор не может гарантировать совместимость.
 *
 * Из List<? super Course> можно писать Course и подтипы,
 * а читать - только как Object (реально может быть List<Object>).
 */

import java.util.*;

public class task_09 {

    static class Course {
        final String title;

        Course(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    static class ProgrammingCourse extends Course {
        ProgrammingCourse(String title) {
            super(title);
        }
    }

    static <T> void copy(List<? extends T> src, List<? super T> dst) {
        Objects.requireNonNull(src, "src");
        Objects.requireNonNull(dst, "dst");

        for (T item : src) {
            dst.add(item);
        }
    }

    static <T> int copyIf(
            Collection<? extends T> src,
            Collection<? super T> dst,
            java.util.function.Predicate<? super T> predicate
    ) {
        int count = 0;

        for (T item : src) {
            if (predicate.test(item)) {
                dst.add(item);
                count++;
            }
        }

        return count;
    }

    public static void main(String[] args) {
        List<ProgrammingCourse> programming = List.of(
                new ProgrammingCourse("Java"),
                new ProgrammingCourse("Kotlin")
        );

        List<Course> courses = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        // 1. copy: producer -> consumer
        copy(programming, courses);
        copy(programming, objects);

        System.out.println(
                "courses = " + courses + ", objects = " + objects
        );

        // 2. copyIf с предикатом
        int copied = copyIf(
                courses,
                new ArrayList<Object>(),
                c -> c.title.startsWith("J")
        );
        System.out.println("copyIf скопировал: " + copied);

        // 3. Чтение из producer-списка
        List<? extends Course> producer = programming;
        Course first = producer.get(0);
        System.out.println("Прочитали из List<? extends Course>: " + first);

        // 4. Запись и чтение из consumer-списка
        List<? super ProgrammingCourse> consumer = courses;
        consumer.add(new ProgrammingCourse("Scala"));

        Object any = consumer.get(0);
        System.out.println(
                "Из List<? super ProgrammingCourse> прочитали как Object: "
                        + any
                        + "; после add: " + courses
        );
    }
}