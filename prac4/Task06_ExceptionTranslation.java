/*
 * Задание 6. Граница перевода исключения
 *
 * Низкоуровневый parser выбрасывает `NumberFormatException`. На уровне импорта
 * преобразуйте её в `CourseImportException` с номером записи и cause. Не ловите
 * `Exception` и не теряйте контекст.
 *
 * Краткое объяснение:
 *  - Низкий уровень (LowLevelParser) ничего не знает об импорте и просто бросает NumberFormatException.
 *  - Уровень импорта знает номер записи, поэтому именно он ловит КОНКРЕТНЫЕ типы
 *    (NumberFormatException, IllegalArgumentException от Course) и переводит их в
 *    CourseImportException(recordNumber, cause).
 *  - cause сохраняется => полный стек и исходное сообщение доступны в логах.
 *  - Сообщение не содержит «сырых» данных записи.
 *  - try-блоки узкие: в них только вызовы, которые могут бросить ожидаемое исключение.
 *    catch (Exception) не используется — иначе были бы «проглочены» и баги вроде NPE.
 */
import java.util.ArrayList;
import java.util.List;

public final class Task06_ExceptionTranslation {

    static final class CourseImportException extends Exception {
        private static final long serialVersionUID = 1L;
        private final int recordNumber;

        CourseImportException(int recordNumber, Throwable cause) {
            super("Invalid course record #" + recordNumber, cause);
            this.recordNumber = recordNumber;
        }

        CourseImportException(int recordNumber, String detail) {
            super("Invalid course record #" + recordNumber + ": " + detail);
            this.recordNumber = recordNumber;
        }

        int recordNumber() { return recordNumber; }
    }

    static final class LowLevelParser {
        static int parseDuration(String raw) {
            return Integer.parseInt(raw.trim());      
        }
    }

    static List<Course> importCourses(List<String> lines) throws CourseImportException {
        List<Course> result = new ArrayList<>();
        int recordNumber = 0;
        for (String line : lines) {
            recordNumber++;
            String[] fields = line.split(";", -1);
            if (fields.length != 3) {
                throw new CourseImportException(recordNumber, "expected 3 fields");
            }

            long id;
            int hours;
            try {
                id = Long.parseLong(fields[0].trim());
                hours = LowLevelParser.parseDuration(fields[2]);
            } catch (NumberFormatException e) {
                throw new CourseImportException(recordNumber, e);
            }

            try {
                result.add(new Course(id, fields[1], hours));
            } catch (IllegalArgumentException e) {        
                throw new CourseImportException(recordNumber, e);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> lines = List.of(
                "1;Java;40",
                "2;Kotlin;30",
                "3;Go;ten");

        try {
            importCourses(lines);
        } catch (CourseImportException e) {
            System.out.println("message:      " + e.getMessage());
            System.out.println("recordNumber: " + e.recordNumber());
            System.out.println("cause:        " + e.getCause());
        }
    }
}
