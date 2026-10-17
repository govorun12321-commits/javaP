/*
 * Задание 3. Собственное исключение
 *
 * Создайте `EnrollmentRejectedException` с `studentId`, `courseId` и reason code.
 * Сохраните исходную причину при оборачивании. `toString` и сообщение не раскрывают
 * лишние данные.
 *
 * Краткое объяснение:
 *  - Исключение checked: отказ в зачислении — ожидаемый исход, вызывающий обязан его учесть.
 *  - studentId/courseId и reason хранятся в полях (для логов и обработки), но НЕ попадают
 *    в getMessage()/toString(): сообщение может уйти пользователю или в общий лог.
 *  - Есть конструктор с cause, поэтому технический сбой (например, БД) при оборачивании
 *    не теряется: getCause() возвращает исходное исключение.
 */

import java.util.Objects;

public final class Task03_CustomException {

    enum ReasonCode {
        COURSE_FULL, PREREQUISITE_MISSING, DUPLICATE_ENROLLMENT, COURSE_CLOSED, TECHNICAL_FAILURE
    }

    static final class EnrollmentRejectedException extends Exception {
        private static final long serialVersionUID = 1L;

        private final long studentId;
        private final long courseId;
        private final ReasonCode reason;

        EnrollmentRejectedException(long studentId, long courseId, ReasonCode reason) {
            this(studentId, courseId, reason, null);
        }

        EnrollmentRejectedException(long studentId, long courseId, ReasonCode reason, Throwable cause) {
            super(messageFor(reason), cause);
            this.studentId = studentId;
            this.courseId = courseId;
            this.reason = reason;
        }

        private static String messageFor(ReasonCode reason) {
            return "Enrollment rejected: " + Objects.requireNonNull(reason, "reason");
        }

        long studentId() { return studentId; }
        long courseId() { return courseId; }
        ReasonCode reason() { return reason; }
    }

    static void enroll(long studentId, long courseId) throws EnrollmentRejectedException {
        try {
            throw new IllegalStateException("db connection lost: jdbc:secret-host/prod");
        } catch (IllegalStateException e) {
            throw new EnrollmentRejectedException(studentId, courseId, ReasonCode.TECHNICAL_FAILURE, e);
        }
    }

    public static void main(String[] args) {
        try {
            enroll(42, 7);
        } catch (EnrollmentRejectedException e) {
            System.out.println("toString: " + e);
            System.out.println("message:  " + e.getMessage());
            System.out.println("reason:   " + e.reason());
            System.out.println("ids (только для внутреннего лога): student=" + e.studentId() + ", course=" + e.courseId());
            System.out.println("cause:    " + e.getCause());
        }
    }
}
