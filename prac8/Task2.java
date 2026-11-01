/*
Задание 2. flatMap

Из курсов со списком модулей получите уникальные нормализованные темы
и обратный индекс topic → courses. Не создавайте вложенный список вручную.

Объяснение: flatMap превращает каждый курс в поток его тем,
поэтому плоский список получается без ручных вложенных циклов.
Тема нормализуется (trim и нижний регистр), а distinct убирает повторы.
Для обратного индекса каждый курс разворачивается в пары (тема, код курса),
и groupingBy собирает пары в карту тема -> курсы.
*/

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Task2 {

    record Module(String name, String topic) {
    }

    record Course(String code, List<Module> modules) {
    }

    static String normalize(String topic) {
        return topic.trim().toLowerCase();
    }

    public static void main(String[] args) {
        List<Course> courses = List.of(
                new Course("JV-101", List.of(
                        new Module("m1", " Variables "),
                        new Module("m2", "Loops"),
                        new Module("m3", "loops"))),
                new Course("JV-301", List.of(
                        new Module("m1", "STREAMS"),
                        new Module("m2", "lambda "),
                        new Module("m3", "Loops"))),
                new Course("DB-201", List.of(
                        new Module("m1", "SQL"),
                        new Module("m2", "streams"))));

        List<String> topics = courses.stream()
                .flatMap(course -> course.modules().stream())
                .map(module -> normalize(module.topic()))
                .distinct()
                .sorted()
                .toList();

        Map<String, TreeSet<String>> index = courses.stream()
                .flatMap(course -> course.modules().stream()
                        .map(module -> Map.entry(normalize(module.topic()), course.code())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        TreeMap::new,
                        Collectors.mapping(Map.Entry::getValue,
                                Collectors.toCollection(TreeSet::new))));

        System.out.println("Темы: " + topics);
        index.forEach((topic, codes) -> System.out.println(topic + " -> " + codes));
    }
}
