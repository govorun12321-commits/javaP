import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Task4 {
    //"Задание 4. Ленивые строки";
    //"Stream из Files.lines владеет открытым файловым ресурсом. Его нужно закрыть через try-with-resources; ошибочный метод ниже возвращает уже закрытый stream.";

    static Stream<String> wrongLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines;
        }
    }

    static List<String> readCorrectly(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.filter(line -> !line.isBlank()).toList();
        }
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of("prac7-data", "lines.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "Java\n\nФайлы\nПотоки\n", StandardCharsets.UTF_8);


        System.out.println("Корректное чтение: " + readCorrectly(file));

        try {
            Stream<String> broken = wrongLines(file);
            broken.count();
            throw new IllegalStateException("Ожидалось, что возвращённый stream уже закрыт");
        } catch (IllegalStateException e) {
            System.out.println("Ошибочный возврат воспроизведён: " + e.getMessage());
        }
    }
}
