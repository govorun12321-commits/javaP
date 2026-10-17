/*
 * Задание 15. Полная граница ошибок CourseHub
 *
 * Спроектируйте use case импорта и зачисления: domain validation, ожидаемые отказы,
 * инфраструктурная ошибка и programming error различаются; cause сохраняется; ресурсы
 * закрываются; публичный слой получает стабильные error codes. Напишите минимум 20 тестов,
 * включая параметризованные и негативные. Защитите, какие исключения ловятся на каждом
 * уровне и почему глобальный `catch (Exception)` запрещён.
 *
 * Краткое объяснение — четыре категории ошибок и кто что ловит:
 *
 *  | категория               | представление                          | где ловится / что происходит                  |
 *  |-------------------------|----------------------------------------|-----------------------------------------------|
 *  | domain validation       | DomainValidationException (unchecked)  | CourseFeedParser: NumberFormat + Domain ->    |
 *  |                         |                                        | ImportFormatException(№ записи, cause);       |
 *  |                         |                                        | EnrollRequest: ловит API -> CH-1001           |
 *  | ожидаемые отказы        | значение ErrorCode (без исключений)    | use case возвращает код; API отдаёт его       |
 *  | инфраструктурная ошибка | InfrastructureException (checked, cause| парсер: IOException -> InfrastructureException;|
 *  |                         | = IOException)                         | API -> CH-5001, исходное исключение в лог     |
 *  | programming error       | NPE / IllegalState / и т.п.            | НИКЕМ не ловится: пробрасывается до            |
 *  |                         |                                        | обработчика фреймворка (лог + HTTP 500)       |
 *
 *  Слои: домен (CourseOffer) бросает только DomainValidationException; парсер ловит
 *  NumberFormatException | DomainValidationException | IOException; use case ничего не ловит
 *  (только объявляет checked); публичный API ловит ровно три типа и превращает их в стабильные
 *  коды CH-xxxx, а наружу отдаёт фиксированные безопасные сообщения (без путей, SQL, ид).
 *
 *  Почему catch (Exception) запрещён: (1) он глушит баги (NPE, ClassCast, IllegalState) и выдаёт их
 *  за «обычную» ошибку с кодом — дефект остаётся незамеченным; (2) стирает различие между
 *  ожидаемым, временным и фатальным -> неверная реакция (retry для бага, 500 вместо 400);
 *  (3) ловит и InterruptedException/чужие unchecked, ломая отмену и транзакции;
 *  (4) скрывает контракт методов: сигнатуры перестают говорить, что может пойти не так.
 *  Это защищено тестом programmingError_isNotTranslatedIntoErrorCode.
 *  Формат фида: "id;title;hours;status(OPEN|CLOSED);seatsLeft;prerequisiteId(0 = нет)".
 */

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class Task15_CourseHubErrorBoundaryTest {
    enum ErrorCode {
        OK("CH-0000", "OK"),
        INVALID_REQUEST("CH-1001", "Request is invalid"),
        IMPORT_FORMAT("CH-1002", "Course feed has an invalid format"),
        COURSE_NOT_FOUND("CH-2001", "Course not found"),
        COURSE_NOT_OPEN("CH-2002", "Course is not open for enrollment"),
        DUPLICATE_ENROLLMENT("CH-2003", "Student is already enrolled"),
        PREREQUISITE_MISSING("CH-2004", "Prerequisite is not completed"),
        NO_SEATS("CH-2005", "No seats left"),
        STORAGE_UNAVAILABLE("CH-5001", "Service is temporarily unavailable");

        private final String code;
        private final String publicMessage;

        ErrorCode(String code, String publicMessage) {
            this.code = code;
            this.publicMessage = publicMessage;
        }

        String code() { return code; }
        String publicMessage() { return publicMessage; }
    }

    enum CourseStatus { OPEN, CLOSED }

    static final class DomainValidationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        DomainValidationException(String field, String problem) {
            super("Invalid " + field + ": " + problem);
        }

        DomainValidationException(String field, String problem, Throwable cause) {
            super("Invalid " + field + ": " + problem, cause);
        }
    }

    record CourseOffer(long id, String title, int hours, CourseStatus status, int seatsLeft, long prerequisiteId) {
        CourseOffer {
            if (id <= 0) throw new DomainValidationException("id", "must be positive");
            if (title == null || title.isBlank()) throw new DomainValidationException("title", "must not be blank");
            if (hours < 1 || hours > 300) throw new DomainValidationException("hours", "out of range");
            if (status == null) throw new DomainValidationException("status", "is required");
            if (seatsLeft < 0) throw new DomainValidationException("seatsLeft", "must not be negative");
            if (prerequisiteId < 0) throw new DomainValidationException("prerequisiteId", "must not be negative");
        }
    }

    static final class ImportFormatException extends Exception {
        private static final long serialVersionUID = 1L;
        private final int recordNumber;

        ImportFormatException(int recordNumber) {
            super("Invalid course record #" + recordNumber);
            this.recordNumber = recordNumber;
        }

        ImportFormatException(int recordNumber, Throwable cause) {
            super("Invalid course record #" + recordNumber, cause);
            this.recordNumber = recordNumber;
        }

        int recordNumber() { return recordNumber; }
    }

    static final class InfrastructureException extends Exception {
        private static final long serialVersionUID = 1L;

        InfrastructureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static final class CourseFeedParser {
        List<CourseOffer> parse(Reader source) throws ImportFormatException, InfrastructureException {
            Objects.requireNonNull(source, "source");              // programming error -> NPE
            List<CourseOffer> offers = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(source)) {
                String line;
                int recordNumber = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    recordNumber++;
                    offers.add(parseRecord(line, recordNumber));
                }
            } catch (IOException e) {
                throw new InfrastructureException("Cannot read course feed", e);
            }
            return offers;
        }

        private CourseOffer parseRecord(String line, int recordNumber) throws ImportFormatException {
            String[] f = line.split(";", -1);
            if (f.length != 6) {
                throw new ImportFormatException(recordNumber);
            }
            try {
                return new CourseOffer(
                        Long.parseLong(f[0].trim()),
                        f[1],
                        Integer.parseInt(f[2].trim()),
                        parseStatus(f[3]),
                        Integer.parseInt(f[4].trim()),
                        Long.parseLong(f[5].trim()));
            } catch (NumberFormatException | DomainValidationException e) {
                throw new ImportFormatException(recordNumber, e);
            }
        }

        private CourseStatus parseStatus(String raw) {
            try {
                return CourseStatus.valueOf(raw.trim());
            } catch (IllegalArgumentException e) {
                throw new DomainValidationException("status", "unknown value", e);
            }
        }
    }

    interface EnrollmentStore {
        boolean isEnrolled(long studentId, long courseId) throws InfrastructureException;

        boolean hasCompleted(long studentId, long courseId) throws InfrastructureException;

        void save(long studentId, long courseId) throws InfrastructureException;
    }

    record EnrollRequest(Reader source, long studentId, long courseId) {
        EnrollRequest {
            Objects.requireNonNull(source, "source");            
            if (studentId <= 0) throw new DomainValidationException("studentId", "must be positive");
            if (courseId <= 0) throw new DomainValidationException("courseId", "must be positive");
        }
    }

    static final class ImportAndEnrollUseCase {
        private final CourseFeedParser parser = new CourseFeedParser();
        private final EnrollmentStore store;

        ImportAndEnrollUseCase(EnrollmentStore store) {
            this.store = store;
        }

        ErrorCode execute(EnrollRequest request) throws ImportFormatException, InfrastructureException {
            List<CourseOffer> offers = parser.parse(request.source());
            CourseOffer offer = offers.stream()
                    .filter(o -> o.id() == request.courseId())
                    .findFirst()
                    .orElse(null);

            if (offer == null) return ErrorCode.COURSE_NOT_FOUND;
            if (offer.status() != CourseStatus.OPEN) return ErrorCode.COURSE_NOT_OPEN;
            if (store.isEnrolled(request.studentId(), offer.id())) return ErrorCode.DUPLICATE_ENROLLMENT;
            if (offer.prerequisiteId() != 0
                    && !store.hasCompleted(request.studentId(), offer.prerequisiteId())) {
                return ErrorCode.PREREQUISITE_MISSING;
            }
            if (offer.seatsLeft() == 0) return ErrorCode.NO_SEATS;

            store.save(request.studentId(), offer.id());
            return ErrorCode.OK;
        }
    }

    record ApiResponse(String code, boolean success, String message) { }

    static final class CourseHubApi {
        private final ImportAndEnrollUseCase useCase;
        private final Consumer<Throwable> errorLog;

        CourseHubApi(ImportAndEnrollUseCase useCase, Consumer<Throwable> errorLog) {
            this.useCase = useCase;
            this.errorLog = errorLog;
        }

        ApiResponse enroll(Reader source, long studentId, long courseId) {
            try {
                return response(useCase.execute(new EnrollRequest(source, studentId, courseId)));
            } catch (DomainValidationException e) {
                errorLog.accept(e);
                return response(ErrorCode.INVALID_REQUEST);
            } catch (ImportFormatException e) {
                errorLog.accept(e);
                return response(ErrorCode.IMPORT_FORMAT);
            } catch (InfrastructureException e) {
                errorLog.accept(e);                                
                return response(ErrorCode.STORAGE_UNAVAILABLE);
            }
        }

        private static ApiResponse response(ErrorCode code) {
            return new ApiResponse(code.code(), code == ErrorCode.OK, code.publicMessage());
        }
    }

    static final class FakeStore implements EnrollmentStore {
        final Set<String> enrolled = new HashSet<>();
        final Set<String> completed = new HashSet<>();
        final List<String> saved = new ArrayList<>();
        InfrastructureException failure;
        RuntimeException bug;

        private static String key(long studentId, long courseId) {
            return studentId + ":" + courseId;
        }

        @Override
        public boolean isEnrolled(long studentId, long courseId) throws InfrastructureException {
            maybeFail();
            return enrolled.contains(key(studentId, courseId));
        }

        @Override
        public boolean hasCompleted(long studentId, long courseId) throws InfrastructureException {
            maybeFail();
            return completed.contains(key(studentId, courseId));
        }

        @Override
        public void save(long studentId, long courseId) throws InfrastructureException {
            maybeFail();
            saved.add(key(studentId, courseId));
        }

        private void maybeFail() throws InfrastructureException {
            if (bug != null) throw bug;
            if (failure != null) throw failure;
        }
    }

    static final class TrackingReader extends Reader {
        private final StringReader delegate;
        private final boolean failOnRead;
        private final boolean failOnClose;
        private int closeCount;

        TrackingReader(String content, boolean failOnRead, boolean failOnClose) {
            this.delegate = new StringReader(content);
            this.failOnRead = failOnRead;
            this.failOnClose = failOnClose;
        }

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            if (failOnRead) throw new IOException("disk unreadable");
            return delegate.read(buffer, offset, length);
        }

        @Override
        public void close() throws IOException {
            closeCount++;
            delegate.close();
            if (failOnClose) throw new IOException("close failed");
        }

        int closeCount() { return closeCount; }
    }

    private static final String FEED = String.join("\n",
            "10;Java;40;OPEN;5;0",
            "20;Spring;60;OPEN;3;10",
            "30;Archive;20;CLOSED;5;0",
            "40;Full;20;OPEN;0;0");

    private static Reader feed(String content) {
        return new StringReader(content);
    }

    private final FakeStore store = new FakeStore();
    private final List<Throwable> logged = new ArrayList<>();

    private ImportAndEnrollUseCase useCase() {
        return new ImportAndEnrollUseCase(store);
    }

    private CourseHubApi api() {
        return new CourseHubApi(useCase(), logged::add);
    }

    @Test
    void parse_validFeed_returnsOffers() throws Exception {
        List<CourseOffer> offers = new CourseFeedParser().parse(feed(FEED));

        assertEquals(4, offers.size());
        assertEquals("Java", offers.get(0).title());
        assertEquals(CourseStatus.CLOSED, offers.get(2).status());
    }

    @Test
    void parse_skipsBlankLines() throws Exception {
        List<CourseOffer> offers = new CourseFeedParser().parse(feed("\n10;Java;40;OPEN;5;0\n\n"));

        assertEquals(1, offers.size());
    }

    @ParameterizedTest(name = "неверное число полей: \"{0}\"")
    @ValueSource(strings = {"1;Java;40;OPEN;5", "1;Java;40;OPEN;5;0;extra", "garbage"})
    void parse_wrongFieldCount_throwsFormatExceptionWithRecordNumber(String record) {
        ImportFormatException ex = assertThrows(ImportFormatException.class,
                () -> new CourseFeedParser().parse(feed("9;Ok;10;OPEN;1;0\n" + record)));

        assertEquals(2, ex.recordNumber());
        assertNull(ex.getCause());
    }

    @ParameterizedTest(name = "нечисловое поле: \"{0}\" -> cause NumberFormatException")
    @ValueSource(strings = {"x;Java;40;OPEN;5;0", "1;Java;forty;OPEN;5;0", "1;Java;40;OPEN;many;0", "1;Java;40;OPEN;5;pre"})
    void parse_nonNumericField_keepsNumberFormatExceptionAsCause(String record) {
        ImportFormatException ex = assertThrows(ImportFormatException.class,
                () -> new CourseFeedParser().parse(feed("9;Ok;10;OPEN;1;0\n" + record)));

        assertEquals(2, ex.recordNumber());
        assertInstanceOf(NumberFormatException.class, ex.getCause());
    }

    @ParameterizedTest(name = "нарушение домена: \"{0}\" -> cause DomainValidationException")
    @ValueSource(strings = {
            "1; ;40;OPEN;5;0", "1;Java;0;OPEN;5;0", "1;Java;301;OPEN;5;0",
            "1;Java;40;OPEN;-1;0", "1;Java;40;PENDING;5;0", "0;Java;40;OPEN;5;0", "1;Java;40;OPEN;5;-2"})
    void parse_domainInvalidRecord_wrapsDomainValidationException(String record) {
        ImportFormatException ex = assertThrows(ImportFormatException.class,
                () -> new CourseFeedParser().parse(feed(record)));

        assertEquals(1, ex.recordNumber());
        assertInstanceOf(DomainValidationException.class, ex.getCause());
    }

    @Test
    void parse_success_closesReader() throws Exception {
        TrackingReader reader = new TrackingReader(FEED, false, false);

        new CourseFeedParser().parse(reader);

        assertEquals(1, reader.closeCount());
    }

    @Test
    void parse_formatError_stillClosesReader() {
        TrackingReader reader = new TrackingReader("bad;record", false, false);

        assertThrows(ImportFormatException.class, () -> new CourseFeedParser().parse(reader));

        assertEquals(1, reader.closeCount());
    }

    @Test
    void parse_readFailure_becomesInfrastructureExceptionWithCause() {
        TrackingReader reader = new TrackingReader(FEED, true, false);

        InfrastructureException ex = assertThrows(InfrastructureException.class, () -> new CourseFeedParser().parse(reader));

        assertInstanceOf(IOException.class, ex.getCause());
        assertEquals("disk unreadable", ex.getCause().getMessage());
        assertEquals(1, reader.closeCount());
    }

    @Test
    void parse_closeFailure_becomesInfrastructureException() {
        TrackingReader reader = new TrackingReader(FEED, false, true);

        InfrastructureException ex = assertThrows(InfrastructureException.class, () -> new CourseFeedParser().parse(reader));

        assertEquals("close failed", ex.getCause().getMessage());
    }

    @Test
    void parse_formatErrorAndCloseFailure_keepsCloseErrorAsSuppressed() {
        TrackingReader reader = new TrackingReader("bad;record", false, true);

        ImportFormatException ex = assertThrows(ImportFormatException.class, () -> new CourseFeedParser().parse(reader));

        assertEquals(1, ex.getSuppressed().length);
        assertEquals("close failed", ex.getSuppressed()[0].getMessage());
    }

    @Test
    void parse_nullReader_isProgrammingErrorNotTranslated() {
        assertThrows(NullPointerException.class, () -> new CourseFeedParser().parse(null));
    }

    @ParameterizedTest(name = "student={0}, course={1} -> DomainValidationException")
    @CsvSource({"0, 1", "-1, 1", "1, 0", "1, -5"})
    void request_nonPositiveIds_areRejected(long studentId, long courseId) {
        assertThrows(DomainValidationException.class, () -> new EnrollRequest(feed(FEED), studentId, courseId));
    }

    @Test
    void request_nullSource_isProgrammingError() {
        assertThrows(NullPointerException.class, () -> new EnrollRequest(null, 1, 1));
    }

    @Test
    void execute_openCourseWithSeats_enrollsAndSaves() throws Exception {
        ErrorCode result = useCase().execute(new EnrollRequest(feed(FEED), 1, 10));

        assertEquals(ErrorCode.OK, result);
        assertEquals(List.of("1:10"), store.saved);
    }

    @ParameterizedTest(name = "course={0} -> {1}, ничего не сохраняется")
    @CsvSource({"99, COURSE_NOT_FOUND", "30, COURSE_NOT_OPEN", "40, NO_SEATS"})
    void execute_expectedRefusals_returnCodeAndDoNotSave(long courseId, ErrorCode expected) throws Exception {
        ErrorCode result = useCase().execute(new EnrollRequest(feed(FEED), 1, courseId));

        assertEquals(expected, result);
        assertTrue(store.saved.isEmpty());
    }

    @Test
    void execute_alreadyEnrolled_returnsDuplicate() throws Exception {
        store.enrolled.add("1:10");

        ErrorCode result = useCase().execute(new EnrollRequest(feed(FEED), 1, 10));

        assertEquals(ErrorCode.DUPLICATE_ENROLLMENT, result);
        assertTrue(store.saved.isEmpty());
    }

    @Test
    void execute_prerequisiteNotCompleted_returnsPrerequisiteMissing() throws Exception {
        ErrorCode result = useCase().execute(new EnrollRequest(feed(FEED), 1, 20));

        assertEquals(ErrorCode.PREREQUISITE_MISSING, result);
    }

    @Test
    void execute_prerequisiteCompleted_enrolls() throws Exception {
        store.completed.add("1:10");

        ErrorCode result = useCase().execute(new EnrollRequest(feed(FEED), 1, 20));

        assertEquals(ErrorCode.OK, result);
        assertEquals(List.of("1:20"), store.saved);
    }

    @Test
    void execute_storeFailure_propagatesInfrastructureExceptionWithCause() {
        store.failure = new InfrastructureException("store down", new IOException("db timeout"));

        InfrastructureException ex = assertThrows(InfrastructureException.class,
                () -> useCase().execute(new EnrollRequest(feed(FEED), 1, 10)));

        assertEquals("db timeout", ex.getCause().getMessage());
    }

    @Test
    void api_success_returnsOkCode() {
        ApiResponse response = api().enroll(feed(FEED), 1, 10);

        assertEquals("CH-0000", response.code());
        assertTrue(response.success());
        assertTrue(logged.isEmpty());
    }

    @ParameterizedTest(name = "course={0} -> {1}")
    @CsvSource({"99, CH-2001", "30, CH-2002", "40, CH-2005", "20, CH-2004"})
    void api_expectedRefusals_mapToStableCodes(long courseId, String expectedCode) {
        ApiResponse response = api().enroll(feed(FEED), 1, courseId);

        assertEquals(expectedCode, response.code());
        assertFalse(response.success());
        assertTrue(logged.isEmpty(), "ожидаемые отказы — не ошибки, в лог ошибок не попадают");
    }

    @Test
    void api_malformedFeed_returnsImportFormatCodeAndLogsCause() {
        ApiResponse response = api().enroll(feed("1;Java;forty;OPEN;5;0"), 1, 1);

        assertEquals("CH-1002", response.code());
        ImportFormatException logged0 = assertInstanceOf(ImportFormatException.class, logged.get(0));
        assertInstanceOf(NumberFormatException.class, logged0.getCause());
    }

    @Test
    void api_invalidIds_returnInvalidRequestCode() {
        ApiResponse response = api().enroll(feed(FEED), 0, 10);

        assertEquals("CH-1001", response.code());
        assertInstanceOf(DomainValidationException.class, logged.get(0));
    }

    @Test
    void api_storageFailure_returnsStorageCodeAndLogsOriginalCause() {
        store.failure = new InfrastructureException("store down", new IOException("secret path /var/db/prod"));

        ApiResponse response = api().enroll(feed(FEED), 1, 10);

        assertEquals("CH-5001", response.code());
        assertEquals(1, logged.size());
        assertEquals("secret path /var/db/prod", logged.get(0).getCause().getMessage());
    }

    @Test
    void api_response_doesNotLeakInternalDetails() {
        store.failure = new InfrastructureException("store down", new IOException("secret path /var/db/prod"));

        ApiResponse response = api().enroll(feed(FEED), 1, 10);

        assertFalse(response.message().contains("secret"));
        assertFalse(response.message().contains("/var"));
    }

    @Test
    void api_readFailure_returnsStorageCode() {
        ApiResponse response = api().enroll(new TrackingReader(FEED, true, false), 1, 10);

        assertEquals("CH-5001", response.code());
    }

    @Test
    void api_programmingError_isNotTranslatedIntoErrorCode() {
        store.bug = new IllegalStateException("bug in store implementation");

        assertThrows(IllegalStateException.class, () -> api().enroll(feed(FEED), 1, 10));

        assertTrue(logged.isEmpty(), "баг не должен маскироваться под обычную ошибку с кодом");
    }

    @Test
    void api_nullSource_isProgrammingErrorAndPropagates() {
        assertThrows(NullPointerException.class, () -> api().enroll(null, 1, 10));
    }

    @Test
    void errorCodes_areUniqueAndStable() {
        Set<String> codes = new HashSet<>();
        for (ErrorCode code : ErrorCode.values()) {
            assertTrue(codes.add(code.code()), "duplicate code " + code.code());
            assertTrue(code.code().matches("CH-\\d{4}"));
        }
        assertEquals("CH-0000", ErrorCode.OK.code());
        assertEquals("CH-1001", ErrorCode.INVALID_REQUEST.code());
        assertEquals("CH-1002", ErrorCode.IMPORT_FORMAT.code());
        assertEquals("CH-5001", ErrorCode.STORAGE_UNAVAILABLE.code());
    }
}
