/*
 * Задание 11.
 * Для шести сценариев — каталог по ID, отсортированный leaderboard,
 * очередь, уникальные теги с порядком, диапазонный поиск,
 * частый доступ по индексу — выберите интерфейс и реализацию.
 * Укажите сложность главных операций и цену памяти/порядка.
 *
 * Пояснения:
 * Сценарий -> реализация (главные операции; цена):
 *
 * 1. каталог по ID -> Map/HashMap: get/put O(1);
 *    порядка нет, нужен equals/hashCode.
 *
 * 2. leaderboard -> NavigableSet/TreeSet с Comparator:
 *    add/remove/first O(log n); память на узлы,
 *    обновление очков = remove + add.
 *
 * 3. очередь -> Queue/ArrayDeque: offer/poll/peek O(1);
 *    кольцевой массив, без null.
 *
 * 4. теги с порядком -> Set/LinkedHashSet: O(1);
 *    доп. ссылки на каждую запись.
 *
 * 5. диапазонный поиск -> NavigableMap/TreeMap:
 *    subMap/floorKey O(log n + k); ключи сравнимы.
 *
 * 6. доступ по индексу -> List/ArrayList:
 *    get O(1), вставка в середину O(n); запас ёмкости.
 */

import java.util.*;

public class task_11 {

    record Course(String id, String title) {}

    record Score(String player, int points) {}

    public static void main(String[] args) {
        Map<String, Course> catalog = new HashMap<>();
        catalog.put("c1", new Course("c1", "Java"));
        System.out.println("1. Каталог: " + catalog.get("c1"));

        Comparator<Score> byRank = Comparator
                .comparingInt(Score::points)
                .reversed()
                .thenComparing(Score::player);

        NavigableSet<Score> board = new TreeSet<>(byRank);
        board.add(new Score("Anna", 90));
        board.add(new Score("Boris", 120));
        board.add(new Score("Clara", 90));

        System.out.println(
                "2. Лидеры: " + board + ", лучший = " + board.first()
        );

        board.remove(new Score("Anna", 90));
        board.add(new Score("Anna", 150));

        System.out.println("   После обновления Anna: первый = " + board.first());

        Queue<String> queue = new ArrayDeque<>();
        queue.offer("t1");
        queue.offer("t2");
        System.out.println("3. Очередь: poll = " + queue.poll());

        Set<String> tags = new LinkedHashSet<>(
                List.of("java", "sql", "java", "oop")
        );
        System.out.println("4. Теги: " + tags);

        NavigableMap<Integer, String> priceList = new TreeMap<>();
        priceList.put(100, "Basic");
        priceList.put(250, "Pro");
        priceList.put(400, "Max");
        priceList.put(900, "Elite");

        System.out.println(
                "5. Цены в [200, 500]: "
                        + priceList.subMap(200, true, 500, true)
                        + ", ближайшая снизу к 300 = "
                        + priceList.floorKey(300)
        );

        List<String> items = new ArrayList<>(List.of("a", "b", "c", "d"));
        System.out.println("6. get(2) = " + items.get(2));
    }
}