/*
Задание 1. Сортировка и детерминизм

Отсортируйте по level, duration и code с явными tie-breakers.
Результат одинаков для разных исходных порядков, если множество данных одинаково.

Объяснение: компаратор сравнивает level, потом duration, потом code.
Если все три поля равны, добавлен последний tie-breaker по title.
Так у любых двух разных курсов порядок всегда определён,
поэтому перемешивание входного списка не влияет на результат.
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Task1 {

    enum Level {
        BEGINNER, MIDDLE, ADVANCED
    }

    record Course(String code, String title, Level level, int duration) {
    }

    public static void main(String[] args) {
        List<Course> courses = List.of(
                new Course("JV-101", "Java basics", Level.BEGINNER, 40),
                new Course("PY-101", "Python basics", Level.BEGINNER, 40),
                new Course("SQL-201", "SQL", Level.MIDDLE, 30),
                new Course("DB-201", "Databases", Level.MIDDLE, 30),
                new Course("JV-301", "Streams", Level.ADVANCED, 25),
                new Course("JV-302", "Concurrency", Level.ADVANCED, 60),
                new Course("GT-100", "Git", Level.BEGINNER, 10),
                new Course("GT-100", "Git advanced", Level.BEGINNER, 10));

        Comparator<Course> comparator = Comparator
                .comparing(Course::level)
                .thenComparingInt(Course::duration)
                .thenComparing(Course::code)
                .thenComparing(Course::title);

        List<Course> first = courses.stream().sorted(comparator).toList();

        boolean allSame = true;
        for (int seed = 1; seed <= 100; seed++) {
            List<Course> mixed = new ArrayList<>(courses);
            Collections.shuffle(mixed, new Random(seed));
            List<Course> result = mixed.stream().sorted(comparator).toList();
            if (!result.equals(first)) {
                allSame = false;
            }
        }

        for (Course c : first) {
            System.out.println(c.level() + " " + c.duration() + " " + c.code());
        }
        System.out.println("Одинаково для 100 перемешиваний: " + allSame);
    }
}
