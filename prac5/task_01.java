/*
 * Задание 1.
 * Сохраните последовательность регистраций, вставьте и удалите элемент, найдите дубликаты. Сравните `ArrayList` и `LinkedList` для чтения по индексу и обхода; выбор подтвердите измерением без заявления о микробенчмарке.
 *
 * Пояснения:
 * Выбор: ArrayList по умолчанию. get(i): ArrayList O(1), LinkedList O(n); обход O(n) у обоих, но ArrayList быстрее (локальность данных, нет узлов).
 * Вставка в середину: ArrayList O(n) (сдвиг массива), LinkedList O(1) лишь при готовой позиции итератора, поиск позиции O(n).
 * Дубликаты через HashSet: O(n) вместо O(n^2).
 * Измерение грубое (System.nanoTime, прогрев, минимум из 5 прогонов), это НЕ микробенчмарк (нет JMH); надёжен только порядок величин.
 */

import java.util.*;

public class task_01 {
    record Registration(String student, String courseCode) {}

    static <T> Set<T> findDuplicates(List<T> items) {
        Set<T> seen = new HashSet<>();
        Set<T> duplicates = new LinkedHashSet<>(); 
        for (T item : items) {
            if (!seen.add(item)) {      
                duplicates.add(item);
            }
        }
        return duplicates;
    }

    static long sink; 

    static long measureUs(Runnable task) {
        for (int i = 0; i < 2; i++) task.run();           
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            long t0 = System.nanoTime();
            task.run();
            best = Math.min(best, (System.nanoTime() - t0) / 1_000);
        }
        return best;
    }

    public static void main(String[] args) {
        List<Registration> regs = new ArrayList<>();
        regs.add(new Registration("Anna", "CS101"));
        regs.add(new Registration("Boris", "CS102"));
        regs.add(new Registration("Anna", "CS101"));   
        regs.add(new Registration("Clara", "CS103"));
        regs.add(new Registration("Boris", "CS102"));  
        System.out.println("Исходный порядок: " + regs);

        regs.add(1, new Registration("Dmitry", "CS104"));   
        System.out.println("После вставки в индекс 1: " + regs);
        regs.remove(0);                                     
        regs.remove(new Registration("Clara", "CS103"));    
        System.out.println("После двух удалений: " + regs);

        System.out.println("Дубликаты: " + findDuplicates(regs));

        final int N = 20_000;
        List<Integer> array = new ArrayList<>();
        List<Integer> linked = new LinkedList<>();
        for (int i = 0; i < N; i++) { array.add(i); linked.add(i); }

        long arrGet = measureUs(() -> { long s = 0; for (int i = 0; i < N; i++) s += array.get(i); sink += s; });
        long linGet = measureUs(() -> { long s = 0; for (int i = 0; i < N; i++) s += linked.get(i); sink += s; });

        final int M = 2_000_000;
        List<Integer> bigArr = new ArrayList<>();
        List<Integer> bigLin = new LinkedList<>();
        for (int i = 0; i < M; i++) { bigArr.add(i); bigLin.add(i); }
        long arrIter = measureUs(() -> { long s = 0; for (int x : bigArr) s += x; sink += s; });
        long linIter = measureUs(() -> { long s = 0; for (int x : bigLin) s += x; sink += s; });

        System.out.println("get(i) для " + N + " элементов: ArrayList=" + arrGet + " мкс, LinkedList=" + linGet + " мкс");
        System.out.println("for-each для " + M + " элементов: ArrayList=" + arrIter + " мкс, LinkedList=" + linIter + " мкс");
    }
}
