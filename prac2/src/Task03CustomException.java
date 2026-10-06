package coursehub;

public final class Task03CustomException {
    public static void main(String[] args) {
        try {
            try { throw new IllegalStateException("database unavailable"); }
            catch (IllegalStateException e) {
                throw new EnrollmentRejectedException(10, 20, EnrollmentRejectedException.NO_SEATS, e);
            }
        } catch (EnrollmentRejectedException e) {
            System.out.println(e);
            System.out.println("cause type = " + e.getCause().getClass().getSimpleName());
        }
    }
}
