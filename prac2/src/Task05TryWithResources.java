package coursehub;

import java.util.ArrayList;
import java.util.List;


public final class Task05TryWithResources {
    static final class LoggingResource implements AutoCloseable {
        private final List<String> log;
        private final boolean failOnClose;
        LoggingResource(List<String> log, boolean failOnClose) {
            this.log = log; this.failOnClose = failOnClose; log.add("open");
        }
        void use() { log.add("use"); }
        @Override public void close() {
            log.add("close");
            if (failOnClose) throw new IllegalStateException("close failed");
        }
    }

    public static void main(String[] args) {
        List<String> ok = new ArrayList<>();
        try (LoggingResource r = new LoggingResource(ok, false)) { r.use(); }
        System.out.println("success: " + ok);

        List<String> both = new ArrayList<>();
        try (LoggingResource r = new LoggingResource(both, true)) {
            r.use();
            throw new IllegalArgumentException("use failed");
        } catch (Exception e) {
            System.out.println("main: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) System.out.println("suppressed: " + s.getMessage());
        }
        System.out.println("both: " + both);
    }
}
