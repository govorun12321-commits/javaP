/*
Задание 5. Детерминированный конкурентный тест.
Напишите JUnit-тест гонки с CountDownLatch, CyclicBarrier или Phaser, управляя
моментом старта и точками протокола. Не используйте Thread.sleep как средство
синхронизации. Тест имеет timeout и всегда освобождает executor.

Объяснение.
Тестируется продажа последнего билета по схеме «проверить, затем взять».
1) Тест unsafeTicketIsSoldTwice. В небезопасном билете между проверкой и записью
   стоит точка протокола: CyclicBarrier на 2 потока. Оба потока сначала проходят
   проверку, ждут друг друга на барьере и только потом записывают. Гонка
   воспроизводится каждый раз, а не случайно, и билет продаётся дважды.
2) Тест safeTicketIsSoldOnce. Потоки ждут CountDownLatch и стартуют одновременно.
   В синхронизированном билете успех должен быть ровно один.
Таймаут задан через @Timeout, executor закрывается в @AfterEach через shutdownNow.
Нужен JUnit 5 (junit-jupiter).
*/
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class Task5 {

    static class UnsafeTicket {
        private boolean free = true;
        private final Runnable pause;

        UnsafeTicket(Runnable pause) {
            this.pause = pause;
        }

        boolean take() {
            if (free) {
                pause.run();
                free = false;
                return true;
            }
            return false;
        }
    }

    static class SafeTicket {
        private boolean free = true;

        synchronized boolean take() {
            if (free) {
                free = false;
                return true;
            }
            return false;
        }
    }

    private ExecutorService pool;

    @BeforeEach
    void createPool() {
        pool = Executors.newFixedThreadPool(8);
    }

    @AfterEach
    void closePool() {
        pool.shutdownNow();
    }

    @Test
    @Timeout(10)
    void unsafeTicketIsSoldTwice() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        UnsafeTicket ticket = new UnsafeTicket(() -> {
            try {
                barrier.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        Future<Boolean> first = pool.submit(ticket::take);
        Future<Boolean> second = pool.submit(ticket::take);

        int sold = 0;
        if (first.get(5, TimeUnit.SECONDS)) {
            sold++;
        }
        if (second.get(5, TimeUnit.SECONDS)) {
            sold++;
        }
        assertEquals(2, sold);
    }

    @Test
    @Timeout(10)
    void safeTicketIsSoldOnce() throws Exception {
        int threadsCount = 8;
        for (int round = 0; round < 20; round++) {
            SafeTicket ticket = new SafeTicket();
            CountDownLatch ready = new CountDownLatch(threadsCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Boolean>> results = new ArrayList<>();

            for (int i = 0; i < threadsCount; i++) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return ticket.take();
                }));
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            int sold = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    sold++;
                }
            }
            assertEquals(1, sold);
        }
    }
}
