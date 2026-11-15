/*
Задание 6. Запись на курс с ограничением мест.
Много потоков одновременно вызывают enroll. Число успешных записей не превышает
capacity, один студент не занимает место дважды, а отказ не изменяет состояние.
Реализуйте минимум два варианта и сравните coarse-grained lock (грубая
блокировка), атомарные операции и concurrent collections.

Реализовано три варианта.
1) LockCourse. Грубая блокировка: весь enroll под synchronized. Просто и
   надёжно, любая проверка и запись выполняются как одно целое. Минус: все потоки
   выстраиваются в очередь, даже когда записываются разные студенты.
2) AtomicCourse. Атомарные операции: состояние это неизменяемое множество в
   AtomicReference. enroll делает копию с новым студентом и пробует заменить её
   через compareAndSet, при неудаче повторяет. Отказ ничего не меняет, потому что
   ссылка не трогается. Нет блокировок, но каждая запись копирует множество и
   при большой конкуренции потоки повторяют попытки.
3) ConcurrentCourse. Concurrent collections: ConcurrentHashMap.newKeySet() и
   AtomicInteger свободных мест. Сначала add() отсеивает дубликаты, затем
   занимается место. Если мест нет, студент убирается обратно. Быстрее всего
   при разных студентах, но между add и remove студент на короткое время виден
   в множестве, хотя отказ уже решён. Итоговое состояние верное, а промежуточное
   нет, поэтому такой вариант самый тонкий.
*/
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Task6 {

    interface Course {
        boolean enroll(int studentId);
        int count();
    }

    static class LockCourse implements Course {
        private final int capacity;
        private final Set<Integer> students = new HashSet<>();

        LockCourse(int capacity) {
            this.capacity = capacity;
        }

        public synchronized boolean enroll(int studentId) {
            if (students.size() >= capacity) {
                return false;
            }
            return students.add(studentId);
        }

        public synchronized int count() {
            return students.size();
        }
    }

    static class AtomicCourse implements Course {
        private final int capacity;
        private final AtomicReference<Set<Integer>> students = new AtomicReference<>(Set.of());

        AtomicCourse(int capacity) {
            this.capacity = capacity;
        }

        public boolean enroll(int studentId) {
            while (true) {
                Set<Integer> current = students.get();
                if (current.size() >= capacity || current.contains(studentId)) {
                    return false;
                }
                Set<Integer> updated = new HashSet<>(current);
                updated.add(studentId);
                if (students.compareAndSet(current, Set.copyOf(updated))) {
                    return true;
                }
            }
        }

        public int count() {
            return students.get().size();
        }
    }

    static class ConcurrentCourse implements Course {
        private final Set<Integer> students = ConcurrentHashMap.newKeySet();
        private final AtomicInteger freePlaces;

        ConcurrentCourse(int capacity) {
            this.freePlaces = new AtomicInteger(capacity);
        }

        public boolean enroll(int studentId) {
            if (!students.add(studentId)) {
                return false;
            }
            if (takePlace()) {
                return true;
            }
            students.remove(studentId);
            return false;
        }

        private boolean takePlace() {
            while (true) {
                int free = freePlaces.get();
                if (free == 0) {
                    return false;
                }
                if (freePlaces.compareAndSet(free, free - 1)) {
                    return true;
                }
            }
        }

        public int count() {
            return students.size();
        }
    }

    static void check(String name, Course course, int capacity) throws InterruptedException {
        int threadsCount = 8;
        int studentsCount = 100;
        AtomicInteger success = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threadsCount);

        long begin = 0;
        try {
            for (int i = 0; i < threadsCount; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        return;
                    }
                    for (int id = 0; id < studentsCount; id++) {
                        if (course.enroll(id)) {
                            success.incrementAndGet();
                        }
                    }
                });
            }
            begin = System.nanoTime();
            start.countDown();
            pool.shutdown();
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } finally {
            pool.shutdownNow();
        }
        long time = (System.nanoTime() - begin) / 1000000;

        boolean good = success.get() == capacity && course.count() == capacity;
        System.out.println(name + ": " + success.get() + "/" + capacity + ", " + time + " мс, " + (good ? "ok" : "ОШИБКА"));
    }

    public static void main(String[] args) throws InterruptedException {
        int capacity = 30;
        check("Lock", new LockCourse(capacity), capacity);
        check("Atomic", new AtomicCourse(capacity), capacity);
        check("Concurrent", new ConcurrentCourse(capacity), capacity);
    }
}
