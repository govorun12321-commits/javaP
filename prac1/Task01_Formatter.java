package prac1;

interface CourseFormatter {
    String format(Course course);
}

class CompactCourseFormatter implements CourseFormatter {
    @Override
    public String format(Course course) {
        return course.id() + ": " + course.title();
    }
}

class DetailedCourseFormatter implements CourseFormatter {
    @Override
    public String format(Course course) {
        return "Course #" + course.id() +
                ", title=" + course.title() +
                ", duration=" + course.durationHours() + "h" +
                ", completed=" + course.completedHours() + "h" +
                ", remaining=" + course.remainingHours() + "h";
    }
}

public class Task01_Formatter {
    public static void main(String[] args) {
        Course course = new Course(1, "Java OOP", 40);

        CourseFormatter formatter = new CompactCourseFormatter();
        System.out.println(formatter.format(course));

        formatter = new DetailedCourseFormatter();
        System.out.println(formatter.format(course));

    }
}
