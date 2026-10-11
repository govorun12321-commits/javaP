/*
 * Задание 5.
 * Воспроизведите `ConcurrentModificationException` при удалении из list
 * в enhanced for. Исправьте через iterator и через `removeIf`.
 * Объясните, почему копирование всей коллекции не всегда лучший default.
 *
 * Пояснения:
 * - Итератор запоминает modCount; list.remove мимо итератора меняет его
 *   -> ConcurrentModificationException.
 * - Если удалить предпоследний элемент, исключения нет,
 *   но последний элемент пропущен.
 * - Копия коллекции - не лучший default:
 *     * O(n) времени и памяти,
 *     * remove(Object) по копии даёт O(n^2),
 *     * снимок может устареть,
 *     * ошибка маскируется.
 * - removeIf - один проход.
 */

import java.util.*;

public class task_05 {

    static List<String> sample() {
        return new ArrayList<>(
                List.of("alpha", "beta", "bravo", "charlie", "delta")
        );
    }

    public static void main(String[] args) {
        // 1. Воспроизводим ConcurrentModificationException
        List<String> list = sample();

        try {
            for (String s : list) {
                if (s.startsWith("b")) {
                    list.remove(s);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Поймано: " + e.getClass().getSimpleName());
        }

        // 2. "Коварный" случай: удаление предпоследнего элемента
        List<String> tricky = new ArrayList<>(List.of("a", "b", "c"));

        for (String s : tricky) {
            System.out.println("  видим: " + s);

            if (s.equals("b")) {
                tricky.remove(s);
            }
        }

        System.out.println(
                "  'c' не был обработан, исключения нет. Остаток: " + tricky
        );

        // 3. Исправление через Iterator
        List<String> viaIterator = sample();

        for (Iterator<String> it = viaIterator.iterator(); it.hasNext(); ) {
            if (it.next().startsWith("b")) {
                it.remove();
            }
        }

        System.out.println("Iterator.remove: " + viaIterator);

        // 4. Исправление через removeIf
        List<String> viaRemoveIf = sample();
        boolean changed = viaRemoveIf.removeIf(s -> s.startsWith("b"));

        System.out.println(
                "removeIf: " + viaRemoveIf + ", изменилось = " + changed
        );

        // 5. Вариант с копией коллекции
        List<String> viaCopy = sample();

        for (String s : new ArrayList<>(viaCopy)) {
            if (s.startsWith("b")) {
                viaCopy.remove(s);
            }
        }

        System.out.println("Копия + remove: " + viaCopy);
    }
}