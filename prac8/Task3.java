/*
Задание 3. reduce с правильной identity

Посчитайте сумму длительностей и объедините value objects.
Покажите неправильную identity, нарушающую результат,
и сформулируйте ассоциативность accumulator.

Объяснение: identity - это такое значение, что identity + x == x.
Для суммы это 0, для Total это Total(0, 0).
Если взять identity 5, то она добавится в результат один раз
при последовательной работе и по разу на каждую часть при разбиении
(например при parallel), поэтому результат зависит от разбиения.

Ассоциативность accumulator: (a + b) + c == a + (b + c).
Тогда неважно, как поток разбит на части и в каком порядке части склеены.
Сложение и объединение Total ассоциативны, а вычитание нет:
(10 - 5) - 2 = 3, но 10 - (5 - 2) = 7.
*/

import java.util.List;

public class Task3 {

    record Course(String code, int duration) {
    }

    record Total(int hours, int courses) {

        Total plus(Total other) {
            return new Total(hours + other.hours, courses + other.courses);
        }
    }

    public static void main(String[] args) {
        List<Course> courses = List.of(
                new Course("A", 40),
                new Course("B", 30),
                new Course("C", 25),
                new Course("D", 60));

        int sum = courses.stream()
                .map(Course::duration)
                .reduce(0, Integer::sum);
        System.out.println("Сумма: " + sum);

        Total total = courses.stream()
                .reduce(new Total(0, 0),
                        (acc, c) -> acc.plus(new Total(c.duration(), 1)),
                        Total::plus);
        System.out.println("Total: " + total);

        Total badIdentity = new Total(5, 0);
        List<Course> left = courses.subList(0, 2);
        List<Course> right = courses.subList(2, 4);

        Total whole = courses.stream()
                .reduce(badIdentity,
                        (acc, c) -> acc.plus(new Total(c.duration(), 1)),
                        Total::plus);
        Total leftPart = left.stream()
                .reduce(badIdentity,
                        (acc, c) -> acc.plus(new Total(c.duration(), 1)),
                        Total::plus);
        Total rightPart = right.stream()
                .reduce(badIdentity,
                        (acc, c) -> acc.plus(new Total(c.duration(), 1)),
                        Total::plus);
        Total glued = leftPart.plus(rightPart);

        System.out.println("identity 5, целиком: " + whole.hours());
        System.out.println("identity 5, две части: " + glued.hours());

        int minusWhole = courses.stream()
                .map(Course::duration)
                .reduce(0, (a, b) -> a - b);
        int minusLeft = left.stream().map(Course::duration).reduce(0, (a, b) -> a - b);
        int minusRight = right.stream().map(Course::duration).reduce(0, (a, b) -> a - b);
        System.out.println("вычитание, целиком: " + minusWhole);
        System.out.println("вычитание, две части: " + (minusLeft - minusRight));
    }
}
