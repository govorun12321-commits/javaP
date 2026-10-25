import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Task3 {
    //"Задание 3. Try-with-resources";
    //"При исключении внутри pipeline оба ресурса закрываются автоматически. Ресурс, объявленный первым, закрывается последним.";

    static class TestResource implements AutoCloseable {
        private final String name;
        private final List<String> events;

        TestResource(String name, List<String> events) {
            this.name = name;
            this.events = events;
            events.add("open " + name);
        }

        void process() {
            events.add("process " + name);
        }

        @Override
        public void close() {
            events.add("close " + name);
        }
    }

    public static void main(String[] args) {
        List<String> events = new ArrayList<>();


        try (TestResource reader = new TestResource("reader", events);
             TestResource writer = new TestResource("writer", events)) {
            reader.process();
            writer.process();
            throw new IOException("Искусственная ошибка в середине pipeline");
        } catch (IOException e) {
            events.add("caught " + e.getMessage());
        }

        events.forEach(System.out::println);
        int writerClose = events.indexOf("close writer");
        int readerClose = events.indexOf("close reader");
        if (writerClose < 0 || readerClose < 0 || writerClose > readerClose) {
            throw new IllegalStateException("Ресурсы закрылись не в ожидаемом порядке");
        }
        System.out.println("Проверка пройдена: writer закрыт раньше reader.");
    }
}
