package coursehub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static coursehub.Task13DecisionTable.*;

class Task13DecisionTableTest {
    @Test void closedCourseWins() { assertEquals(Result.REJECT_CLOSED, decide(Status.CLOSED, true, true, false)); }
    @Test void noSeatsRejects() { assertEquals(Result.REJECT_NO_SEATS, decide(Status.OPEN, false, true, false)); }
    @Test void missingPrerequisiteRejects() { assertEquals(Result.REJECT_PREREQUISITE, decide(Status.OPEN, true, false, false)); }
    @Test void repeatedRejects() { assertEquals(Result.REJECT_DUPLICATE, decide(Status.OPEN, true, true, true)); }
    @Test void allRulesPass() { assertEquals(Result.ACCEPT, decide(Status.OPEN, true, true, false)); }
}
