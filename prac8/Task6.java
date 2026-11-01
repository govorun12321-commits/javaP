/*
Задание 6. Собственный collector

Создайте статистику count/sum/min/max без mutable static state.
Combiner корректен для разбиения, finisher возвращает immutable result.
Сравните с готовыми summarizing collectors.

Объяснение: изменяемое состояние живёт только в объекте Box,
который создаётся заново для каждого прохода (supplier), статических полей нет.
Combiner складывает count и sum и выбирает min и max из двух частей,
поэтому разбиение потока на части даёт тот же результат, что и целый проход.
Finisher превращает Box в record Stats, который изменить нельзя.
Готовый summarizingInt делает то же самое, но возвращает изменяемый
IntSummaryStatistics; свой collector нужен, если нужен свой тип результата.
*/

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Task6 {

    record Stats(long count, long sum, int min, int max) {
    }

    static class Box {
        long count = 0;
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        void add(int value) {
            count++;
            sum += value;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        Box merge(Box other) {
            count += other.count;
            sum += other.sum;
            min = Math.min(min, other.min);
            max = Math.max(max, other.max);
            return this;
        }

        Stats toStats() {
            if (count == 0) {
                return new Stats(0, 0, 0, 0);
            }
            return new Stats(count, sum, min, max);
        }
    }

    static Collector<Integer, Box, Stats> statsCollector() {
        return Collector.of(Box::new, Box::add, Box::merge, Box::toStats);
    }

    public static void main(String[] args) {
        List<Integer> durations = List.of(40, 30, 25, 60, 10, 50, 35);

        Stats whole = durations.stream().collect(statsCollector());

        Collector<Integer, Box, Stats> collector = statsCollector();
        Box first = collector.supplier().get();
        Box second = collector.supplier().get();
        for (int i = 0; i < durations.size(); i++) {
            if (i < 3) {
                collector.accumulator().accept(first, durations.get(i));
            } else {
                collector.accumulator().accept(second, durations.get(i));
            }
        }
        Stats glued = collector.finisher().apply(collector.combiner().apply(first, second));

        var ready = durations.stream().collect(Collectors.summarizingInt(Integer::intValue));

        System.out.println("Свой collector: " + whole);
        System.out.println("Две части:      " + glued);
        System.out.println("Совпадают: " + whole.equals(glued));
        System.out.println("summarizingInt: count=" + ready.getCount() + ", sum=" + ready.getSum()
                + ", min=" + ready.getMin() + ", max=" + ready.getMax());
    }
}
