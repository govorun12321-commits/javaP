/*
 * Задание 3. Полиморфный массив
 *
 * Сохраните несколько CourseFormatter в массиве и примените каждый к одному курсу без if
 * по типу. Добавьте третью реализацию, не меняя обход. Критерий: клиент зависит только
 * от интерфейса.
 *
 * Пояснение:
 * Цикл for знает только CourseFormatter. Третья реализация (JsonCourseFormatter) добавляется
 * лишь в инициализацию массива - код обхода (printAll) не меняется (Open/Closed).
 * Никаких instanceof и if по типу нет: выбор поведения делает dynamic dispatch.
 */
public class Task03 {

    interface CourseFormatter {
        String format(Course course);
    }

    static class CompactCourseFormatter implements CourseFormatter {
        @Override public String format(Course c) { return c.title() + " " + c.completedHours() + "/" + c.durationHours(); }
    }

    static class DetailedCourseFormatter implements CourseFormatter {
        @Override public String format(Course c) {
            return "Курс #" + c.id() + " «" + c.title() + "», осталось " + c.remainingHours() + " ч";
        }
    }


    static class JsonCourseFormatter implements CourseFormatter {
        @Override public String format(Course c) {
            return "{\"id\":" + c.id() + ",\"title\":\"" + c.title() + "\",\"done\":" + c.isCompleted() + "}";
        }
    }

    static void printAll(CourseFormatter[] formatters, Course course) {
        for (CourseFormatter f : formatters) {
            System.out.println(f.format(course));
        }
    }

    public static void main(String[] args) {
        Course course = new Course(10, "Java Fundamentals", 32);
        course.completeHours(6);

        CourseFormatter[] formatters = {
                new CompactCourseFormatter(),
                new DetailedCourseFormatter(),
                new JsonCourseFormatter()      
        };
        printAll(formatters, course);
    }
}
