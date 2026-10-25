import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Task5 {
    //"Задание 5. Бинарный формат";
    //"Файл содержит magic header, версию, число записей и строки. При неизвестной версии, неверном заголовке или обрезанном файле чтение завершается ошибкой.";

    private static final int MAGIC = 0x43525331;
    private static final int VERSION = 1;

    record Course(String name, int hours) {}

    static void write(Path path, List<Course> courses) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(courses.size());
            for (Course course : courses) {
                out.writeUTF(course.name());
                out.writeInt(course.hours());
            }
        }
    }

    static List<Course> read(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            int magic = in.readInt();
            if (magic != MAGIC) {
                throw new IOException("Неверный magic header");
            }
            int version = in.readInt();
            if (version != VERSION) {
                throw new IOException("Неизвестная версия формата: " + version);
            }
            int count = in.readInt();
            if (count < 0 || count > 100_000) {
                throw new IOException("Недопустимое количество записей: " + count);
            }
            List<Course> courses = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                courses.add(new Course(in.readUTF(), in.readInt()));
            }
            if (in.read() != -1) {
                throw new IOException("В файле есть лишние данные");
            }
            return courses;
        } catch (EOFException e) {
            throw new IOException("Файл обрезан: не хватает данных записи", e);
        }
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of("prac7-data", "courses.bin");
        Files.createDirectories(file.getParent());
        List<Course> courses = List.of(
                new Course("Java", 72),
                new Course("SQL", 36),
                new Course("Алгоритмы", 48)
        );

        write(file, courses);
        System.out.println("Прочитано: " + read(file));

        Path truncated = file.resolveSibling("courses-truncated.bin");
        byte[] data = Files.readAllBytes(file);
        Files.write(truncated, java.util.Arrays.copyOf(data, Math.max(0, data.length - 3)));
        try {
            read(truncated);
            throw new IllegalStateException("Ожидалась ошибка обрезанного файла");
        } catch (IOException e) {
            System.out.println("Обрезанный файл отклонён: " + e.getMessage());
        }
    }
}
