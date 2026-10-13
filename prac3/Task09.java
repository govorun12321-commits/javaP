import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * Задание 9. Политика допуска как стратегия
 *
 * Объявите EnrollmentPolicy и реализации по возрасту, prerequisites и вместимости. Клиент
 * принимает политику конструктором. Добавление новой политики не меняет EnrollmentService.
 *
 * Пояснение:
 * EnrollmentService получает список EnrollmentPolicy в конструкторе (внедрение зависимости)
 * и работает только с интерфейсом. Новое правило (в main добавлено лямбдой) - новый объект
 * в списке; сам сервис не правится (Open/Closed).
 */
public class Task09 {

    record Student(String name, int age, Set<Long> completedCourseIds) { }
    record EnrollmentRequest(Student student, Course course, int enrolledCount) { }

    interface EnrollmentPolicy { 
        String check(EnrollmentRequest r);
    }

    static class AgePolicy implements EnrollmentPolicy {
        private final int minAge;
        AgePolicy(int minAge) { this.minAge = minAge; }
        @Override public String check(EnrollmentRequest r) {
            return r.student().age() >= minAge ? null : "Минимальный возраст: " + minAge;
        }
    }

    static class PrerequisitePolicy implements EnrollmentPolicy {
        private final Set<Long> required;
        PrerequisitePolicy(Set<Long> required) { this.required = required; }
        @Override public String check(EnrollmentRequest r) {
            return r.student().completedCourseIds().containsAll(required) ? null : "Не пройдены prerequisites: " + required;
        }
    }

    static class CapacityPolicy implements EnrollmentPolicy {
        private final int capacity;
        CapacityPolicy(int capacity) { this.capacity = capacity; }
        @Override public String check(EnrollmentRequest r) {
            return r.enrolledCount() < capacity ? null : "Нет мест (" + capacity + ")";
        }
    }

    static class EnrollmentService {
        private final List<EnrollmentPolicy> policies;
        EnrollmentService(List<EnrollmentPolicy> policies) { this.policies = List.copyOf(policies); }

        List<String> enroll(EnrollmentRequest request) {          
            List<String> reasons = new ArrayList<>();
            for (EnrollmentPolicy p : policies) {
                String reason = p.check(request);
                if (reason != null) reasons.add(reason);
            }
            return reasons;
        }
    }

    public static void main(String[] args) {
        Course course = new Course(2, "Advanced Java", 60);
        Student s = new Student("Аня", 17, Set.of(1L));
        EnrollmentRequest req = new EnrollmentRequest(s, course, 30);

        List<EnrollmentPolicy> policies = new ArrayList<>(List.of(
                new AgePolicy(18), new PrerequisitePolicy(Set.of(1L, 5L)), new CapacityPolicy(30)));
        System.out.println(new EnrollmentService(policies).enroll(req));

        policies.add(r -> r.course().isCompleted() ? "Курс уже завершён" : null);
        System.out.println(new EnrollmentService(policies).enroll(req));
    }
}
