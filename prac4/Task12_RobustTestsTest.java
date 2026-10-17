/*
 * Задание 12. Устранить хрупкие тесты
 *
 * Исправьте тесты, зависящие от порядка выполнения, общего static state, текущей директории
 * и точного полного текста `toString`. Каждый тест запускается отдельно и в случайном порядке.
 *
 * Краткое объяснение — что было и как исправлено:
 *
 *  1. Общий static state + порядок.
 *     БЫЛО:  class IdGenerator { static long next = 1; static long nextId() { return next++; } }
 *            test1: assertEquals(1, IdGenerator.nextId());   // проходит, только если запущен первым
 *            test2: assertEquals(2, IdGenerator.nextId());   // проходит, только если запущен после test1
 *     СТАЛО: состояние — поле экземпляра; @BeforeEach создаёт НОВЫЙ генератор для каждого теста.
 *
 *  2. Текущая директория.
 *     БЫЛО:  new FileWriter("report.txt") — файл создаётся в user.dir; тест зависит от места запуска
 *            и мусорит в проекте; тесты мешают друг другу.
 *     СТАЛО: писатель принимает каталог параметром, тест использует @TempDir (уникальный каталог,
 *            удаляется автоматически).
 *
 *  3. Точный toString.
 *     БЫЛО:  assertEquals("Ticket{id=7, title='Java'}", ticket.toString());
 *            Любое косметическое изменение формата ломает тест, хотя поведение верное.
 *     СТАЛО: проверяем только существенное — что строка содержит id и название.
 *
 *  4. Порядок. @TestMethodOrder(MethodOrderer.Random.class) каждый запуск выполняет тесты в новом
 *     порядке; seed печатается в логе (junit.jupiter.execution.order.random.seed для воспроизведения).
 *     Отдельный запуск: mvn -Dtest=Task12_RobustTestsTest#generator_startsFromOne test
 */

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

@TestMethodOrder(MethodOrderer.Random.class)
class Task12_RobustTestsTest {

    static final class IdGenerator {
        private long next = 1;                

        long nextId() {
            return next++;
        }
    }

    static final class ReportWriter {
        Path write(Path directory, String fileName, List<String> lines) throws IOException {
            return Files.write(directory.resolve(fileName), lines, StandardCharsets.UTF_8);
        }
    }

    static final class Ticket {
        private final long id;
        private final String title;

        Ticket(long id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return "Ticket{id=" + id + ", title='" + title + "'}";
        }
    }

    private IdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new IdGenerator();        
    }

    @Test
    void generator_startsFromOne() {
        assertEquals(1, generator.nextId());
    }

    @Test
    void generator_incrementsByOne() {
        generator.nextId();

        long second = generator.nextId();

        assertEquals(2, second);
    }

    @Test
    void write_createsFileInGivenDirectory(@TempDir Path directory) throws IOException {
        ReportWriter writer = new ReportWriter();

        Path file = writer.write(directory, "report.txt", List.of("line1", "line2"));

        assertEquals(directory, file.getParent());
        assertEquals(List.of("line1", "line2"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void toString_containsIdAndTitle_withoutDependingOnExactFormat() {
        Ticket ticket = new Ticket(7, "Java");

        String text = ticket.toString();

        assertTrue(text.contains("7"));
        assertTrue(text.contains("Java"));
    }
}
