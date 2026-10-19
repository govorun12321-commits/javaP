package coursehub.practice6.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportBuilderTest {

    @Test
    void bothApproachesProduceTheSameReport() {
        assertEquals(ReportBuilder.buildWithPlus(10_000), ReportBuilder.buildWithBuilder(10_000));
    }

    @Test
    void reportHasOneLinePerNumber() {
        String report = ReportBuilder.buildWithBuilder(10_000);
        assertEquals(10_000, report.lines().count());
        assertTrue(report.startsWith("Line 1: "));
    }

    @Test
    void emptyReportIsEmptyString() {
        assertEquals("", ReportBuilder.buildWithPlus(0));
        assertEquals("", ReportBuilder.buildWithBuilder(0));
    }

    @Test
    void negativeLineCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReportBuilder.buildWithPlus(-1));
        assertThrows(IllegalArgumentException.class, () -> ReportBuilder.buildWithBuilder(-1));
    }

    @Test
    void measurementConfirmsSameResult() {
        ReportBuilder.Measurement measurement = ReportBuilder.measure(2_000);
        assertTrue(measurement.sameResult());
        assertEquals(2_000, measurement.lines());
        assertTrue(measurement.plusNanos() > 0);
        assertTrue(measurement.builderNanos() > 0);
    }

    @Test
    void stringMethodsDoNotChangeOriginal() {
        ReportBuilder.Immutability immutability = ReportBuilder.immutability();
        assertEquals("course", immutability.original());
        assertEquals("COURSE-HUB", immutability.changed());
        assertNotEquals(immutability.original(), immutability.changed());
    }
}
