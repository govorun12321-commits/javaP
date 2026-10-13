import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * Задание 15. Расширяемый движок правил
 *
 * Спроектируйте CourseHub enrollment engine. Правила возвращают типизированный результат,
 * могут быть открытыми стратегиями или закрытой sealed-иерархией, композиция правил поддерживает
 * allOf и anyOf, а сервис не проверяет конкретные классы. Требуется диаграмма зависимостей,
 * минимум пять реализаций, демонстрация dynamic dispatch и записка.
 *
 * ДИАГРАММА ЗАВИСИМОСТЕЙ (A --> B: A зависит от B)
 *  EnrollmentEngine --> Rule --> RuleResult (sealed: Pass | Fail | Wait)
 *  EnrollmentEngine --> EnrollmentResult (sealed: Accepted | Rejected | WaitListed)
 *  AllOf, AnyOf --> Rule (хранят List<Rule>, сами являются Rule)
 *  NamedRule <|-- MinAgeRule, PrerequisitesRule, CapacityRule, PaymentRule, CourseOpenRule
 *  Rule --> EnrollmentContext (record)
 *
 *   ЗАПИСКА
 *    Наследование: тонкая база NamedRule (имя + final evaluate) и sealed-результаты.
 *    Композиция: AllOf/AnyOf содержат правила и сами являются Rule (Composite); движок
 *    получает корневое правило через конструктор.
 *    Sealed: конечный набор результатов даёт exhaustive switch без default, а новый вариант
 *    вызывает ошибку компиляции везде, где его не обработали. Результаты закрыты, правила открыты.
 */
public class Task15 {

    record Student(String name, int age, Set<Long> completedCourseIds, boolean paid) { }
    record EnrollmentContext(Student student, Course course, int enrolled, int capacity, Set<Long> prerequisites) { }

    sealed interface RuleResult permits Pass, Fail, Wait { }
    record Pass() implements RuleResult { }
    record Fail(String reason) implements RuleResult { }
    record Wait(String reason) implements RuleResult { }

    sealed interface EnrollmentResult permits Accepted, Rejected, WaitListed { }
    record Accepted(String student) implements EnrollmentResult { }
    record Rejected(String student, String reason) implements EnrollmentResult { }
    record WaitListed(String student, String reason) implements EnrollmentResult { }

    interface Rule {
        RuleResult evaluate(EnrollmentContext ctx);
        String name();
    }

    static abstract class NamedRule implements Rule {
        private final String name;
        protected NamedRule(String name) { this.name = name; }
        @Override public final String name() { return name; }
        @Override public final RuleResult evaluate(EnrollmentContext ctx) { return check(ctx); }
        protected abstract RuleResult check(EnrollmentContext ctx);
    }

    static class MinAgeRule extends NamedRule {
        private final int minAge;
        MinAgeRule(int minAge) { super("MinAge(" + minAge + ")"); this.minAge = minAge; }
        @Override protected RuleResult check(EnrollmentContext c) {
            return c.student().age() >= minAge ? new Pass() : new Fail("возраст меньше " + minAge);
        }
    }

    static class PrerequisitesRule extends NamedRule {
        PrerequisitesRule() { super("Prerequisites"); }
        @Override protected RuleResult check(EnrollmentContext c) {
            return c.student().completedCourseIds().containsAll(c.prerequisites())
                    ? new Pass() : new Fail("не пройдены prerequisites " + c.prerequisites());
        }
    }

    static class CapacityRule extends NamedRule {
        CapacityRule() { super("Capacity"); }
        @Override protected RuleResult check(EnrollmentContext c) {
            return c.enrolled() < c.capacity() ? new Pass() : new Wait("мест нет, лист ожидания");
        }
    }

    static class PaymentRule extends NamedRule {
        PaymentRule() { super("Payment"); }
        @Override protected RuleResult check(EnrollmentContext c) {
            return c.student().paid() ? new Pass() : new Fail("курс не оплачен");
        }
    }

