package coursehub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;

class Task12StableTestsTest {
    @Test
    void eachTestOwnsItsState() throws Exception {
        Course c = new Course(1, "Java", 10);
        assertEquals(0, c.completedHours());
        var dir = Files.createTempDirectory("coursehub-test-");
        assertTrue(Files.exists(dir));
    }

    @Test
    void doNotDependOnToStringFormatting() {
        EnrollmentRejectedException e = new EnrollmentRejectedException(1, 2, "NO_SEATS");
        assertEquals("NO_SEATS", e.reasonCode());
    }
}
