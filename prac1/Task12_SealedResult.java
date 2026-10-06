package prac1;

sealed interface EnrollmentResult
        permits Accepted, Rejected, WaitListed {
}

record Accepted(long studentId, long courseId) implements EnrollmentResult {
}

record Rejected(String reason) implements EnrollmentResult {
}

record WaitListed(int position) implements EnrollmentResult {
}

public class Task12_SealedResult {
    static String describe(EnrollmentResult result) {
        return switch (result) {
            case Accepted a -> "Accepted student=" + a.studentId();
            case Rejected r -> "Rejected: " + r.reason();
            case WaitListed w -> "Wait-listed at position " + w.position();
        };
    }

    public static void main(String[] args) {
        System.out.println(describe(new Accepted(1, 10)));
        System.out.println(describe(new Rejected("Prerequisites")));
        System.out.println(describe(new WaitListed(3)));
    }
}
