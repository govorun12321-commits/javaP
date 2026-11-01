/*
Задание 7. Однопроходный отчёт CourseHub

Из большого потока enrollment-событий сформируйте уникальных студентов,
completion rate по курсам, top-3, распределение по level и список аномалий.
Данные могут поступать только один раз. Результат детерминирован,
память ограничена требованиями, side effects отсутствуют.
Объясните, где Stream API уместен, а где обычный цикл яснее.

Объяснение: поток читается один раз, события нигде не хранятся.
Один собственный collector ведёт всё сразу: множество студентов,
счётчики по курсам, счётчики по level и первые аномалии.
Память зависит только от числа уникальных студентов (их нужно помнить,
чтобы не посчитать дважды), числа курсов и числа level.
Список аномалий ограничен пятью первыми, остальные только считаются.
Поток последовательный и упорядоченный, поэтому результат всегда одинаковый,
а в top-3 при равном rate курсы сортируются по code.
Внутри collector изменяются только его собственные объекты, внешнего состояния нет.

Где уместен Stream API: когда данные идут конвейером
(фильтр, преобразование, группировка, подсчёт) и результат собирается
готовыми collectors, как в заданиях 2 и 4.
Где обычный цикл яснее: когда за один проход меняется много разных
переменных, есть ранний выход или сложные условия.
Тогда логика в collector разбивается на supplier, accumulator и combiner,
и читать её тяжелее, чем обычный for с несколькими переменными.
Здесь collector оправдан только тем, что нужно подчеркнуть работу с потоком,
который нельзя прочитать дважды.
*/

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Task7 {

    enum Level {
        BEGINNER, MIDDLE, ADVANCED
    }

    enum Status {
        STARTED, COMPLETED
    }

    record Event(int id, String studentId, String courseCode, Level level, Status status, int score) {
    }

    record CourseRate(String code, double rate) {
    }

    record Report(int students, List<CourseRate> top3, Map<Level, Integer> levels,
                  List<String> anomalies, int anomalyTotal) {
    }

    static final int ANOMALY_LIMIT = 5;

    static class Acc {
        Set<String> students = new HashSet<>();
        Map<String, int[]> courses = new HashMap<>();
        Map<Level, Integer> levels = new EnumMap<>(Level.class);
        List<String> anomalies = new ArrayList<>();
        int anomalyTotal = 0;

        void addAnomaly(String text) {
            anomalyTotal++;
            if (anomalies.size() < ANOMALY_LIMIT) {
                anomalies.add(text);
            }
        }

        void add(Event e) {
            if (e.studentId().isBlank()) {
                addAnomaly("событие " + e.id() + ": пустой студент");
            } else {
                students.add(e.studentId());
            }
            if (e.score() < 0 || e.score() > 100) {
                addAnomaly("событие " + e.id() + ": плохая оценка " + e.score());
            }
            int[] counters = courses.computeIfAbsent(e.courseCode(), k -> new int[2]);
            counters[0]++;
            if (e.status() == Status.COMPLETED) {
                counters[1]++;
            }
            levels.merge(e.level(), 1, Integer::sum);
        }

        Acc merge(Acc other) {
            students.addAll(other.students);
            other.courses.forEach((code, counters) -> {
                int[] mine = courses.computeIfAbsent(code, k -> new int[2]);
                mine[0] += counters[0];
                mine[1] += counters[1];
            });
            other.levels.forEach((level, count) -> levels.merge(level, count, Integer::sum));
            anomalyTotal += other.anomalyTotal;
            for (String text : other.anomalies) {
                if (anomalies.size() < ANOMALY_LIMIT) {
                    anomalies.add(text);
                }
            }
            return this;
        }

        Report toReport() {
            List<CourseRate> top3 = courses.entrySet().stream()
                    .map(entry -> new CourseRate(entry.getKey(),
                            (double) entry.getValue()[1] / entry.getValue()[0]))
                    .sorted(Comparator.comparingDouble(CourseRate::rate).reversed()
                            .thenComparing(CourseRate::code))
                    .limit(3)
                    .toList();
            return new Report(students.size(), top3, Map.copyOf(levels),
                    List.copyOf(anomalies), anomalyTotal);
        }
    }

    static final String[] CODES = {"JV-101", "JV-301", "JV-302", "DB-201", "SQL-201", "GT-100", "PY-101", "WB-150"};
    static final Level[] LEVELS = {Level.BEGINNER, Level.ADVANCED, Level.ADVANCED, Level.MIDDLE,
            Level.MIDDLE, Level.BEGINNER, Level.BEGINNER, Level.MIDDLE};

    static Event makeEvent(int i) {
        int course = i % 8;
        String student = (i % 50000 == 777) ? "" : "s" + (i % 5000);
        Status status = ((i / 8) % 10 < course + 1) ? Status.COMPLETED : Status.STARTED;
        int score = (i % 40000 == 5) ? 150 : (i % 101);
        return new Event(i, student, CODES[course], LEVELS[course], status, score);
    }

    public static void main(String[] args) {
        Stream<Event> events = IntStream.range(0, 200_000).mapToObj(Task7::makeEvent);

        Collector<Event, Acc, Report> collector =
                Collector.of(Acc::new, Acc::add, Acc::merge, Acc::toReport);

        Report report = events.collect(collector);

        System.out.println("Уникальных студентов: " + report.students());
        System.out.println("Top-3:");
        for (CourseRate r : report.top3()) {
            System.out.println("  " + r.code() + " " + String.format("%.1f", r.rate() * 100) + "%");
        }
        System.out.println("По level: " + new EnumMap<>(report.levels()));
        System.out.println("Аномалий: " + report.anomalyTotal());
        report.anomalies().forEach(a -> System.out.println("  " + a));
    }
}
