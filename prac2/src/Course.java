package coursehub;


public class Course {
    private final long id;
    private final String title;
    private final int durationHours;
    private int completedHours;

    public Course(long id, String title, int durationHours) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank");
        if (durationHours < 1 || durationHours > 100)
            throw new IllegalArgumentException("durationHours must be between 1 and 100");
        this.id = id;
        this.title = title.trim();
        this.durationHours = durationHours;
        this.completedHours = 0;
    }

    public Course(long id, String title, int durationHours, int completedHours) {
        this(id, title, durationHours);
        if (completedHours < 0 || completedHours > durationHours)
            throw new IllegalArgumentException("completedHours must be between 0 and durationHours");
        this.completedHours = completedHours;
    }

    public long id() { return id; }
    public String title() { return title; }
    public int durationHours() { return durationHours; }
    public int completedHours() { return completedHours; }
    public int remainingHours() { return durationHours - completedHours; }
    public boolean isCompleted() { return remainingHours() == 0; }

    public void completeHours(int hours) {
        if (hours <= 0) throw new IllegalArgumentException("hours must be positive");
        if (completedHours + hours > durationHours)
            throw new IllegalArgumentException("cannot complete more than remaining hours");
        completedHours += hours;
    }
}
