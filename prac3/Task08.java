import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Задание 8. Нарушение LSP
 *
 * Найдите проблему в ReadOnlyCourseRepository extends CourseRepository, который выбрасывает
 * исключение из save. Перестройте интерфейсы так, чтобы read-only реализация не обещала
 * невозможную операцию.
 *
 * Проблема:
 *   class ReadOnlyCourseRepository extends CourseRepository {
 *       @Override void save(Course c) { throw new UnsupportedOperationException(); }
 *   }
 * Клиент, принимающий CourseRepository, вправе вызвать save() - подтип ломает контракт
 * базового типа (нарушение принципа подстановки Лисков): код, корректный для базового типа,
 * падает на подтипе.
 *
 * Решение (Interface Segregation): разделить на CourseReader и CourseWriter.
 * ReadOnly реализует только CourseReader, поэтому у него просто нет метода save, и
 * ошибка ловится компилятором, а не в runtime.
 */
public class Task08 {

    interface CourseReader {
        Optional<Course> findById(long id);
        List<Course> findAll();
    }

    interface CourseWriter {
        void save(Course course);
    }

    interface CourseRepository extends CourseReader, CourseWriter { }

    static class InMemoryCourseRepository implements CourseRepository {
        private final List<Course> data = new ArrayList<>();
        @Override public Optional<Course> findById(long id) { return data.stream().filter(c -> c.id() == id).findFirst(); }
        @Override public List<Course> findAll() { return List.copyOf(data); }
        @Override public void save(Course course) { data.add(course); }
    }

    static class ReadOnlyCourseRepository implements CourseReader {
        private final CourseReader source;
        ReadOnlyCourseRepository(CourseReader source) { this.source = source; }
        @Override public Optional<Course> findById(long id) { return source.findById(id); }
        @Override public List<Course> findAll() { return source.findAll(); }
    }

    static void printCatalog(CourseReader reader) {          // клиенту достаточно чтения
        reader.findAll().forEach(c -> System.out.println(c.id() + " " + c.title()));
    }

    public static void main(String[] args) {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        repo.save(new Course(1, "Java", 40));
        repo.save(new Course(2, "SQL", 20));

        CourseReader readOnly = new ReadOnlyCourseRepository(repo);
        printCatalog(readOnly);
    }
}
