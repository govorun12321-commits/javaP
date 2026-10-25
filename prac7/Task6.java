import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Task6 {
    //"Задание 6. Временный файл и move";
    //"Новая версия сначала полностью записывается во временный файл того же каталога. После закрытия выполняется замена; если atomic move не поддерживается, используется обычная замена.";

    static void save(Path target, List<String> courses) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, target.getFileName().toString(), ".tmp");
        boolean moved = false;

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (String course : courses) {
                    writer.write(course);
                    writer.newLine();
                }
                writer.flush();
            }

            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temp);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path target = Path.of("prac7-data", "courses-atomic.txt");

        save(target, List.of("Java", "Сети", "Операционные системы"));
        System.out.println(Files.readString(target, StandardCharsets.UTF_8));
    }
}
