/*
Задание 2. Флаг остановки и visibility.
Реализуйте worker с флагом остановки, исследуйте некорректный вариант и
исправьте его через volatile либо синхронизацию. Объясните, почему volatile
обеспечивает видимость, но не превращает составную операцию в атомарную.

Объяснение.
Некорректный вариант: поле running обычное. Поток worker может держать его в
регистре или кэше, и после running = false в главном потоке цикл продолжает
крутиться вечно. JVM не обязана показывать запись одного потока другому без
специальной связи (happens-before).

Исправление: volatile boolean running. Запись в volatile-поле происходит до
каждого последующего чтения этого поля, поэтому worker сразу видит false.
Так volatile обеспечивает видимость.

volatile не делает составную операцию атомарной. hits++ это чтение, прибавление
и запись. Каждый шаг по отдельности видим, но между чтением и записью другой
поток может вклиниться, и инкремент потеряется. Для атомарности нужен
synchronized или AtomicInteger.
*/
import java.util.concurrent.atomic.AtomicInteger;

public class Task2 {

    static class BadWorker extends Thread {
        private boolean running = true;

        public void run() {
            long ticks = 0;
            while (running) {
                ticks++;
            }
        }

        void stopWork() {
            running = false;
        }
    }

    static class GoodWorker extends Thread {
        private volatile boolean running = true;

        public void run() {
            long ticks = 0;
            while (running) {
                ticks++;
            }
        }

        void stopWork() {
            running = false;
        }
    }

    static volatile int hits = 0;

    static void checkBad() throws InterruptedException {
        BadWorker worker = new BadWorker();
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(500);
        worker.stopWork();
        worker.join(1000);
        if (worker.isAlive()) {
            System.out.println("Без volatile: не остановился");
        } else {
            System.out.println("Без volatile: остановился (повезло)");
        }
    }

    static void checkGood() throws InterruptedException {
        GoodWorker worker = new GoodWorker();
        worker.start();
        Thread.sleep(500);
        worker.stopWork();
        worker.join(1000);
        if (worker.isAlive()) {
            System.out.println("С volatile: не остановился");
        } else {
            System.out.println("С volatile: остановился");
        }
    }

    static void checkVolatileCounter() throws InterruptedException {
        AtomicInteger atomicHits = new AtomicInteger();
        Runnable job = () -> {
            for (int i = 0; i < 100000; i++) {
                hits++;
                atomicHits.incrementAndGet();
            }
        };
        Thread first = new Thread(job);
        Thread second = new Thread(job);
        first.start();
        second.start();
        first.join();
        second.join();
        System.out.println("volatile int: " + hits + " из 200000");
        System.out.println("AtomicInteger: " + atomicHits.get() + " из 200000");
    }

    public static void main(String[] args) throws InterruptedException {
        checkBad();
        checkGood();
        checkVolatileCounter();
    }
}
