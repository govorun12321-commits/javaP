package coursehub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Task09CourseBoundariesTest {
    @Test
    void durationAroundMinAndMax() {
        assertFalse(Task09Boundaries.accepts(0));
        assertTrue(Task09Boundaries.accepts(1));
        assertTrue(Task09Boundaries.accepts(100));
        assertFalse(Task09Boundaries.accepts(101));
    }
}
