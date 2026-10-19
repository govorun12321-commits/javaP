package coursehub.practice6.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateParserTest {

    @Test
    void validDatesAreParsed() {
        assertEquals(LocalDate.of(2026, 10, 17), DateParser.parse("startDate", "17.10.2026"));
        assertEquals(LocalDate.of(2024, 2, 29), DateParser.parse("startDate", "29.02.2024"));
        assertEquals(LocalDate.of(2000, 2, 29), DateParser.parse("startDate", "29.02.2000"));
    }

    @Test
    void impossibleDatesAreRejected() {
        List<String> impossible = List.of("29.02.2025", "29.02.2100", "31.04.2026", "32.01.2026", "00.01.2026",
                "15.13.2026", "15.00.2026");
        for (String value : impossible) {
            assertThrows(InvalidDateFieldException.class, () -> DateParser.parse("startDate", value), value);
        }
    }

    @Test
    void extraTextAndWrongShapeAreRejected() {
        List<String> wrong = List.of("17.10.2026 ", " 17.10.2026", "17.10.2026abc", "17.10.2026 12:00",
                "1.1.2026", "17.10.26", "2026-10-17", "17/10/2026", "", " ", "17.10.12345",
                "\u0661\u0667.10.2026");
        for (String value : wrong) {
            assertThrows(InvalidDateFieldException.class, () -> DateParser.parse("startDate", value), value);
        }
    }

    @Test
    void messageContainsFieldAndOriginalValue() {
        InvalidDateFieldException exception =
                assertThrows(InvalidDateFieldException.class, () -> DateParser.parse("startDate", "31.04.2026"));
        assertEquals("startDate", exception.fieldName());
        assertEquals("31.04.2026", exception.rawValue());
        assertTrue(exception.getMessage().contains("'startDate'"));
        assertTrue(exception.getMessage().contains("\"31.04.2026\""));
    }

    @Test
    void parserCauseIsPreserved() {
        InvalidDateFieldException exception =
                assertThrows(InvalidDateFieldException.class, () -> DateParser.parse("startDate", "31.04.2026"));
        assertTrue(exception.getCause() instanceof DateTimeParseException);
    }

    @Test
    void nullValueHasNoCauseBecauseNothingFailedInsideParser() {
        InvalidDateFieldException exception =
                assertThrows(InvalidDateFieldException.class, () -> DateParser.parse("endDate", null));
        assertNull(exception.getCause());
        assertNull(exception.rawValue());
        assertTrue(exception.getMessage().contains("'endDate'"));
    }

    @Test
    void formattingRoundTrips() {
        LocalDate date = LocalDate.of(2026, 3, 5);
        assertEquals("05.03.2026", DateParser.format(date));
        assertEquals(date, DateParser.parse("date", DateParser.format(date)));
    }
}
