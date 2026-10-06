package coursehub;

public final class Task13DecisionTable {
    public enum Status { OPEN, CLOSED }
    public enum Result { ACCEPT, REJECT_CLOSED, REJECT_NO_SEATS, REJECT_PREREQUISITE, REJECT_DUPLICATE }

    public static Result decide(Status status, boolean hasSeats, boolean prerequisite, boolean repeated) {
        if (status == Status.CLOSED) return Result.REJECT_CLOSED;
        if (!hasSeats) return Result.REJECT_NO_SEATS;
        if (!prerequisite) return Result.REJECT_PREREQUISITE;
        if (repeated) return Result.REJECT_DUPLICATE;
        return Result.ACCEPT;
    }
}
