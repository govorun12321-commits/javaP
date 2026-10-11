/*
 * Задание 13.
 * По курсам и тегам постройте `Map<Tag, Set<CourseId>>`;
 * поддержите добавление, удаление курса и пересечение нескольких тегов.
 * Пустые set не остаются в индексе.
 * Обоснуйте реализацию каждой коллекции.
 *
 * Пояснения:
 * - byTag: HashMap (нужен только поиск по ключу).
 *   Значения: TreeSet<CourseId> - детерминированный отсортированный результат.
 * - tagsOfCourse: HashMap<CourseId, Set<Tag>> - удаление курса за O(k)
 *   без обхода всего индекса.
 * - Пустой set удаляется вместе с тегом.
 * - Пересечение начинается с наименьшего множества.
 */

import java.util.*;

public class task_13 {

    static Tag t(String s) {
        return new Tag(s);
    }

    static CourseId c(int i) {
        return new CourseId(i);
    }

    public static void main(String[] args) {
        TagIndex index = new TagIndex();

        index.addCourse(c(1), List.of(t("java"), t("oop"), t("beginner")));
        index.addCourse(c(2), List.of(t("java"), t("streams")));
        index.addCourse(c(3), List.of(t("Java"), t("OOP"), t("advanced")));

        System.out.println(
                "java & oop        -> "
                        + index.findByAllTags(List.of(t("java"), t("oop")))
        );
        System.out.println(
                "java              -> "
                        + index.findByAllTags(List.of(t("java")))
        );
        System.out.println(
                "java & streams & oop -> "
                        + index.findByAllTags(
                                List.of(t("java"), t("streams"), t("oop"))
                        )
        );
        System.out.println(
                "неизвестный тег   -> "
                        + index.findByAllTags(List.of(t("java"), t("go")))
        );

        System.out.println("Тегов в индексе: " + index.tagCount());

        index.removeCourse(c(2));
        System.out.println(
                "После удаления курса 2: тегов = " + index.tagCount()
                        + ", streams есть? " + index.hasTag(t("streams"))
        );
        System.out.println(
                "Повторное удаление: " + index.removeCourse(c(2))
        );

        index.addCourse(c(1), List.of(t("python")));
        System.out.println(
                "После замены тегов курса 1: beginner есть? "
                        + index.hasTag(t("beginner"))
                        + ", java & oop -> "
                        + index.findByAllTags(List.of(t("java"), t("oop")))
        );
    }
}

record Tag(String name) {
    Tag {
        Objects.requireNonNull(name, "name");
        name = name.trim().toLowerCase();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Пустой тег");
        }
    }
}

record CourseId(int value) implements Comparable<CourseId> {
    @Override
    public int compareTo(CourseId o) {
        return Integer.compare(value, o.value);
    }
}

final class TagIndex {

    private final Map<Tag, Set<CourseId>> byTag = new HashMap<>();
    private final Map<CourseId, Set<Tag>> tagsOfCourse = new HashMap<>();

    void addCourse(CourseId id, Collection<Tag> tags) {
        Objects.requireNonNull(id, "id");

        Set<Tag> newTags = Set.copyOf(tags);
        removeCourse(id);

        if (newTags.isEmpty()) {
            return;
        }

        tagsOfCourse.put(id, newTags);

        for (Tag tag : newTags) {
            byTag
                    .computeIfAbsent(tag, k -> new TreeSet<>())
                    .add(id);
        }
    }

    boolean removeCourse(CourseId id) {
        Set<Tag> tags = tagsOfCourse.remove(id);

        if (tags == null) {
            return false;
        }

        for (Tag tag : tags) {
            Set<CourseId> ids = byTag.get(tag);
            ids.remove(id);

            if (ids.isEmpty()) {
                byTag.remove(tag);
            }
        }

        return true;
    }

    Set<CourseId> findByAllTags(Collection<Tag> tags) {
        if (tags.isEmpty()) {
            return Set.of();
        }

        List<Set<CourseId>> sets = new ArrayList<>();

        for (Tag tag : new LinkedHashSet<>(tags)) {
            Set<CourseId> ids = byTag.get(tag);

            if (ids == null) {
                return Set.of();
            }

            sets.add(ids);
        }

        sets.sort(Comparator.comparingInt(Set::size));

        Set<CourseId> result = new TreeSet<>(sets.get(0));

        for (int i = 1; i < sets.size() && !result.isEmpty(); i++) {
            result.retainAll(sets.get(i));
        }

        return Collections.unmodifiableSet(result);
    }

    int tagCount() {
        return byTag.size();
    }

    boolean hasTag(Tag tag) {
        return byTag.containsKey(tag);
    }
}