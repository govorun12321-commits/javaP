package coursehub;

import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class Task14ImporterTest {
    static final class CloseFailReader extends Reader {
        private final String data;
        CloseFailReader(String data) { this.data = data; }
        private boolean read = false;
        @Override public int read(char[] c, int off, int len) {
            if (read) return -1;
            read = true;
            c[off] = data.charAt(0);
            return 1;
        }
        @Override public void close() { throw new RuntimeException("close failed"); }
    }

    @Test void malformedRecordHasRecordNumber() {
        var e = assertThrows(CourseImportException.class,
            () -> Task14Importer.importCourses(new java.io.StringReader("bad")));
        assertEquals(1, e.recordNumber());
        assertTrue(e.getCause() instanceof IllegalArgumentException);
    }

    @Test void successfulImportCountsProcessedRecords() throws Exception {
        var result = Task14Importer.importCourses(new java.io.StringReader("Java,10\nSQL,20"));
        assertEquals(2, result.processedRecords());
        assertEquals(2, result.courses().size());
    }

    @Test void closeFailureIsSuppressedWhenMainFailureExists() {
        Reader r = new Reader() {
            boolean first = true;
            public int read(char[] c, int off, int len) {
                if (first) { first=false; c[off]='x'; return 1; }
                return -1;
            }
            public void close() { throw new RuntimeException("close failed"); }
        };
        var e = assertThrows(CourseImportException.class, () -> Task14Importer.importCourses(r));
        assertEquals(1, e.recordNumber());
        assertTrue(e.getSuppressed().length >= 1);
    }
}
