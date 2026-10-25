import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Task9 {
    //"Задание 9. Версионируемый формат";
    //"Формат содержит версию схемы и SHA-256 контрольную сумму полезной нагрузки. 
    // Версия 1 мигрируется в модель версии 2, будущая версия отклоняется, а данные проверяются до создания итогового списка.";

    private static final int MAGIC = 0x43525632;
    private static final int CURRENT_VERSION = 2;

    record Course(String name, int hours, String category) {}

    static byte[] checksum(byte[] data) throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 недоступен", e);
        }
    }

    static byte[] encodeV2(List<Course> courses) throws IOException {
        ByteArrayOutputStream payloadBytes = new ByteArrayOutputStream();
        try (DataOutputStream payload = new DataOutputStream(payloadBytes)) {
            payload.writeInt(courses.size());
            for (Course course : courses) {
                payload.writeUTF(course.name());
                payload.writeInt(course.hours());
                payload.writeUTF(course.category());
            }
        }
        byte[] payload = payloadBytes.toByteArray();
        byte[] hash = checksum(payload);

        ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(fileBytes)) {
            out.writeInt(MAGIC);
            out.writeInt(CURRENT_VERSION);
            out.writeInt(payload.length);
            out.write(payload);
            out.writeInt(hash.length);
            out.write(hash);
        }
        return fileBytes.toByteArray();
    }

    static List<Course> decode(byte[] file) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(file))) {
            if (in.readInt() != MAGIC) {
                throw new IOException("Неверный заголовок");
            }
            int version = in.readInt();
            if (version != 1 && version != 2) {
                throw new IOException("Неизвестная версия схемы: " + version);
            }

            int payloadLength = in.readInt();
            if (payloadLength < 0 || payloadLength > file.length) {
                throw new IOException("Некорректная длина данных");
            }
            byte[] payload = in.readNBytes(payloadLength);
            if (payload.length != payloadLength) {
                throw new IOException("Обрезанная полезная нагрузка");
            }
            int hashLength = in.readInt();
            if (hashLength != 32) {
                throw new IOException("Некорректная длина checksum");
            }
            byte[] storedHash = in.readNBytes(hashLength);
            if (storedHash.length != hashLength || in.read() != -1) {
                throw new IOException("Обрезанный файл или лишние данные");
            }
            if (!MessageDigest.isEqual(storedHash, checksum(payload))) {
                throw new IOException("Checksum не совпадает: данные повреждены");
            }

            List<Course> temporary = new ArrayList<>();
            try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(payload))) {
                int count = data.readInt();
                if (count < 0 || count > 100_000) {
                    throw new IOException("Недопустимое количество записей");
                }
                for (int i = 0; i < count; i++) {
                    String name = data.readUTF();
                    int hours = data.readInt();
                    String category = version == 1 ? "Без категории" : data.readUTF();
                    if (name.isBlank() || hours <= 0) {
                        throw new IOException("Некорректная запись номер " + (i + 1));
                    }
                    temporary.add(new Course(name, hours, category));
                }
                if (data.read() != -1) {
                    throw new IOException("Лишние данные внутри полезной нагрузки");
                }
            }
            return List.copyOf(temporary);
        } catch (java.io.EOFException e) {
            throw new IOException("Файл обрезан", e);
        }
    }

    static byte[] makeV1(List<Course> courses) throws IOException {
        ByteArrayOutputStream payloadBytes = new ByteArrayOutputStream();
        try (DataOutputStream payload = new DataOutputStream(payloadBytes)) {
            payload.writeInt(courses.size());
            for (Course course : courses) {
                payload.writeUTF(course.name());
                payload.writeInt(course.hours());
            }
        }
        byte[] payload = payloadBytes.toByteArray();
        byte[] hash = checksum(payload);
        ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(fileBytes)) {
            out.writeInt(MAGIC);
            out.writeInt(1);
            out.writeInt(payload.length);
            out.write(payload);
            out.writeInt(hash.length);
            out.write(hash);
        }
        return fileBytes.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        Path directory = Path.of("prac7-data");
        Files.createDirectories(directory);
        Path file = directory.resolve("courses-v2.bin");

        List<Course> original = List.of(
                new Course("Java", 72, "Программирование"),
                new Course("SQL", 36, "Базы данных")
        );
        Files.write(file, encodeV2(original));
        System.out.println("Версия 2: " + decode(Files.readAllBytes(file)));

        byte[] oldFormat = makeV1(List.of(new Course("Сети", 24, "")));
        System.out.println("После миграции версии 1: " + decode(oldFormat));

        byte[] corrupted = Files.readAllBytes(file);
        corrupted[corrupted.length / 2] ^= 1;
        try {
            decode(corrupted);
            throw new IllegalStateException("Повреждение должно было обнаружиться");
        } catch (IOException e) {
            System.out.println("Повреждённый файл отклонён: " + e.getMessage());
        }

        byte[] future = Arrays.copyOf(Files.readAllBytes(file), Files.readAllBytes(file).length);
        future[7] = 3;
        try {
            decode(future);
            throw new IllegalStateException("Будущая версия должна быть отклонена");
        } catch (IOException e) {
            System.out.println("Будущая версия отклонена: " + e.getMessage());
        }
    }
}
