package coursehub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Task11AssertThrowsTest {
    @Test
    void failedCommandKeepsStateAndHasUsefulMessage() {
        Course course = new Course(1, "Java", 10);
        course.completeHours(4);
        int before = course.completedHours();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> course.completeHours(7));

        assertEquals("cannot complete more than remaining hours", ex.getMessage());
        assertEquals(before, course.completedHours());
    }
}
