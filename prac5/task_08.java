/*
 * Задание 8.
 * Реализуйте `max` для элементов `Comparable`.
 * Корректно сформулируйте bound с учётом подтипов.
 * Проверьте на `Integer`, `String` и собственном value object.
 *
 * Пояснения:
 * bound: T extends Comparable<? super T>.
 * Apple extends Fruit, Fruit implements Comparable<Fruit>:
 *   - Apple сравним как Fruit, но не является Comparable<Apple>,
 *   - поэтому строгий T extends Comparable<T> отвергает такой тип:
 *         static <T extends Comparable<T>> T maxStrict(Collection<? extends T> c)
 *         Apple a = maxStrict(List.of(new Apple()));   // ошибка компиляции: incompatible bounds
 *
 * Collection<? extends T> - коллекцию только читаем (producer).
 */

import java.util.*;

public class task_08 {

    static <T extends Comparable<? super T>> T max(
            Collection<? extends T> items
    ) {
        Objects.requireNonNull(items, "items");

        Iterator<? extends T> it = items.iterator();

        if (!it.hasNext()) {
            throw new NoSuchElementException("Коллекция пуста");
        }

        T best = it.next();

        while (it.hasNext()) {
            T cur = it.next();

            if (cur.compareTo(best) > 0) {
                best = cur;
            }
        }

        return best;
    }

    record Grade(int points) implements Comparable<Grade> {
        Grade {
            if (points < 0 || points > 100) {
                throw new IllegalArgumentException("0..100");
            }
        }

        @Override
        public int compareTo(Grade o) {
            return Integer.compare(points, o.points);
        }
    }

    static class Fruit implements Comparable<Fruit> {
        final int weight;

        Fruit(int weight) {
            this.weight = weight;
        }

        @Override
        public int compareTo(Fruit o) {
            return Integer.compare(weight, o.weight);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + weight + ")";
        }
    }

    static class Apple extends Fruit {
        Apple(int weight) {
            super(weight);
        }
    }

    public static void main(String[] args) {
        System.out.println("Integer: " + max(List.of(3, 9, 4)));
        System.out.println("String:  " + max(List.of("kiwi", "pear", "apple")));
        System.out.println(
                "Grade:   "
                        + max(Set.of(new Grade(70), new Grade(95), new Grade(88)))
        );

        Apple heaviest = max(
                List.of(new Apple(120), new Apple(180), new Apple(150))
        );
        System.out.println("Apple:   " + heaviest);

        try {
            max(List.<Integer>of());
        } catch (NoSuchElementException e) {
            System.out.println("пусто -> " + e.getMessage());
        }
    }
}