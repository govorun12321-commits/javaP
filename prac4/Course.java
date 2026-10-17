import java.util.Objects;

public class Course {
    public static final int MIN_DURATION_HOURS = 1;
    public static final int MAX_DURATION_HOURS = 300;

    private final long id;
    private final String title;
    private final int durationHours;
    private int completedHours;

    Course(long id, String title, int durationHours, int completedHours) {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(title, "title");
        if (title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (durationHours < MIN_DURATION_HOURS || durationHours > MAX_DURATION_HOURS) {
            throw new IllegalArgumentException("durationHours must be in ["
                    + MIN_DURATION_HOURS + ", " + MAX_DURATION_HOURS + "]");
        }
        if (completedHours < 0 || completedHours > durationHours) {
            throw new IllegalArgumentException("completedHours must be in [0, durationHours]");
        }
        this.id = id;
        this.title = title.trim();
        this.durationHours = durationHours;
        this.completedHours = completedHours;
    }

    public Course(long id, String title, int durationHours) {
        this(id, title, durationHours, 0);
    }

    public long id() {
        return id;
    }

    public String title() {
        return title;
    }

    public int durationHours() {
        return durationHours;
    }

    public int completedHours() {
        return completedHours;
    }

    public int remainingHours() {
        return durationHours - completedHours;
    }

    public boolean isCompleted() {
        return remainingHours() == 0;
    }

    public void completeHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("hours must be positive");
        }
        if (hours > remainingHours()) {
            throw new IllegalStateException("Cannot complete " + hours
                    + " hours: only " + remainingHours() + " remaining");
        }
        this.completedHours += hours;
    }

    public static void main(String[] args) {
        Course course = new Course(10, "Java Fundamentals", 32);
        course.completeHours(6);

        System.out.println(course.completedHours());
        System.out.println(course.remainingHours());
        System.out.println(course.isCompleted());
    }
}
