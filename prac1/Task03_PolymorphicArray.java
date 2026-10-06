package prac1;

interface CourseFormatter3 {
    String format(Course course);
}

class CompactFormatter3 implements CourseFormatter3 {
    @Override
    public String format(Course course) {
        return course.id() + " | " + course.title();
    }
}

class DetailedFormatter3 implements CourseFormatter3 {
    @Override
    public String format(Course course) {
        return course.title() + " [" + course.completedHours() + "/" +
                course.durationHours() + "h]";
    }
}

class JsonFormatter3 implements CourseFormatter3 {
    @Override
    public String format(Course course) {
        return "{\"id\":" + course.id() + ",\"title\":\"" +
                course.title() + "\",\"remaining\":" + course.remainingHours() + "}";
    }
}

public class Task03_PolymorphicArray {
    public static void main(String[] args) {
        Course course = new Course(2, "Java Collections", 24);

        CourseFormatter3[] formatters = {
                new CompactFormatter3(),
                new DetailedFormatter3(),
                new JsonFormatter3()
        };

        for (CourseFormatter3 formatter : formatters) {
            System.out.println(formatter.format(course));
        }
    }
}
