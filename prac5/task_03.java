/*
 * Задание 3.
 * Постройте индекс `CourseId -> Course`, обработайте повторный ID
 * без молчаливой перезаписи.
 * Сравните `get`, `containsKey`, `putIfAbsent` и `computeIfAbsent`.
 *
 * Пояснения:
 * putIfAbsent + проверка результата:
 *   - повторный ID -> исключение, а не молчаливая перезапись
 *     (put перезаписывает).
 * get:
 *   - null и для 'нет ключа', и для значения null.
 * containsKey:
 *   - только наличие (два поиска вместе с get).
 * putIfAbsent:
 *   - значение создаётся заранее, возвращает существующее или null.
 * computeIfAbsent:
 *   - значение создаётся лениво только при отсутствии ключа,
 *     возвращает итоговое значение; map внутри функции менять нельзя.
 */

import java.util.*;

public class task_03 {

    record Course(String id, String title) {}

    static Map<String, Course> buildIndex(List<Course> courses) {
        Map<String, Course> index = new HashMap<>();

        for (Course course : courses) {
            Course previous = index.putIfAbsent(course.id(), course);

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Повторный CourseId '" + course.id() + "': уже есть "
                                + previous + ", пришёл " + course
                );
            }
        }

        return index;
    }

    public static void main(String[] args) {
        List<Course> ok = List.of(
                new Course("c1", "Java"),
                new Course("c2", "SQL")
        );
        System.out.println("Индекс: " + buildIndex(ok));

        List<Course> bad = List.of(
                new Course("c1", "Java"),
                new Course("c2", "SQL"),
                new Course("c1", "Kotlin")
        );

        try {
            buildIndex(bad);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        Map<String, Course> map = new HashMap<>();
        map.put("c1", new Course("c1", "Java"));

        Course old = map.put("c1", new Course("c1", "Kotlin"));
        System.out.println(
                "put вернул старое: " + old + ", теперь: " + map.get("c1")
        );

        System.out.println("get(нет) = " + map.get("zzz"));

        Map<String, String> withNull = new HashMap<>();
        withNull.put("k", null);
        System.out.println(
                "get(k)=" + withNull.get("k")
                        + ", но containsKey(k)=" + withNull.containsKey("k")
                        + "  <- get не различает 'нет' и 'null'"
        );

        System.out.println("containsKey(c1) = " + map.containsKey("c1"));

        Course r1 = map.putIfAbsent("c1", new Course("c1", "Scala"));
        Course r2 = map.putIfAbsent("c3", new Course("c3", "Go"));
        System.out.println(
                "putIfAbsent(c1) вернул существующий: " + r1
                        + "; putIfAbsent(c3) вернул null: " + r2
        );

        int[] calls = {0};

        map.computeIfAbsent("c1", k -> {
            calls[0]++;
            return new Course(k, "не вызовется");
        });

        Course created = map.computeIfAbsent("c4", k -> {
            calls[0]++;
            return new Course(k, "Rust");
        });

        System.out.println(
                "computeIfAbsent: функция вызвана раз = " + calls[0]
                        + ", создан " + created
        );

        Map<String, List<String>> byLevel = new TreeMap<>();

        for (String[] pair : new String[][]{
                {"basic", "Java"},
                {"adv", "JVM"},
                {"basic", "SQL"}
        }) {
            byLevel
                    .computeIfAbsent(pair[0], k -> new ArrayList<>())
                    .add(pair[1]);
        }

        System.out.println("Группировка: " + byLevel);
    }
}