package prac1;

import java.util.List;

record EnrollmentRequest(Student student, Course course) {
}

sealed interface EnrollmentDecision
        permits AcceptedDecision, RejectedDecision, WaitListedDecision {
}

record AcceptedDecision(String message) implements EnrollmentDecision {
}

record RejectedDecision(String reason) implements EnrollmentDecision {
}

record WaitListedDecision(int position) implements EnrollmentDecision {
}

interface EnrollmentRule {
    EnrollmentDecision evaluate(EnrollmentRequest request);
}

class AgeEnrollmentRule implements EnrollmentRule {
    private final int minAge;

    AgeEnrollmentRule(int minAge) {
        this.minAge = minAge;
    }

    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        if (request.student().age() >= minAge) {
            return new AcceptedDecision("Age accepted");
        }
        return new RejectedDecision("Minimum age is " + minAge);
    }
}

class PrerequisiteRule implements EnrollmentRule {
    private final java.util.Set<String> prerequisites;

    PrerequisiteRule(java.util.Set<String> prerequisites) {
        this.prerequisites = java.util.Set.copyOf(prerequisites);
    }

    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        if (request.student().completedCourses().containsAll(prerequisites)) {
            return new AcceptedDecision("Prerequisites accepted");
        }
        return new RejectedDecision("Missing prerequisites");
    }
}

class CapacityRule implements EnrollmentRule {
    private final int capacity;
    private final java.util.function.IntSupplier currentCount;

    CapacityRule(int capacity, java.util.function.IntSupplier currentCount) {
        this.capacity = capacity;
        this.currentCount = currentCount;
    }

    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        if (currentCount.getAsInt() < capacity) {
            return new AcceptedDecision("Capacity available");
        }
        return new WaitListedDecision(currentCount.getAsInt() - capacity + 1);
    }
}

class CourseCompletedRule implements EnrollmentRule {
    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        if (request.course().isCompleted()) {
            return new RejectedDecision("Course already completed");
        }
        return new AcceptedDecision("Course is active");
    }
}

class NameRequiredRule implements EnrollmentRule {
    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        if (request.student().name() != null && !request.student().name().isBlank()) {
            return new AcceptedDecision("Student name present");
        }
        return new RejectedDecision("Student name is required");
    }
}

class AllOfRule implements EnrollmentRule {
    private final List<EnrollmentRule> rules;

    AllOfRule(List<EnrollmentRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        for (EnrollmentRule rule : rules) {
            EnrollmentDecision result = rule.evaluate(request);
            if (result instanceof RejectedDecision || result instanceof WaitListedDecision) {
                return result;
            }
        }
        return new AcceptedDecision("All rules passed");
    }
}

class AnyOfRule implements EnrollmentRule {
    private final List<EnrollmentRule> rules;

    AnyOfRule(List<EnrollmentRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public EnrollmentDecision evaluate(EnrollmentRequest request) {
        RejectedDecision lastRejected = new RejectedDecision("No rule accepted");
        for (EnrollmentRule rule : rules) {
            EnrollmentDecision result = rule.evaluate(request);
            if (result instanceof AcceptedDecision accepted) {
                return accepted;
            }
            if (result instanceof RejectedDecision rejected) {
                lastRejected = rejected;
            }
        }
        return lastRejected;
    }
}

class CourseHubEnrollmentService {
    private final EnrollmentRule rule;

    CourseHubEnrollmentService(EnrollmentRule rule) {
        this.rule = rule;
    }

    EnrollmentDecision enroll(EnrollmentRequest request) {
        return rule.evaluate(request);
    }
}

public class Task15_CourseHub {
    static String render(EnrollmentDecision decision) {
        return switch (decision) {
            case AcceptedDecision a -> "ACCEPTED: " + a.message();
            case RejectedDecision r -> "REJECTED: " + r.reason();
            case WaitListedDecision w -> "WAITLISTED: position " + w.position();
        };
    }

    public static void main(String[] args) {
        Course course = new Course(100, "Advanced Java", 40);
        Student student = new Student(
                1,
                "Alex",
                20,
                java.util.Set.of("Java Basics")
        );

        EnrollmentRule combined = new AllOfRule(List.of(
                new AgeEnrollmentRule(18),
                new PrerequisiteRule(java.util.Set.of("Java Basics")),
                new CapacityRule(10, () -> 3),
                new CourseCompletedRule(),
                new NameRequiredRule()
        ));

        CourseHubEnrollmentService service =
                new CourseHubEnrollmentService(combined);

        EnrollmentDecision decision =
                service.enroll(new EnrollmentRequest(student, course));

        System.out.println(render(decision));

        EnrollmentRule[] rules = {
                new AgeEnrollmentRule(18),
                new PrerequisiteRule(java.util.Set.of("Java Basics")),
                new CapacityRule(10, () -> 3),
                new CourseCompletedRule(),
                new NameRequiredRule()
        };

        for (EnrollmentRule rule : rules) {
            System.out.println(render(
                    rule.evaluate(new EnrollmentRequest(student, course))
            ));
        }
    }
}
