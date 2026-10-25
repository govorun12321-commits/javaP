import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Task7 {
    //"Задание 7. Частично повреждённый импорт";
    //"Выбран контракт all-or-nothing: если найдена хотя бы одна ошибка, каталог не публикуется. Результат явно содержит либо готовый каталог, либо список ошибок.";

    record Course(String name, int hours) {}
    record ImportError(int line, String message) {}
    record ImportResult(boolean success, List<Course> courses, List<ImportError> errors) {}

    static ImportResult importFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Course> temporary = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(";", -1);
            if (parts.length != 2 || parts[0].isBlank()) {
                errors.add(new ImportError(i + 1, "Ожидается формат название;часы"));
                continue;
            }
            try {
                int hours = Integer.parseInt(parts[1].trim());
                if (hours <= 0) {
                    throw new NumberFormatException();
                }
                temporary.add(new Course(parts[0].trim(), hours));
            } catch (NumberFormatException e) {
                errors.add(new ImportError(i + 1, "Количество часов должно быть положительным целым числом"));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportResult(false, List.of(), List.copyOf(errors));
        }
        return new ImportResult(true, List.copyOf(temporary), List.of());
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of("prac7-data", "import.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "Java;72\nSQL;36\nНеверная строка\nСети;-4\n", StandardCharsets.UTF_8);

        ImportResult result = importFile(file);
        if (result.success()) {
            System.out.println("Каталог опубликован: " + result.courses());
        } else {
            System.out.println("Импорт отменён. Каталог не изменён.");
            for (ImportError error : result.errors()) {
                System.out.println("Строка " + error.line() + ": " + error.message());
            }
        }
    }
}
