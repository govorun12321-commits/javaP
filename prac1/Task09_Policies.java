package prac1;

import java.util.Set;

interface EnrollmentPolicy {
    boolean allows(Course course, Student student);
    String reason();
}

record Student(long id, String name, int age, java.util.Set<String> completedCourses) {
}

class AgePolicy implements EnrollmentPolicy {
    private final int minimumAge;

    AgePolicy(int minimumAge) {
        this.minimumAge = minimumAge;
    }

    @Override
    public boolean allows(Course course, Student student) {
        return student.age() >= minimumAge;
    }

    @Override
    public String reason() {
        return "Minimum age: " + minimumAge;
    }
}

class PrerequisitesPolicy implements EnrollmentPolicy {
    private final java.util.Set<String> prerequisites;

    PrerequisitesPolicy(java.util.Set<String> prerequisites) {
        this.prerequisites = Set.copyOf(prerequisites);
    }

    @Override
    public boolean allows(Course course, Student student) {
        return student.completedCourses().containsAll(prerequisites);
    }

    @Override
    public String reason() {
        return "Prerequisites: " + prerequisites;
    }
}

class CapacityPolicy implements EnrollmentPolicy {
    private final int capacity;
    private final java.util.function.IntSupplier enrolledCount;

    CapacityPolicy(int capacity, java.util.function.IntSupplier enrolledCount) {
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
    }

    @Override
    public boolean allows(Course course, Student student) {
        return enrolledCount.getAsInt() < capacity;
    }

    @Override
    public String reason() {
        return "Capacity: " + capacity;
    }
}

class EnrollmentService9 {
    private final EnrollmentPolicy policy;

    EnrollmentService9(EnrollmentPolicy policy) {
        this.policy = policy;
    }

    boolean enroll(Course course, Student student) {
        if (!policy.allows(course, student)) {
            System.out.println("Rejected: " + policy.reason());
            return false;
        }
        System.out.println("Accepted");
        return true;
    }
}

public class Task09_Policies {
    public static void main(String[] args) {
        Course course = new Course(1, "Java OOP", 40);
        Student student = new Student(1, "Alex", 20, Set.of("Java Basics"));

        EnrollmentService9 age =
                new EnrollmentService9(new AgePolicy(18));
        age.enroll(course, student);

        EnrollmentService9 prerequisites =
                new EnrollmentService9(new PrerequisitesPolicy(Set.of("Java Basics")));
        prerequisites.enroll(course, student);

        EnrollmentService9 capacity =
                new EnrollmentService9(new CapacityPolicy(10, () -> 3));
        capacity.enroll(course, student);
    }
}
