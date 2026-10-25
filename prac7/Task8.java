import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Task8 {
    //"Задание 8. Потоковый импорт большого файла";
    //"Файл обрабатывается построчно, поэтому целиком в память не загружается. Счётчики обновляются во время чтения, ошибки ограничены по количеству, импорт останавливается после заданного порога ошибок.";

    record Course(String name, int hours) {}
    record ImportSummary(long linesRead, long imported, long errors, boolean cancelled, List<String> errorMessages) {}

    static ImportSummary importLarge(Path path, int maxErrorsToKeep, long cancelAfterErrors) throws IOException {
        long linesRead = 0;
        long imported = 0;
        long errors = 0;
        boolean cancelled = false;
        List<String> messages = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                linesRead++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                try {
                    if (parts.length != 2 || parts[0].isBlank()) {
                        throw new IllegalArgumentException("Ожидается название;часы");
                    }
                    int hours = Integer.parseInt(parts[1].trim());
                    if (hours <= 0) {
                        throw new IllegalArgumentException("Часы должны быть положительными");
                    }
                    imported++;
                } catch (RuntimeException e) {
                    errors++;
                    if (messages.size() < maxErrorsToKeep) {
                        messages.add("Строка " + linesRead + ": " + e.getMessage());
                    }
                    if (errors >= cancelAfterErrors) {
                        cancelled = true;
                        break;
                    }
                }
            }
        }
        return new ImportSummary(linesRead, imported, errors, cancelled, List.copyOf(messages));
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of("prac7-data", "large-import.txt");
        Files.createDirectories(file.getParent());
        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (int i = 1; i <= 100_000; i++) {
                if (i % 997 == 0) {
                    writer.write("ошибка-" + i + ";abc");
                } else {
                    writer.write("Курс-" + i + ";36");
                }
                writer.newLine();
            }
        }


        ImportSummary summary = importLarge(file, 5, 10);
        System.out.println("Прочитано строк: " + summary.linesRead());
        System.out.println("Корректных записей: " + summary.imported());
        System.out.println("Ошибок найдено: " + summary.errors());
        System.out.println("Импорт отменён: " + summary.cancelled());
        summary.errorMessages().forEach(System.out::println);
    }
}
