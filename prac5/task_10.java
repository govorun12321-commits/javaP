/*
 * Задание 10.
 * Проверьте сущности Course в `HashSet` и как ключи `HashMap`.
 * Измените поле, не участвующее в равенстве, затем намеренно включите его
 * в hash и воспроизведите проблему.
 *
 * Пояснения:
 * - CourseA: equals/hashCode только по id - изменение title безопасно.
 * - CourseB: title в equals и hashCode, но изменяем:
 *     * после setTitle contains/get/remove не находят элемент
 *       (лежит в корзине по старому хэшу),
 *     * size не меняется,
 *     * повторный add добавляет тот же объект второй раз.
 *
 * Правило: ключи HashMap/HashSet неизменяемы по полям,
 * входящим в equals/hashCode.
 */

import java.util.*;

public class task_10 {

    static class CourseA {
        private final String id;
        private String title;

        CourseA(String id, String title) {
            this.id = id;
            this.title = title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof CourseA c && c.id.equals(id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "A(" + id + "," + title + ")";
        }
    }

    static class CourseB {
        private final String id;
        private String title;

        CourseB(String id, String title) {
            this.id = id;
            this.title = title;
        }

        void setTitle(String title) {
            this.title = title;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof CourseB c
                    && c.id.equals(id)
                    && c.title.equals(title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, title);
        }

        @Override
        public String toString() {
            return "B(" + id + "," + title + ")";
        }
    }

    public static void main(String[] args) {
        // 1. CourseA: изменение title безопасно
        CourseA a = new CourseA("c1", "Java");

        Set<CourseA> setA = new HashSet<>(List.of(a));
        Map<CourseA, String> mapA = new HashMap<>();
        mapA.put(a, "группа 1");

        a.setTitle("Java 21");

        System.out.println(
                "A: contains = " + setA.contains(a)
                        + ", map.get = " + mapA.get(a)
                        + ", найден по новому ключу c1: "
                        + setA.contains(new CourseA("c1", "что угодно"))
        );

        // 2. CourseB: изменение title ломает контракт
        CourseB b = new CourseB("c1", "Java");

        Set<CourseB> setB = new HashSet<>();
        setB.add(b);

        Map<CourseB, String> mapB = new HashMap<>();
        mapB.put(b, "группа 1");

        System.out.println(
                "B до изменения: contains = " + setB.contains(b)
                        + ", map.get = " + mapB.get(b)
        );

        b.setTitle("Java 21");

        System.out.println(
                "B после изменения: contains = " + setB.contains(b)
                        + ", map.get = " + mapB.get(b)
                        + ", remove = " + setB.remove(b)
        );
        System.out.println(
                "  но элемент всё ещё внутри: size = " + setB.size()
                        + ", при обходе: " + setB
        );

        setB.add(b);
        System.out.println(
                "  повторный add(b): size = " + setB.size()
                        + " -> один и тот же объект лежит дважды: " + setB
        );
    }
}