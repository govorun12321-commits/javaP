/*
 * Задание 2. Checked или unchecked
 *
 * Распределите десять ситуаций: неверный аргумент метода, отсутствующий файл, нарушение
 * состояния, сетевой отказ, ошибка конфигурации, недостаток памяти. Для каждой выберите
 * checked exception, unchecked exception, другой результат или невозможность
 * восстановления и объясните решение.
 *
 * Краткое объяснение (правило выбора):
 *  - checked: вызывающий код реально может восстановиться и компилятор должен заставить об этом подумать
 *             (внешний мир: файлы, сеть);
 *  - unchecked: ошибка вызывающего кода или нарушенный инвариант — исправляется кодом, а не обработкой;
 *  - значение (Optional / result): отказ — обычная, ожидаемая часть логики;
 *  - Error: восстановиться в приложении нельзя, ловить не нужно.
 */

import java.util.List;

public final class Task02_CheckedOrUnchecked {

    enum Handling { CHECKED, UNCHECKED, RESULT_VALUE, UNRECOVERABLE }

    record Situation(String situation, Handling handling, String javaType, String why) { }

    static final List<Situation> TABLE = List.of(
        new Situation("1. Неверный аргумент (completeHours(-5))", Handling.UNCHECKED,
            "IllegalArgumentException",
            "Баг вызывающего кода: ловить нечего, нужно исправить вызов и проверить предусловие."),
        new Situation("2. Отсутствующий файл с импортом", Handling.CHECKED,
            "java.io.IOException / NoSuchFileException",
            "Внешняя среда; вызывающий может выбрать другой файл, значение по умолчанию или сообщить пользователю."),
        new Situation("3. Нарушение состояния (завершить уже завершённый курс)", Handling.UNCHECKED,
            "IllegalStateException",
            "Нарушен порядок использования объекта; это ошибка программы, а не среды."),
        new Situation("4. Сетевой отказ (таймаут, connection refused)", Handling.CHECKED,
            "IOException (на границе — оборачивается в свой InfrastructureException)",
            "Временный отказ: возможны retry, fallback, понятное сообщение. Компилятор должен напоминать об этом."),
        new Situation("5. Ошибка конфигурации при старте (нет обязательного параметра)", Handling.UNCHECKED,
            "IllegalStateException / свой ConfigurationException extends RuntimeException",
            "Fail-fast: без конфигурации работать нельзя, продолжать бессмысленно, обрабатывать на каждом уровне — шум."),
        new Situation("6. Недостаток памяти", Handling.UNRECOVERABLE,
            "OutOfMemoryError (Error)",
            "Состояние JVM ненадёжно; корректно — логировать и завершать процесс, а не ловить."),
        new Situation("7. Курс с таким id не найден в каталоге", Handling.RESULT_VALUE,
            "Optional<Course>",
            "Отсутствие — нормальный исход запроса; исключение для обычного случая дорого и шумно."),
        new Situation("8. Пользователь ввёл «abc» вместо числа часов", Handling.RESULT_VALUE,
            "типизированный ParseResult (Ok / Invalid(reason))",
            "Частая и ожидаемая ошибка ввода: вызывающему нужна причина для сообщения пользователю."),
        new Situation("9. null в обязательном параметре", Handling.UNCHECKED,
            "NullPointerException (Objects.requireNonNull)",
            "Программная ошибка; требуем немедленно и с понятным именем параметра."),
        new Situation("10. Бесконечная рекурсия", Handling.UNRECOVERABLE,
            "StackOverflowError (Error)",
            "Дефект алгоритма; восстановление невозможно — исправляется код.")
    );

    public static void main(String[] args) {
        for (Situation s : TABLE) {
            System.out.printf("%-62s | %-13s | %s%n    почему: %s%n%n",
                    s.situation(), s.handling(), s.javaType(), s.why());
        }
    }
}
