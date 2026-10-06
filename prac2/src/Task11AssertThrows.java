package coursehub;


public final class Task11AssertThrows {
    public static void failWithoutMutation(Course course, int hours) {
        course.completeHours(hours); // Course валидирует до изменения state
    }
}
