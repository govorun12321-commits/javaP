package coursehub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Task08CourseCompleteHoursTest {
    @Test
    void completeHours_addsHoursToCompletedHours() {
        Course course = new Course(10, "Java Fundamentals", 32);
        course.completeHours(6);
        assertEquals(6, course.completedHours());
    }
}
