package coursehub;

import java.util.List;


public final class Task02CheckedUnchecked {
    record Decision(String situation, String choice, String why) {}

    public static List<Decision> decisions() {
        return List.of(
            new Decision("Неверный аргумент метода", "unchecked: IllegalArgumentException", "Ошибка вызывающего кода."),
            new Decision("Файл не найден при обязательном импорте", "checked: IOException", "Ожидаемый внешний отказ, его можно обработать."),
            new Decision("Нарушение состояния объекта", "unchecked: IllegalStateException", "Команда недопустима в текущем состоянии."),
            new Decision("Сетевой отказ", "checked: IOException или Result", "Инфраструктурный отказ можно повторить/показать пользователю."),
            new Decision("Ошибка конфигурации при старте", "другой результат: fail-fast", "Приложение не может корректно стартовать."),
            new Decision("Недостаток памяти", "невосстановимо/не ловить", "OutOfMemoryError — ошибка JVM, не обычный бизнес-отказ."),
            new Decision("Дубликат заявки", "другой результат/Result", "Ожидаемый бизнес-исход, а не авария."),
            new Decision("Курс не найден", "Optional", "Отсутствие — нормальный результат поиска."),
            new Decision("Некорректный формат записи импорта", "checked: CourseImportException", "Есть контекст записи и импорт можно продолжить/отклонить."),
            new Decision("Ошибка программирования: null там, где его быть не должно", "unchecked: NullPointerException/bug", "Исправляется кодом, а не обработкой в бизнес-слое.")
        );
    }

    public static void main(String[] args) {
        decisions().forEach(d -> System.out.println(d.situation() + " -> " + d.choice() + " — " + d.why()));
    }
}
