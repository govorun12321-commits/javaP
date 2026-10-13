/*
 * Задание 1. Один контракт, две реализации
 *
 * Объявите CourseFormatter с методом format(Course). Реализуйте CompactCourseFormatter
 * и DetailedCourseFormatter. Вызовите обе реализации через переменную интерфейсного типа.
 * Объясните, что известно компилятору и что выбирается во время выполнения.
 *
 * Пояснение:
 * Компилятор знает только compile-time тип переменной (CourseFormatter): он проверяет,
 * что у интерфейса есть метод format(Course) с подходящей сигнатурой, и генерирует вызов
 * через интерфейс. Какая именно реализация выполнится, JVM выбирает во время выполнения
 * по фактическому классу объекта (dynamic dispatch). Одна и та же строка f.format(course)
 * даёт разный результат в зависимости от объекта в переменной.
 */
public class Task01 {

    interface CourseFormatter {
        String format(Course course);
    }

    static class CompactCourseFormatter implements CourseFormatter {
        @Override
        public String format(Course c) {
            return c.title() + " " + c.completedHours() + "/" + c.durationHours();
        }
    }

    static class DetailedCourseFormatter implements CourseFormatter {
        @Override
        public String format(Course c) {
            return "Курс #" + c.id() + " «" + c.title() + "»: длительность " + c.durationHours()
                    + " ч, пройдено " + c.completedHours() + " ч, осталось " + c.remainingHours() + " ч";
        }
    }

    public static void main(String[] args) {
        Course course = new Course(10, "Java Fundamentals", 32);
        course.completeHours(6);

        CourseFormatter f = new CompactCourseFormatter();   
        System.out.println(f.format(course));               

        f = new DetailedCourseFormatter();                  
        System.out.println(f.format(course));
    }
}
