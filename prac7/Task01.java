import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Task01 {
    
    public static void main(String[] args) throws IOException {
        Path directory = Path.of("prac7-data");
        Files.createDirectories(directory);
        Path file = directory.resolve("courses.txt");

        List<String> courses = List.of(
                "Java",
                "Алгоритмы и структуры данных",
                "Базы данных"
        );
        String content = String.join(System.lineSeparator(), courses) + System.lineSeparator();

        Files.writeString(file, content, StandardCharsets.UTF_8);

        try {
            String loaded = Files.readString(file, StandardCharsets.UTF_8);
            System.out.println("Прочитанный каталог:");
            System.out.print(loaded);
        } catch (java.nio.file.NoSuchFileException e) {
            System.out.println("Файл каталога не найден: " + file);
        } catch (IOException e) {
            throw new IOException("Не удалось прочитать каталог курсов: " + file, e);
        }

        Path missing = directory.resolve("missing.txt");
        try {
            System.out.println(Files.readString(missing, StandardCharsets.UTF_8));
        } catch (java.nio.file.NoSuchFileException e) {
            System.out.println("Ожидаемое отсутствие файла обработано: " + missing);
        }

        Path existingDirectory = directory.resolve("not-a-file");
        Files.createDirectories(existingDirectory);
        try {
            Files.readString(existingDirectory, StandardCharsets.UTF_8);
            throw new IllegalStateException("Ожидалась ошибка: указан каталог вместо файла");
        } catch (IOException e) {
            System.out.println("Каталог вместо файла отклонён: " + e.getClass().getSimpleName());
        }
    }
}