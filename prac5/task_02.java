/*
 * Задание 2.
    * Удалите дубликаты `CourseCode`, сохранив первый порядок появления.
    * Выберите реализацию set и объясните влияние `equals/hashCode`.
 *
 * Пояснения:
 * LinkedHashSet:
 *   - уникальность + порядок первого появления, add/contains O(1).
 * HashSet:
 *   - без порядка.
 * TreeSet:
 *   - сортировка, O(log n).
 *
 * HashSet:
 *   - сначала hashCode (корзина), потом equals.
 *   - без equals/hashCode равенство по ссылке;
 *   - equals без hashCode нарушает контракт -> дубликаты, contains=false.
 *
 * TreeSet:
 *   - опирается на compareTo, а не на equals/hashCode.
 */

import java.util.*;

public class task_02 {

    record CourseCode(String value) {
        CourseCode {
            Objects.requireNonNull(value, "value");
            value = value.trim().toUpperCase();

            if (value.isEmpty()) {
                throw new IllegalArgumentException("Пустой код курса");
            }
        }
    }

    static class IdentityCode {
        final String value;

        IdentityCode(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    static class HalfCode {
        final String value;

        HalfCode(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HalfCode h && h.value.equals(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static void main(String[] args) {
        List<CourseCode> raw = List.of(
                new CourseCode("cs101"),
                new CourseCode("CS102"),
                new CourseCode(" cs101 "),
                new CourseCode("CS103"),
                new CourseCode("cs102"),
                new CourseCode("CS104")
        );

        System.out.println("Вход: " + raw);

        // Правильное решение:
        Set<CourseCode> unique = new LinkedHashSet<>(raw);
        System.out.println("LinkedHashSet: " + unique);

        // Сравнение:
        System.out.println("HashSet (порядок не гарантирован): " + new HashSet<>(raw));

        Set<String> sorted = new TreeSet<>();
        for (CourseCode courseCode : raw) {
            sorted.add(courseCode.value());
        }
        System.out.println("TreeSet (по возрастанию, не по появлению): " + sorted);

        Set<IdentityCode> identitySet = new HashSet<>();
        identitySet.add(new IdentityCode("CS101"));
        identitySet.add(new IdentityCode("CS101"));
        System.out.println(
                "Без equals/hashCode размер = " + identitySet.size() + " (ожидали 1)"
        );

        Set<HalfCode> halfSet = new HashSet<>();
        HalfCode a = new HalfCode("CS101");
        HalfCode b = new HalfCode("CS101");

        halfSet.add(a);
        halfSet.add(b);

        System.out.println(
                "a.equals(b) = " + a.equals(b)
                        + ", но только equals без hashCode: размер = " + halfSet.size()
                        + " (обычно 2: разные корзины)"
        );
        System.out.println(
                "contains(новый равный) = "
                        + halfSet.contains(new HalfCode("CS101"))
                        + " (обычно false)"
        );
    }
}