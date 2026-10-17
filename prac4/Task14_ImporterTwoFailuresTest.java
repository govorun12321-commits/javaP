/*
 * Задание 14. Ресурс с двумя отказами
 *
 * Реализуйте импортёр из абстрактного `Reader`, который может получить malformed record и
 * ошибку закрытия. Тестами зафиксируйте основное и suppressed исключение, число обработанных
 * записей и отсутствие частично опубликованного результата.
 *
 * Краткое объяснение:
 *  - Импортёр читает Reader через try-with-resources (BufferedReader закрывает исходный Reader).
 *  - Записи накапливаются в локальный staged-список и публикуются в sink ТОЛЬКО после успешного
 *    чтения и закрытия: при любой ошибке sink остаётся пустым (атомарность).
 *  - Malformed record -> CourseImportException(номер записи, cause=NumberFormatException).
 *    Если при этом упал close(), его IOException попадает в getSuppressed() основного исключения.
 *  - Если тело отработало, а упал только close() — IOException оборачивается в CourseImportException
 *    с сохранением cause.
 *  - processedRecords — число успешно разобранных записей до отказа.
 *  Формат записи: "id;title;durationHours".
 */

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class Task14_ImporterTwoFailuresTest {

    static final class CourseImportException extends Exception {
        private static final long serialVersionUID = 1L;
        private final int recordNumber;

        CourseImportException(String message, int recordNumber, Throwable cause) {
            super(message, cause);
            this.recordNumber = recordNumber;
        }

        int recordNumber() { return recordNumber; }
    }

    static final class CourseImporter {
        private int processedRecords;

        int processedRecords() { return processedRecords; }

        int importInto(Reader source, List<Course> sink) throws CourseImportException {
            processedRecords = 0;
            List<Course> staged = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(source)) {
                String line;
                int recordNumber = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    recordNumber++;
                    staged.add(parse(line, recordNumber));
                    processedRecords++;
                }
            } catch (IOException e) {
                throw new CourseImportException("I/O failure during import", 0, e);
            }
            sink.addAll(staged);                    
            return staged.size();
        }

        private Course parse(String line, int recordNumber) throws CourseImportException {
            String[] fields = line.split(";", -1);
            if (fields.length != 3) {
                throw new CourseImportException("Malformed record #" + recordNumber, recordNumber, null);
            }
            try {
                return new Course(Long.parseLong(fields[0].trim()), fields[1], Integer.parseInt(fields[2].trim()));
            } catch (IllegalArgumentException e) {
                throw new CourseImportException("Malformed record #" + recordNumber, recordNumber, e);
            }
        }
    }

    static final class ScriptedReader extends Reader {
        private final StringReader delegate;
        private final boolean failOnClose;
        private int closeCount;

        ScriptedReader(String content, boolean failOnClose) {
            this.delegate = new StringReader(content);
            this.failOnClose = failOnClose;
        }

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            return delegate.read(buffer, offset, length);
        }

        @Override
        public void close() throws IOException {
            closeCount++;
            delegate.close();
            if (failOnClose) {
                throw new IOException("close failed");
            }
        }

        int closeCount() { return closeCount; }
    }

    private static String feed(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    void importInto_validFeed_publishesAllRecordsAndClosesReader() throws CourseImportException {
        CourseImporter importer = new CourseImporter();
        ScriptedReader reader = new ScriptedReader(feed("1;Java;40", "2;Kotlin;30", "3;Go;20"), false);
        List<Course> sink = new ArrayList<>();

        int imported = importer.importInto(reader, sink);

        assertEquals(3, imported);
        assertEquals(3, sink.size());
        assertEquals(3, importer.processedRecords());
        assertEquals(1, reader.closeCount());
    }

    @Test
    void importInto_malformedRecord_throwsWithCauseAndPublishesNothing() {
        CourseImporter importer = new CourseImporter();
        ScriptedReader reader = new ScriptedReader(feed("1;Java;40", "2;Kotlin;30", "3;Go;abc"), false);
        List<Course> sink = new ArrayList<>();

        CourseImportException ex = assertThrows(CourseImportException.class, () -> importer.importInto(reader, sink));

        assertEquals(3, ex.recordNumber());
        assertInstanceOf(NumberFormatException.class, ex.getCause());
        assertEquals(0, ex.getSuppressed().length);
        assertEquals(2, importer.processedRecords());
        assertTrue(sink.isEmpty());
        assertEquals(1, reader.closeCount());
    }

    @Test
    void importInto_malformedRecordAndCloseFailure_keepsCloseErrorAsSuppressed() {
        CourseImporter importer = new CourseImporter();
        ScriptedReader reader = new ScriptedReader(feed("1;Java;40", "2;Kotlin;30", "3;Go;abc"), true);
        List<Course> sink = new ArrayList<>();

        CourseImportException ex = assertThrows(CourseImportException.class, () -> importer.importInto(reader, sink));

        assertEquals(3, ex.recordNumber());                                 
        assertInstanceOf(NumberFormatException.class, ex.getCause());
        assertEquals(1, ex.getSuppressed().length);                        
        assertInstanceOf(IOException.class, ex.getSuppressed()[0]);
        assertEquals("close failed", ex.getSuppressed()[0].getMessage());
        assertEquals(2, importer.processedRecords());
        assertTrue(sink.isEmpty());
        assertEquals(1, reader.closeCount());
    }

    @Test
    void importInto_validFeedButCloseFails_throwsAndPublishesNothing() {
        CourseImporter importer = new CourseImporter();
        ScriptedReader reader = new ScriptedReader(feed("1;Java;40", "2;Kotlin;30"), true);
        List<Course> sink = new ArrayList<>();

        CourseImportException ex = assertThrows(CourseImportException.class, () -> importer.importInto(reader, sink));

        assertInstanceOf(IOException.class, ex.getCause());
        assertEquals("close failed", ex.getCause().getMessage());
        assertEquals(2, importer.processedRecords());      
        assertTrue(sink.isEmpty());                        
        assertEquals(1, reader.closeCount());
    }

    @Test
    void importInto_wrongFieldCount_isMalformedWithoutCause() {
        CourseImporter importer = new CourseImporter();
        ScriptedReader reader = new ScriptedReader(feed("1;Java;40", "only-one-field"), false);
        List<Course> sink = new ArrayList<>();

        CourseImportException ex = assertThrows(CourseImportException.class, () -> importer.importInto(reader, sink));

        assertEquals(2, ex.recordNumber());
        assertNull(ex.getCause());
        assertEquals(1, importer.processedRecords());
        assertTrue(sink.isEmpty());
    }
}
