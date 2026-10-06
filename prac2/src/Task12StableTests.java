package coursehub;


public final class Task12StableTests {
    public static String rules() {
        return "Не зависим от порядка; каждый тест создаёт свои данные; временный каталог задаёт сам тест; toString проверяем по стабильному полю, а не целой строке.";
    }
}
