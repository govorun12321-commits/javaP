package coursehub;


public final class Task09Boundaries {
    public static boolean accepts(int hours) {
        try { new Course(1, "Course", hours); return true; }
        catch (IllegalArgumentException e) { return false; }
    }
}
