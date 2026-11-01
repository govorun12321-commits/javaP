/*
Задание 4. Collectors

Сгруппируйте курсы по level, посчитайте количество и среднюю длительность,
соберите summary object. Не выполняйте три полных прохода без объяснения.

Объяснение: используется один проход по списку.
groupingBy раскладывает курсы по level, а downstream-коллектор teeing
одновременно считает количество и среднюю длительность
и сразу собирает из них LevelSummary.
Три отдельных прохода (группировка, count, average) читали бы данные три раза,
а для большого потока это было бы лишней работой.
*/

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Task4 {

    enum Level {
        BEGINNER, MIDDLE, ADVANCED
    }

    record Course(String code, Level level, int duration) {
    }

    record LevelSummary(long count, double averageDuration) {
    }

    public static void main(String[] args) {
        List<Course> courses = List.of(
                new Course("JV-101", Level.BEGINNER, 40),
                new Course("GT-100", Level.BEGINNER, 10),
                new Course("SQL-201", Level.MIDDLE, 30),
                new Course("DB-201", Level.MIDDLE, 35),
                new Course("JV-301", Level.ADVANCED, 25),
                new Course("JV-302", Level.ADVANCED, 60),
                new Course("JV-303", Level.ADVANCED, 50));

        Map<Level, LevelSummary> summary = courses.stream()
                .collect(Collectors.groupingBy(
                        Course::level,
                        TreeMap::new,
                        Collectors.teeing(
                                Collectors.counting(),
                                Collectors.averagingInt(Course::duration),
                                LevelSummary::new)));

        summary.forEach((level, s) -> System.out.println(
                level + ": count=" + s.count() + ", avg=" + String.format("%.1f", s.averageDuration())));
    }
}
