package prac1;

interface Identifiable {
    long id();
}

interface CoursePolicy {
    boolean allows(Course course);
}

abstract class BaseEntity implements Identifiable {
    private final long id;

    protected BaseEntity(long id) {
        this.id = id;
    }

    @Override
    public long id() {
        return id;
    }
}

interface Formatter<T> {
    String format(T value);
}

interface ClockProvider {
    java.time.Instant now();
}

class SimpleClockProvider implements ClockProvider {
    @Override
    public java.time.Instant now() {
        return java.time.Instant.now();
    }
}

public class Task05_Types {
    public static void main(String[] args) {
        System.out.println("Identifiable -> interface");
        System.out.println("CoursePolicy -> interface");
        System.out.println("BaseEntity -> abstract class");
        System.out.println("Formatter -> interface");
        System.out.println("ClockProvider -> interface");
    }
}
