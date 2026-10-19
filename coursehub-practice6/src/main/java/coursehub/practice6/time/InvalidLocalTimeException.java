package coursehub.practice6.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class InvalidLocalTimeException extends IllegalArgumentException {

    private final LocalDateTime localTime;
    private final ZoneId zone;

    public InvalidLocalTimeException(LocalDateTime localTime, ZoneId zone) {
        super("Local time " + localTime + " does not exist in zone " + zone + " because of a daylight saving gap");
        this.localTime = localTime;
        this.zone = zone;
    }

    public LocalDateTime localTime() {
        return localTime;
    }

    public ZoneId zone() {
        return zone;
    }
}
