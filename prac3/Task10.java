/*
 * Задание 10. Конфликт default-методов
 *
 * Создайте два интерфейса с одинаковым default-методом. Получите ошибку компиляции, затем
 * разрешите конфликт явным override. Объясните, почему множественное наследование поведения
 * требует явного выбора.
 *
 * Ошибка компиляции (если убрать override из CourseReport):
 *   class CourseReport implements JsonExportable, CsvExportable { }
 *   -> error: types JsonExportable and CsvExportable are incompatible;
 *      class CourseReport inherits unrelated defaults for export() from types JsonExportable and CsvExportable
 *
 * Пояснение:
 * Если класс унаследовал две реализации одной сигнатуры, у компилятора нет правила,
 * какая "главнее" - угадывание привело бы к тихим ошибкам при добавлении default-метода
 * в интерфейс. Поэтому язык требует явного решения: переопределить метод и либо написать
 * свою логику, либо выбрать/скомбинировать через Интерфейс.super.метод().
 */
public class Task10 {

    interface JsonExportable {
        default String export(Course c) { return "{\"title\":\"" + c.title() + "\"}"; }
    }

    interface CsvExportable {
        default String export(Course c) { return c.id() + "," + c.title(); }
    }

    static class CourseReport implements JsonExportable, CsvExportable {
        @Override
        public String export(Course c) {                       
            return JsonExportable.super.export(c) + " | " + CsvExportable.super.export(c);
        }
    }

    public static void main(String[] args) {
        Course c = new Course(3, "Kotlin", 25);
        System.out.println(new CourseReport().export(c));
    }
}
