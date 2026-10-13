/*
 * Задание 12. Sealed-иерархия результата
 *
 * Создайте sealed interface EnrollmentResult с Accepted, Rejected и WaitListed. Обработайте
 * все варианты через exhaustive switch без default. Добавьте новый permitted-тип и
 * зафиксируйте, где компилятор потребовал изменение.
 *
 * Пояснение:
 * sealed ограничивает набор подтипов, поэтому компилятор знает все варианты и проверяет
 * switch на полноту без default. Добавлен четвёртый тип Expired (запись истекла):
 *   1) его пришлось добавить в список permits (иначе сам Expired не компилируется);
 *   2) сразу после этого компилятор выдал ошибку в describe():
 *      "the switch expression does not cover all possible input values" -
 *      пришлось добавить ветку case Expired.
 * Если бы был default, ошибки не было бы и новый вариант молча попал бы в общую ветку.
 */
public class Task12 {

    sealed interface EnrollmentResult permits Accepted, Rejected, WaitListed, Expired { }

    record Accepted(String student) implements EnrollmentResult { }
    record Rejected(String student, String reason) implements EnrollmentResult { }
    record WaitListed(String student, int position) implements EnrollmentResult { }
    record Expired(String student) implements EnrollmentResult { }   

    static String describe(EnrollmentResult result) {
        return switch (result) {
            case Accepted a -> a.student() + ": принят";
            case Rejected r -> r.student() + ": отказ (" + r.reason() + ")";
            case WaitListed w -> w.student() + ": в листе ожидания, позиция " + w.position();
            case Expired e -> e.student() + ": заявка истекла";    
        };
    }

    public static void main(String[] args) {
        EnrollmentResult[] results = {
                new Accepted("Аня"), new Rejected("Борис", "возраст"),
                new WaitListed("Вера", 3), new Expired("Глеб")
        };
        for (EnrollmentResult r : results) System.out.println(describe(r));
    }
}
