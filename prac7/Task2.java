import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Task2 {
    //"Задание 2. Reader/Writer";
    //"BufferedReader читает текст построчно, BufferedWriter записывает его буферизированно. Байты декодируются в символы при чтении с выбранной кодировкой UTF-8.";

    public static void main(String[] args) throws IOException {
        Path input = Path.of("prac7-data", "reader-input.txt");
        Path output = Path.of("prac7-data", "reader-output.txt");
        Files.createDirectories(input.getParent());
        Files.writeString(input, "Java\nПривет, мир!\nТретья строка\n", StandardCharsets.UTF_8);

        long lineCount = 0;
        long charCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                charCount += line.length();
                writer.write(line);
                writer.newLine();
            }
        }

        System.out.println("Количество строк: " + lineCount);
        System.out.println("Количество символов без разделителей строк: " + charCount);
        System.out.println("Результат записан в " + output);
    }
}
