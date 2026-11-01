/*
Задание 5. Optional как результат

Найдите самый длинный курс, преобразуйте результат и задайте fallback.
Не используйте Optional.get без доказанного наличия
и не храните Optional в поле сущности.

Объяснение: метод findLongest возвращает Optional, потому что в пустом списке
самого длинного курса нет. Результат преобразуется через map,
а fallback задаётся через orElse, поэтому get не нужен.
Поле сущности Course обычное (String, int), Optional в нём не хранится.
При одинаковой длительности побеждает курс с меньшим code, чтобы результат был однозначным.
*/

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Task5 {

    record Course(String code, String title, int duration) {
    }

    static Optional<Course> findLongest(List<Course> courses) {
        return courses.stream()
                .max(Comparator.comparingInt(Course::duration)
                        .thenComparing(Course::code, Comparator.reverseOrder()));
    }

    static String describe(List<Course> courses) {
        return findLongest(courses)
                .map(course -> course.title().toUpperCase() + " (" + course.duration() + " ч)")
                .orElse("курсов нет");
    }

    public static void main(String[] args) {
        List<Course> courses = List.of(
                new Course("JV-101", "Java basics", 40),
                new Course("JV-302", "Concurrency", 60),
                new Course("DB-201", "Databases", 60),
                new Course("GT-100", "Git", 10));

        System.out.println("Самый длинный: " + describe(courses));
        System.out.println("Пустой список: " + describe(List.of()));
    }
}
