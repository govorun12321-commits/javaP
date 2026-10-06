package coursehub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.*;

class Task15CourseHubTest {
    @Test void validImportAndEnrollmentSucceeds() {
        assertNull(Task15CourseHub.importAndEnroll(new StringReader("Java,10"), 5));
    }

    @Test void malformedRecordHasImportCode() {
        var e = Task15CourseHub.importAndEnroll(new StringReader("Java,abc"), 5);
        assertEquals(Task15CourseHub.ErrorCode.IMPORT_FAILED, e.code());
    }

    @Test void emptyImportHasNotFoundCode() {
        var e = Task15CourseHub.importAndEnroll(new StringReader(""), 5);
        assertEquals(Task15CourseHub.ErrorCode.NOT_FOUND, e.code());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -99})
    void invalidIdsAreNotValidSearches(long id) {
        assertTrue(Task15CourseHub.findCourse(id).isEmpty());
    }

    @Test void courseValidationRejectsBadDuration() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "x", 0));
    }

    @Test void courseValidationRejectsTooLongDuration() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "x", 101));
    }

    @Test void completeHoursCannotBeNegative() {
        Course c = new Course(1, "x", 10);
        assertThrows(IllegalArgumentException.class, () -> c.completeHours(-1));
        assertEquals(0, c.completedHours());
    }

    @Test void completeHoursCannotOverflow() {
        Course c = new Course(1, "x", 10);
        c.completeHours(6);
        assertThrows(IllegalArgumentException.class, () -> c.completeHours(5));
        assertEquals(6, c.completedHours());
    }

    @Test void customExceptionKeepsCause() {
        Throwable cause = new IllegalStateException("low level");
        var e = new EnrollmentRejectedException(1, 2, "NO_SEATS", cause);
        assertSame(cause, e.getCause());
        assertEquals("NO_SEATS", e.reasonCode());
    }

    @Test void customExceptionDoesNotExposeIdsInMessage() {
        var e = new EnrollmentRejectedException(12345, 67890, "NO_SEATS");
        assertFalse(e.getMessage().contains("12345"));
        assertFalse(e.toString().contains("67890"));
    }

    @Test void enrollmentRejectsDuplicate() {
        EnrollmentService s = new EnrollmentService();
        Course c = new Course(1, "x", 10);
        assertDoesNotThrow(() -> s.enroll(1, c, true, 1));
        var e = assertThrows(EnrollmentRejectedException.class, () -> s.enroll(1, c, true, 1));
        assertEquals(EnrollmentRejectedException.ALREADY_ENROLLED, e.reasonCode());
    }

    @Test void enrollmentRejectsNoSeats() {
        var e = assertThrows(EnrollmentRejectedException.class,
            () -> new EnrollmentService().enroll(1, new Course(1, "x", 10), true, 0));
        assertEquals(EnrollmentRejectedException.NO_SEATS, e.reasonCode());
    }

    @Test void enrollmentRejectsMissingPrerequisite() {
        var e = assertThrows(EnrollmentRejectedException.class,
            () -> new EnrollmentService().enroll(1, new Course(1, "x", 10), false, 1));
        assertEquals(EnrollmentRejectedException.PREREQUISITE, e.reasonCode());
    }

    @Test void priceRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> Task07ErrorAsValue.calculatePrice(null));
    }

    @Test void decisionTableAcceptsHappyPath() {
        assertEquals(Task13DecisionTable.Result.ACCEPT,
            Task13DecisionTable.decide(Task13DecisionTable.Status.OPEN, true, true, false));
    }

    @Test void decisionTableClosedHasPriority() {
        assertEquals(Task13DecisionTable.Result.REJECT_CLOSED,
            Task13DecisionTable.decide(Task13DecisionTable.Status.CLOSED, false, false, true));
    }

    @Test void importDoesNotPublishPartialResultOnMalformedRecord() {
        var e = Task15CourseHub.importAndEnroll(new StringReader("Good,10\nBad,abc"), 5);
        assertEquals(Task15CourseHub.ErrorCode.IMPORT_FAILED, e.code());
    }

    @Test void findPositiveIdReturnsCourse() {
        assertTrue(Task15CourseHub.findCourse(7).isPresent());
    }

    @Test void emptyTitleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Course(1, " ", 10));
    }

    @Test void zeroIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Course(0, "x", 10));
    }

    @Test void exactDurationCompletesCourse() {
        Course c = new Course(1, "x", 10);
        c.completeHours(10);
        assertTrue(c.isCompleted());
    }

    @Test void importKeepsStablePublicCode() {
        var e = Task15CourseHub.importAndEnroll(new StringReader("Bad record"), 5);
        assertEquals(Task15CourseHub.ErrorCode.IMPORT_FAILED, e.code());
    }
}
