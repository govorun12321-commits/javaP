/*
 * Задание 7.
 * Напишите `firstOrThrow(List<T>)`, `swap(List<T>, int, int)` и
 * `indexBy(List<T>, Function<T,K>)`.
 * Последний метод должен явно обрабатывать конфликт ключей.
 *
 * Пояснения:
 * - firstOrThrow читает -> List<? extends T>;
 *   swap читает и пишет -> точный List<T>.
 * - indexBy: конфликт ключей явный - по умолчанию IllegalStateException,
 *   либо политика через BinaryOperator. Сложность O(n).
 */

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class task_07 {

    private task_07() {}

    static <T> T firstOrThrow(List<? extends T> list) {
        Objects.requireNonNull(list, "list");

        if (list.isEmpty()) {
            throw new NoSuchElementException("Список пуст");
        }

        return list.get(0);
    }

    static <T> void swap(List<T> list, int i, int j) {
        Objects.requireNonNull(list, "list");
        Objects.checkIndex(i, list.size());
        Objects.checkIndex(j, list.size());

        if (i == j) {
            return;
        }

        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    static <T, K> Map<K, T> indexBy(
            List<T> list,
            Function<? super T, ? extends K> keyFn
    ) {
        return indexBy(list, keyFn, (existing, incoming) -> {
            throw new IllegalStateException(
                    "Конфликт ключа: " + existing + " и " + incoming
            );
        });
    }

    static <T, K> Map<K, T> indexBy(
            List<T> list,
            Function<? super T, ? extends K> keyFn,
            BinaryOperator<T> onConflict
    ) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(keyFn, "keyFn");
        Objects.requireNonNull(onConflict, "onConflict");

        Map<K, T> result = new LinkedHashMap<>();

        for (T item : list) {
            K key = Objects.requireNonNull(
                    keyFn.apply(item),
                    "ключ не может быть null"
            );

            T existing = result.get(key);
            result.put(
                    key,
                    existing == null ? item : onConflict.apply(existing, item)
            );
        }

        return result;
    }

    record Course(String code, String title) {}

    public static void main(String[] args) {
        // 1. firstOrThrow
        List<String> names = new ArrayList<>(List.of("Anna", "Boris", "Clara"));
        System.out.println("firstOrThrow: " + firstOrThrow(names));

        try {
            firstOrThrow(List.of());
        } catch (NoSuchElementException e) {
            System.out.println("пустой -> " + e.getMessage());
        }

        // 2. swap
        swap(names, 0, 2);
        System.out.println("swap(0,2): " + names);

        try {
            swap(names, 0, 5);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("плохой индекс -> " + e.getMessage());
        }

        try {
            swap(List.of(1, 2), 0, 1);
        } catch (UnsupportedOperationException e) {
            System.out.println("неизменяемый список -> UnsupportedOperationException");
        }

        // 3. indexBy
        List<Course> courses = List.of(
                new Course("CS1", "Java"),
                new Course("CS2", "SQL"),
                new Course("CS1", "Kotlin")
        );

        try {
            indexBy(courses, Course::code);
        } catch (IllegalStateException e) {
            System.out.println("indexBy (по умолчанию): " + e.getMessage());
        }

        System.out.println(
                "оставить первый:    "
                        + indexBy(courses, Course::code, (old, nw) -> old)
        );
        System.out.println(
                "оставить последний: "
                        + indexBy(courses, Course::code, (old, nw) -> nw)
        );
        System.out.println(
                "без конфликта:      "
                        + indexBy(
                                List.of(
                                        new Course("A", "x"),
                                        new Course("B", "y")
                                ),
                                Course::code
                        )
        );
    }
}