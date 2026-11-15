/*
Задание 4. Producer–consumer.
Организуйте обработку заявок через ограниченную BlockingQueue. Несколько
producers добавляют заявки, consumers обрабатывают их. Реализуйте корректное
завершение без busy waiting и объясните, как ёмкость очереди создаёт
backpressure. Перенесите обработку на ExecutorService. Выберите размер пула для
заданного CPU-bound или blocking-сценария, дайте потокам осмысленные имена и
гарантируйте shutdown, ограниченное ожидание и аварийный shutdownNow.

Объяснение.
Очередь ArrayBlockingQueue с ёмкостью 5. Метод put() блокирует producer, когда
очередь заполнена, а take() блокирует consumer, когда очередь пуста. Потоки
спят и не крутят цикл, поэтому busy waiting нет.

Backpressure: если consumers работают медленнее producers, очередь заполняется
до предела, и producers останавливаются на put(). Скорость заявок подстраивается
под скорость обработки, а память не растёт бесконечно.

Завершение: когда все producers закончили, в очередь кладётся по одной
специальной заявке STOP на каждого consumer. Получив STOP, consumer выходит из
цикла.

Размер пула: обработка заявки здесь blocking (имитация ожидания ответа от
внешнего сервиса через sleep). Потоки большую часть времени ждут, а не считают,
поэтому consumers берём с запасом: ядер * 2. Для CPU-bound задачи брали бы
столько потоков, сколько ядер (иногда ядра + 1), потому что лишние потоки
только мешали бы друг другу. Producers только кладут заявки, им хватит 2 потоков.

Остановка пула: shutdown(), затем awaitTermination с ограничением по времени,
а если не успели, shutdownNow() прерывает потоки.
*/
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Task4 {

    static class Order {
        final int id;

        Order(int id) {
            this.id = id;
        }
    }

    static final Order STOP = new Order(-1);

    static void produce(BlockingQueue<Order> queue, int firstId, int count) {
        try {
            for (int i = 0; i < count; i++) {
                queue.put(new Order(firstId + i));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void consume(BlockingQueue<Order> queue, AtomicInteger done) {
        try {
            while (true) {
                Order order = queue.take();
                if (order == STOP) {
                    break;
                }
                Thread.sleep(5);
                done.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void runWithThreads() throws InterruptedException {
        BlockingQueue<Order> queue = new ArrayBlockingQueue<>(5);
        AtomicInteger done = new AtomicInteger();
        int producersCount = 2;
        int consumersCount = 3;
        int ordersEach = 20;

        Thread[] producers = new Thread[producersCount];
        Thread[] consumers = new Thread[consumersCount];

        for (int i = 0; i < consumersCount; i++) {
            consumers[i] = new Thread(() -> consume(queue, done));
            consumers[i].start();
        }
        for (int i = 0; i < producersCount; i++) {
            int firstId = i * ordersEach;
            producers[i] = new Thread(() -> produce(queue, firstId, ordersEach));
            producers[i].start();
        }

        for (Thread producer : producers) {
            producer.join();
        }
        for (int i = 0; i < consumersCount; i++) {
            queue.put(STOP);
        }
        for (Thread consumer : consumers) {
            consumer.join();
        }
        System.out.println("Потоки: обработано " + done.get());
    }

    static ThreadFactory namedThreads(String prefix) {
        AtomicInteger number = new AtomicInteger(1);
        return task -> new Thread(task, prefix + "-" + number.getAndIncrement());
    }

    static void stopPool(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.out.println("Пул не остановился");
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    static void runWithExecutors() throws InterruptedException {
        BlockingQueue<Order> queue = new ArrayBlockingQueue<>(5);
        AtomicInteger done = new AtomicInteger();
        int producersCount = 2;
        int consumersCount = Runtime.getRuntime().availableProcessors() * 2;
        int ordersEach = 20;

        ExecutorService producersPool = Executors.newFixedThreadPool(producersCount, namedThreads("producer"));
        ExecutorService consumersPool = Executors.newFixedThreadPool(consumersCount, namedThreads("consumer"));

        try {
            for (int i = 0; i < consumersCount; i++) {
                consumersPool.submit(() -> consume(queue, done));
            }
            for (int i = 0; i < producersCount; i++) {
                int firstId = i * ordersEach;
                producersPool.submit(() -> produce(queue, firstId, ordersEach));
            }
            stopPool(producersPool);
            for (int i = 0; i < consumersCount; i++) {
                queue.put(STOP);
            }
        } finally {
            stopPool(producersPool);
            stopPool(consumersPool);
        }
        System.out.println("Executor: обработано " + done.get());
    }

    public static void main(String[] args) throws InterruptedException {
        runWithThreads();
        runWithExecutors();
    }
}
