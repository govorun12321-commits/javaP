/*
 * Задание 14.
 * Реализуйте generic `LruCache<K,V>` фиксированной ёмкости
 * на стандартных коллекциях.
 * `get` обновляет недавность, превышение удаляет старейший элемент,
 * `null`-политика явна.
 * Докажите сценариями порядок вытеснения и стабильность ключей.
 *
 * Пояснения:
 * - LinkedHashMap с accessOrder=true: O(1),
 *   старейший элемент - первый в keySet.
 *   Композиция, а не наследование.
 * - null-политика: null-ключи и значения запрещены (NPE),
 *   get возвращает Optional.
 *   Ключи должны быть неизменяемыми (сценарий 6).
 */

import java.util.*;

public class task_14 {

    static void check(boolean condition, String what) {
        if (!condition) {
            throw new AssertionError("ПРОВАЛ: " + what);
        }
        System.out.println("  OK: " + what);
    }

    static class MutableKey {
        int n;

        MutableKey(int n) {
            this.n = n;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof MutableKey k && k.n == n;
        }

        @Override
        public int hashCode() {
            return n;
        }
    }

    record StableKey(int n) {}

    public static void main(String[] args) {

        System.out.println("Сценарий 1: порядок вытеснения без обращений");

        LruCache<String, Integer> c1 = new LruCache<>(3);
        c1.put("a", 1);
        c1.put("b", 2);
        c1.put("c", 3);
        c1.put("d", 4);

        check(
                c1.get("a").isEmpty(),
                "a вытеснен как самый старый"
        );
        check(
                c1.keysOldestFirst().equals(List.of("b", "c", "d")),
                "порядок " + c1.keysOldestFirst()
        );

        System.out.println("Сценарий 2: get обновляет недавность");

        LruCache<String, Integer> c2 = new LruCache<>(3);
        c2.put("a", 1);
        c2.put("b", 2);
        c2.put("c", 3);

        check(
                c2.get("a").equals(Optional.of(1)),
                "get(a) вернул 1"
        );

        c2.put("d", 4);

        check(
                c2.get("b").isEmpty(),
                "вытеснен b, а не a"
        );
        check(
                c2.keysOldestFirst().equals(List.of("c", "a", "d")),
                "порядок " + c2.keysOldestFirst()
        );

        System.out.println(
                "Сценарий 3: put существующего ключа "
                        + "обновляет значение и недавность, размер не растёт"
        );

        LruCache<String, Integer> c3 = new LruCache<>(2);
        c3.put("a", 1);
        c3.put("b", 2);
        c3.put("a", 10);
        c3.put("c", 3);

        check(c3.size() == 2, "размер = 2");
        check(
                c3.get("b").isEmpty() && c3.get("a").equals(Optional.of(10)),
                "b вытеснен, a=10 жив"
        );

        System.out.println("Сценарий 4: промах не меняет состояние");

        LruCache<String, Integer> c4 = new LruCache<>(2);
        c4.put("a", 1);
        c4.put("b", 2);
        c4.get("zzz");

        check(
                c4.keysOldestFirst().equals(List.of("a", "b")),
                "порядок не изменился"
        );

        System.out.println("Сценарий 5: политика null и граничные значения");

        try {
            c4.put(null, 1);
            check(false, "null-ключ");
        } catch (NullPointerException e) {
            check(true, "null-ключ -> NPE");
        }

        try {
            c4.put("x", null);
            check(false, "null-значение");
        } catch (NullPointerException e) {
            check(true, "null-значение -> NPE");
        }

        try {
            c4.get(null);
            check(false, "get(null)");
        } catch (NullPointerException e) {
            check(true, "get(null) -> NPE");
        }

        try {
            new LruCache<String, String>(0);
            check(false, "capacity 0");
        } catch (IllegalArgumentException e) {
            check(true, "capacity=0 -> IAE");
        }

        LruCache<String, Integer> one = new LruCache<>(1);
        one.put("a", 1);
        one.put("b", 2);

        check(
                one.keysOldestFirst().equals(List.of("b")),
                "ёмкость 1: остаётся только последний"
        );

        System.out.println("Сценарий 6: стабильность ключей");

        LruCache<MutableKey, String> bad = new LruCache<>(2);
        MutableKey mutableKey = new MutableKey(1);
        bad.put(mutableKey, "v");
        mutableKey.n = 2;

        check(
                bad.get(mutableKey).isEmpty()
                        && bad.get(new MutableKey(1)).isEmpty(),
                "изменяемый ключ потерян: не находится ни по новому, "
                        + "ни по старому значению"
        );
        check(
                bad.size() == 1,
                "запись осталась в кэше «призраком» и занимает место"
        );

        LruCache<StableKey, String> good = new LruCache<>(2);
        good.put(new StableKey(1), "v");

        check(
                good.get(new StableKey(1)).equals(Optional.of("v")),
                "неизменяемый record-ключ находится по значению"
        );
    }
}

final class LruCache<K, V> {

    private final int capacity;
    private final LinkedHashMap<K, V> map =
            new LinkedHashMap<>(16, 0.75f, true);

    LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "capacity должна быть > 0: " + capacity
            );
        }

        this.capacity = capacity;
    }

    Optional<V> get(K key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(map.get(key));
    }

    void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        map.put(key, value);

        if (map.size() > capacity) {
            Iterator<K> eldest = map.keySet().iterator();
            eldest.next();
            eldest.remove();
        }
    }

    int size() {
        return map.size();
    }

    List<K> keysOldestFirst() {
        return List.copyOf(map.keySet());
    }
}