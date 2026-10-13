/*
 * Задание 14. Наследование взрывает комбинации
 *
 * Есть форматы курса Online, Classroom, Hybrid и политики оплаты Free, Fixed, Subscription.
 * Откажитесь от девяти подклассов, разделив независимые оси на компоненты. Продемонстрируйте
 * добавление нового формата и новой оплаты с минимальным изменением существующего кода.
 *
 * Пояснение:
 * Наследованием 3 x 3 = 9 классов (OnlineFreeCourse, OnlineFixedCourse, ...), а новый формат
 * добавил бы ещё 3. Две оси независимы, поэтому каждая становится отдельной абстракцией
 * (CourseFormat, PaymentPolicy), а CourseOffering их компонует: 3 + 3 = 6 классов, а после
 * добавления Webinar и Donation - 8 вместо 16. Существующий код не изменяется: новые
 * классы только добавляются.
 */
public class Task14 {

    interface CourseFormat {
        String description();
    }

    interface PaymentPolicy {
        int price(Course course);          
        String description();
    }

    // Ось 1: формат
    static class Online implements CourseFormat { @Override public String description() { return "онлайн"; } }
    static class Classroom implements CourseFormat { @Override public String description() { return "очно в аудитории"; } }
    static class Hybrid implements CourseFormat { @Override public String description() { return "гибрид"; } }

    // Ось 2: оплата
    static class Free implements PaymentPolicy {
        @Override public int price(Course c) { return 0; }
        @Override public String description() { return "бесплатно"; }
    }
    static class Fixed implements PaymentPolicy {
        private final int amount;
        Fixed(int amount) { this.amount = amount; }
        @Override public int price(Course c) { return amount; }
        @Override public String description() { return "фиксированная цена"; }
    }
    static class Subscription implements PaymentPolicy {
        private final int perMonth, months;
        Subscription(int perMonth, int months) { this.perMonth = perMonth; this.months = months; }
        @Override public int price(Course c) { return perMonth * months; }
        @Override public String description() { return "подписка " + perMonth + "/мес."; }
    }

    // Композиция осей
    static class CourseOffering {
        private final Course course;
        private final CourseFormat format;
        private final PaymentPolicy payment;
        CourseOffering(Course course, CourseFormat format, PaymentPolicy payment) {
            this.course = course; this.format = format; this.payment = payment;
        }
        String describe() {
            return course.title() + ": " + format.description() + ", " + payment.description() + ", итого " + payment.price(course) + " EUR";
        }
    }


    static class Webinar implements CourseFormat { @Override public String description() { return "вебинар"; } }
    static class Donation implements PaymentPolicy {
        @Override public int price(Course c) { return 5; }
        @Override public String description() { return "пожертвование"; }
    }

    public static void main(String[] args) {
        Course java = new Course(1, "Java", 40);
        System.out.println(new CourseOffering(java, new Online(), new Free()).describe());
        System.out.println(new CourseOffering(java, new Classroom(), new Fixed(300)).describe());
        System.out.println(new CourseOffering(java, new Hybrid(), new Subscription(40, 3)).describe());
        System.out.println(new CourseOffering(java, new Webinar(), new Donation()).describe());
    }
}
