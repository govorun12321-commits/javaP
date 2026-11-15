/*
Задание 3. Потокобезопасный реестр.
Реализуйте реестр активных сессий на ConcurrentHashMap. Операция «создать,
только если отсутствует» должна быть атомарной.

Объяснение.
Связка containsKey + put не атомарна: два потока могут одновременно увидеть,
что ключа нет, и оба создадут сессию. Поэтому используются атомарные методы
ConcurrentHashMap: putIfAbsent и computeIfAbsent. Для одного ключа они
выполняются как единая операция.
*/
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Task3 {

    static class Session {
        private static final AtomicInteger nextId = new AtomicInteger(1);
        final int id;
        final String user;

        Session(String user) {
            this.id = nextId.getAndIncrement();
            this.user = user;
        }
    }

    static class SessionRegistry {
        private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
        private final AtomicInteger createdCount = new AtomicInteger();

        boolean createIfAbsent(String user) {
            return sessions.putIfAbsent(user, new Session(user)) == null;
        }

        Session getOrCreate(String user) {
            return sessions.computeIfAbsent(user, name -> {
                createdCount.incrementAndGet();
                return new Session(name);
            });
        }

        void close(String user) {
            sessions.remove(user);
        }

        int size() {
            return sessions.size();
        }

        int created() {
            return createdCount.get();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SessionRegistry registry = new SessionRegistry();
        AtomicInteger successCount = new AtomicInteger();
        Session[] found = new Session[10];

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            int index = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    if (registry.createIfAbsent("anna")) {
                        successCount.incrementAndGet();
                    }
                }
                found[index] = registry.getOrCreate("boris");
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        boolean sameSession = true;
        for (Session session : found) {
            if (session != found[0]) {
                sameSession = false;
            }
        }

        System.out.println("createIfAbsent успешно: " + successCount.get());
        System.out.println("getOrCreate создал: " + registry.created());
        System.out.println("Одна сессия у всех: " + sameSession);
        System.out.println("Всего сессий: " + registry.size());
    }
}