    static class CourseOpenRule extends NamedRule {
        CourseOpenRule() { super("CourseOpen"); }
        @Override protected RuleResult check(EnrollmentContext c) {
            return c.course().isCompleted() ? new Fail("курс уже завершён") : new Pass();
        }
    }

    static class AllOf implements Rule {
        private final List<Rule> rules;
        AllOf(Rule... rules) { this.rules = List.of(rules); }
        @Override public String name() { return "allOf"; }
        @Override public RuleResult evaluate(EnrollmentContext ctx) {
            Wait firstWait = null;
            for (Rule r : rules) {
                RuleResult res = r.evaluate(ctx);                 
                if (res instanceof Fail) return res;               
                if (res instanceof Wait w && firstWait == null) firstWait = w;
            }
            return firstWait != null ? firstWait : new Pass();
        }
    }

    static class AnyOf implements Rule {
        private final List<Rule> rules;
        AnyOf(Rule... rules) { this.rules = List.of(rules); }
        @Override public String name() { return "anyOf"; }
        @Override public RuleResult evaluate(EnrollmentContext ctx) {
            Wait firstWait = null;
            List<String> reasons = new ArrayList<>();
            for (Rule r : rules) {
                RuleResult res = r.evaluate(ctx);
                if (res instanceof Pass) return res;
                if (res instanceof Wait w && firstWait == null) firstWait = w;
                if (res instanceof Fail f) reasons.add(f.reason());
            }
            return firstWait != null ? firstWait : new Fail(String.join("; ", reasons));
        }
    }

    static class EnrollmentEngine {
        private final Rule rule;
        EnrollmentEngine(Rule rule) { this.rule = rule; }
        EnrollmentResult enroll(EnrollmentContext ctx) {
            return switch (rule.evaluate(ctx)) {                   
                case Pass p -> new Accepted(ctx.student().name());
                case Fail f -> new Rejected(ctx.student().name(), f.reason());
                case Wait w -> new WaitListed(ctx.student().name(), w.reason());
            };
        }
    }

    static String show(EnrollmentResult r) {
        return switch (r) {
            case Accepted a -> "ПРИНЯТ " + a.student();
            case Rejected x -> "ОТКАЗ " + x.student() + ": " + x.reason();
            case WaitListed w -> "ОЖИДАНИЕ " + w.student() + ": " + w.reason();
        };
    }

    public static void main(String[] args) {
        Course course = new Course(2, "Advanced Java", 60);
        Student adult = new Student("Аня", 20, Set.of(1L), true);
        Student minor = new Student("Боря", 16, Set.of(1L), true);
        Student unpaid = new Student("Вера", 22, Set.of(1L), false);

        // Dynamic dispatch: массив ссылок типа Rule, вызывается метод фактического класса
        Rule[] rules = { new MinAgeRule(18), new PrerequisitesRule(), new CapacityRule(), new PaymentRule(), new CourseOpenRule() };
        EnrollmentContext ctx = new EnrollmentContext(adult, course, 30, 30, Set.of(1L));
        for (Rule r : rules) {
            System.out.println(r.getClass().getSimpleName() + " -> " + r.evaluate(ctx));
        }

        // Композиция: все базовые условия И (оплачено ИЛИ возраст >= 18)
        Rule root = new AllOf(new CourseOpenRule(), new PrerequisitesRule(),
                new AnyOf(new PaymentRule(), new MinAgeRule(18)), new CapacityRule());
        EnrollmentEngine engine = new EnrollmentEngine(root);

        System.out.println(show(engine.enroll(new EnrollmentContext(adult, course, 10, 30, Set.of(1L)))));  
        System.out.println(show(engine.enroll(new EnrollmentContext(adult, course, 30, 30, Set.of(1L)))));  
        System.out.println(show(engine.enroll(new EnrollmentContext(minor, course, 10, 30, Set.of(1L)))));  
        System.out.println(show(engine.enroll(new EnrollmentContext(new Student("Глеб", 15, Set.of(1L), false), course, 10, 30, Set.of(1L))))); 
        System.out.println(show(engine.enroll(new EnrollmentContext(unpaid, course, 10, 30, Set.of(1L, 9L))))); 
    }
}
