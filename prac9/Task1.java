/*
Задание 1. Потерянные инкременты.
Несколько потоков увеличивают общий счётчик. Получите итог меньше ожидаемого и
разложите counter++ на чтение, вычисление и запись. Тест должен многократно
выявлять проблему. Исправьте счётчик с помощью synchronized. Укажите защищаемый
инвариант и объект-монитор. Сравните синхронизированный метод и минимальный
синхронизированный блок.

Объяснение.
counter++ состоит из трёх шагов: прочитать value, прибавить 1, записать обратно.
Два потока могут прочитать одно и то же значение, оба запишут value + 1, и один
инкремент потеряется.

Защищаемый инвариант: value равно количеству выполненных вызовов increment().

Объект-монитор:
- в синхронизированном методе это this (сам счётчик);
- в синхронизированном блоке это отдельный объект lock.

Сравнение:
- synchronized-метод блокирует весь метод, в том числе код, который не трогает
  общие данные. Код короче, но потоки ждут друг друга дольше.
- synchronized-блок закрывает только строку с изменением value. Работа с
  локальными переменными идёт параллельно, поэтому блокировка держится меньше.
  Монитор lock приватный, и чужой код не может случайно на нём синхронизироваться.
*/
public class Task1 {

    interface Counter {
        void increment();
        int get();
    }

    static class BadCounter implements Counter {
        private int value = 0;

        public void increment() {
            int temp = value;
            temp = temp + 1;
            Thread.yield();
            value = temp;
        }

        public int get() {
            return value;
        }
    }

    static class MethodCounter implements Counter {
        private int value = 0;

        public synchronized void increment() {
            value++;
        }

        public synchronized int get() {
            return value;
        }
    }

    static class BlockCounter implements Counter {
        private final Object lock = new Object();
        private int value = 0;

        public void increment() {
            int step = countStep();
            synchronized (lock) {
                value += step;
            }
        }

        private int countStep() {
            return 2 - 1;
        }

        public int get() {
            synchronized (lock) {
                return value;
            }
        }
    }

    static void runThreads(Counter counter, int threadsCount, int times) throws InterruptedException {
        Thread[] threads = new Thread[threadsCount];
        for (int i = 0; i < threadsCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < times; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int threadsCount = 4;
        int times = 100000;
        int expected = threadsCount * times;

        for (int round = 1; round <= 5; round++) {
            Counter bad = new BadCounter();
            runThreads(bad, threadsCount, times);
            System.out.println("Без sync, раунд " + round + ": " + bad.get() + " из " + expected);
        }

        Counter methodCounter = new MethodCounter();
        long start = System.nanoTime();
        runThreads(methodCounter, threadsCount, times);
        long methodTime = (System.nanoTime() - start) / 1000000;
        System.out.println("Метод: " + methodCounter.get() + ", " + methodTime + " мс");

        Counter blockCounter = new BlockCounter();
        start = System.nanoTime();
        runThreads(blockCounter, threadsCount, times);
        long blockTime = (System.nanoTime() - start) / 1000000;
        System.out.println("Блок: " + blockCounter.get() + ", " + blockTime + " мс");
    }
}
