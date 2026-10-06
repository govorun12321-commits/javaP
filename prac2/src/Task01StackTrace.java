package coursehub;


public final class Task01StackTrace {
    static void level1() { level2(); }
    static void level2() { level3(); }
    static void level3() { level4(); }
    static void level4() { new Course(1, null, 10); } 

    public static void main(String[] args) {
        try { level1(); }
        catch (NullPointerException e) {
            System.out.println("Тип: " + e.getClass().getSimpleName());
            System.out.println("Сообщение: " + e.getMessage());
            System.out.println("Место: Course constructor / title validation");
            System.out.println("Путь: main -> level1 -> level2 -> level3 -> level4 -> Course");
            System.out.println("Исправление: проверять null до вызова методов String.");
        }
    }
}
