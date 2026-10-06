package prac1;

interface ReadCourseRepository {
    Course findById(long id);
}

interface WriteCourseRepository {
    void save(Course course);
}

interface CourseRepository extends ReadCourseRepository, WriteCourseRepository {
}

class InMemoryCourseRepository implements CourseRepository {
    private final java.util.Map<Long, Course> data = new java.util.HashMap<>();

    @Override
    public Course findById(long id) {
        return data.get(id);
    }

    @Override
    public void save(Course course) {
        data.put(course.id(), course);
    }
}

class ReadOnlyCourseRepository implements ReadCourseRepository {
    private final java.util.Map<Long, Course> data;

    ReadOnlyCourseRepository(java.util.Map<Long, Course> data) {
        this.data = data;
    }

    @Override
    public Course findById(long id) {
        return data.get(id);
    }
}

public class Task08_LSP {
    static void printCourse(ReadCourseRepository repository, long id) {
        System.out.println(repository.findById(id));
    }

    public static void main(String[] args) {
        java.util.Map<Long, Course> data = new java.util.HashMap<>();
        data.put(1L, new Course(1, "OOP", 30));

        ReadCourseRepository readOnly = new ReadOnlyCourseRepository(data);
        printCourse(readOnly, 1);

        CourseRepository writable = new InMemoryCourseRepository();
        writable.save(new Course(2, "Java", 20));
        printCourse(writable, 2);
    }
}
