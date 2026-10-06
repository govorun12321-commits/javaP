package prac1;

public class Course {
    private long id;
    private String title;
    private int durationHours;
    private int completedHours;

    Course(long id, String title, int durationHours, int completedHours) {
        if (id > 0) this.id = id;
        else System.out.println("ERR");

        if (title != null && !title.isBlank()) this.title = title.trim();

        if (durationHours >= 1 && durationHours <= 100) this.durationHours = durationHours;

        if (completedHours < durationHours && completedHours >= 0) {
            this.completedHours = completedHours;
        }
    }

    public Course(long id, String title, int durationHours) {
        if (id > 0) this.id = id;
        else System.out.println("ERR");

        if (title != null && !title.isBlank()) this.title = title.trim();

        if (durationHours >= 1 && durationHours <= 300) this.durationHours = durationHours;

        this.completedHours = 0;
    }

    public long id() { return id; }
    public String title() { return title; }
    public int durationHours() { return durationHours; }
    public int completedHours() { return completedHours; }

    public int remainingHours() { return durationHours - completedHours; }
    public boolean isCompleted() { return remainingHours() == 0; }

    public void completeHours(int hours) {
        if (hours < 0 || remainingHours() < hours) {
            System.out.println("ERR");
        } else {
            completedHours += hours;
        }
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", title='" + title + "', durationHours=" +
                durationHours + ", completedHours=" + completedHours + "}";
    }

    public static void main(String[] args) {
        Course course = new Course(10, "Java Fundamentals", 32);
        course.completeHours(6);

        System.out.println(course.completedHours());
        System.out.println(course.remainingHours());
        System.out.println(course.isCompleted());
    }
}
