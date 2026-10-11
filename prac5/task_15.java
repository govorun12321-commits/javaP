/*
 * Задание 15.
 * Курсы образуют ориентированный граф зависимостей.
 * Постройте generic-модель графа на map/set, найдите допустимый порядок
 * изучения или цикл, выдайте диагностический путь цикла.
 * Результат детерминирован при одинаковом вводе.
 * Защитите выбор коллекций, сложности и поведение при отсутствующей вершине.
 *
 * Пояснения:
 * - Граф: LinkedHashMap<V, LinkedHashSet<V>> (курс -> prerequisites):
 *     порядок добавления = детерминированный результат;
 *     HashSet только для проверок принадлежности.
 * - DFS в постпорядке даёт порядок изучения;
 *   ребро в вершину на текущем пути = цикл (путь берётся срезом).
 * - Сложность O(V+E) времени, O(V) памяти.
 *   Рекурсия: глубина = длина цепочки.
 * - Отсутствующая вершина: NoSuchElementException (защита от опечаток),
 *   null: NPE.
 */

import java.util.*;

public class task_15 {

    static PrereqGraph<String> build(String... courses) {
        PrereqGraph<String> graph = new PrereqGraph<>();

        for (String course : courses) {
            graph.addCourse(course);
        }

        return graph;
    }

    public static void main(String[] args) {
        PrereqGraph<String> g = build(
                "Algorithms", "Java", "OOP", "Collections", "Generics"
        );
        g.addPrerequisite("OOP", "Java");
        g.addPrerequisite("Collections", "OOP");
        g.addPrerequisite("Generics", "OOP");
        g.addPrerequisite("Algorithms", "Collections");

        System.out.println("Ациклический: " + g.plan());

        PrereqGraph<String> g2 = build(
                "Algorithms", "Java", "OOP", "Collections", "Generics"
        );
        g2.addPrerequisite("OOP", "Java");
        g2.addPrerequisite("Collections", "OOP");
        g2.addPrerequisite("Generics", "OOP");
        g2.addPrerequisite("Algorithms", "Collections");

        System.out.println(
                "Детерминированность: " + g.plan().equals(g2.plan())
        );

        PrereqGraph<String> cyclic = build("A", "B", "C", "D");
        cyclic.addPrerequisite("A", "B");
        cyclic.addPrerequisite("B", "C");
        cyclic.addPrerequisite("C", "A");
        cyclic.addPrerequisite("D", "A");

        Plan<String> plan = cyclic.plan();
        System.out.println("С циклом: " + plan);

        if (plan instanceof Plan.Cycle<String> cycle) {
            System.out.println(
                    "  Путь: " + String.join(" требует ", cycle.path())
            );
        }

        PrereqGraph<String> self = build("X");
        self.addPrerequisite("X", "X");
        System.out.println("Self-loop: " + self.plan());

        try {
            g.addPrerequisite("OOP", "Kotlin");
        } catch (NoSuchElementException e) {
            System.out.println("addPrerequisite: " + e.getMessage());
        }

        try {
            g.prerequisitesOf("Rust");
        } catch (NoSuchElementException e) {
            System.out.println("prerequisitesOf: " + e.getMessage());
        }

        System.out.println("Пустой граф: " + new PrereqGraph<String>().plan());
    }
}

sealed interface Plan<V> {
    record Order<V>(List<V> courses) implements Plan<V> {}

    record Cycle<V>(List<V> path) implements Plan<V> {}
}

final class PrereqGraph<V> {

    private final Map<V, Set<V>> prerequisites = new LinkedHashMap<>();

    void addCourse(V course) {
        Objects.requireNonNull(course, "course");
        prerequisites.computeIfAbsent(course, k -> new LinkedHashSet<>());
    }

    void addPrerequisite(V course, V prerequisite) {
        requireVertex(course);
        requireVertex(prerequisite);
        prerequisites.get(course).add(prerequisite);
    }

    Set<V> prerequisitesOf(V course) {
        requireVertex(course);
        return Collections.unmodifiableSet(prerequisites.get(course));
    }

    private void requireVertex(V vertex) {
        Objects.requireNonNull(vertex, "vertex");

        if (!prerequisites.containsKey(vertex)) {
            throw new NoSuchElementException(
                    "Нет курса в графе: " + vertex
            );
        }
    }

    Plan<V> plan() {
        Set<V> done = new HashSet<>();
        List<V> path = new ArrayList<>();
        Set<V> onPath = new HashSet<>();
        List<V> order = new ArrayList<>();

        for (V start : prerequisites.keySet()) {
            List<V> cycle = visit(start, done, path, onPath, order);

            if (cycle != null) {
                return new Plan.Cycle<>(cycle);
            }
        }

        return new Plan.Order<>(List.copyOf(order));
    }

    private List<V> visit(
            V vertex,
            Set<V> done,
            List<V> path,
            Set<V> onPath,
            List<V> order
    ) {
        if (done.contains(vertex)) {
            return null;
        }

        path.add(vertex);
        onPath.add(vertex);

        for (V prerequisite : prerequisites.get(vertex)) {
            if (onPath.contains(prerequisite)) {
                List<V> cycle = new ArrayList<>(
                        path.subList(
                                path.indexOf(prerequisite),
                                path.size()
                        )
                );
                cycle.add(prerequisite);
                return cycle;
            }

            List<V> cycle = visit(
                    prerequisite, done, path, onPath, order
            );

            if (cycle != null) {
                return cycle;
            }
        }

        path.remove(path.size() - 1);
        onPath.remove(vertex);
        done.add(vertex);
        order.add(vertex);

        return null;
    }
}