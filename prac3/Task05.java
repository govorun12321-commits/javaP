import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/*
 * Задание 5. Интерфейс или абстрактный класс
 *
 * Для понятий Identifiable, CoursePolicy, BaseEntity, Formatter, ClockProvider выберите
 * interface, abstract class, обычный class или отсутствие отдельного типа. Для каждого
 * решения запишите мотивировку и цену изменения.
 *
 * Решения:
 *   Identifiable  - interface. Чистая способность "имеет id", без состояния.
 *               Цена: новый метод ломает всех реализующих (смягчается default).
 *   CoursePolicy  - interface (стратегия). Взаимозаменяемые правила без общего состояния.
 *               Цена: новая политика - новый класс; смена контракта затрагивает все политики.
 *   BaseEntity    - abstract class. Общее состояние (id), equals/hashCode; сам по себе не создаётся.
 *               Цена: занимает единственный слот наследования, правки бьют по всем наследникам.
 *   Formatter     - interface (функциональный). Один метод, можно лямбдой.
 *               Цена: минимальная.
 *   ClockProvider - отдельный тип не нужен: есть java.time.Clock (в тестах - Clock.fixed).
 *               Цена: зависимость от типа JDK (приемлема).
 */
public class Task05 {

    interface Identifiable {
        long id();
    }

    interface CoursePolicy {
        boolean allows(Course course);
    }

    static abstract class BaseEntity implements Identifiable {
        private final long id;
        protected BaseEntity(long id) { this.id = id; }
        @Override public long id() { return id; }
        @Override public final boolean equals(Object o) {
            return o != null && getClass() == o.getClass() && ((BaseEntity) o).id == id;
        }
        @Override public final int hashCode() { return Long.hashCode(id); }
    }

    static class Student extends BaseEntity {
        final String name;
        Student(long id, String name) { super(id); this.name = name; }
    }

    @FunctionalInterface
    interface Formatter<T> {
        String format(T value);
    }

    public static void main(String[] args) {
        Course course = new Course(1, "Java", 40);
        Student s = new Student(7, "Аня");

        CoursePolicy notFinished = c -> !c.isCompleted();
        Formatter<Course> shortFmt = c -> c.title() + " (" + c.durationHours() + " ч)";
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC); 

        System.out.println("Student id=" + s.id() + ", equals same id: " + s.equals(new Student(7, "Другой")));
        System.out.println("Policy allows: " + notFinished.allows(course));
        System.out.println(shortFmt.format(course));
        System.out.println("Now (fixed): " + clock.instant());
    }
}
