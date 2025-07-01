package ca.bazlur.modern.concurrency.c05;

public class MutableLoggingContext {
    // A ThreadLocal holding the current log level
    private static final ThreadLocal<String> LOG_LEVEL = new ThreadLocal<>(); //①

    public static void setLogLevel(String level) {
        LOG_LEVEL.set(level); //②
    }

    public static String getLogLevel() {
        return LOG_LEVEL.get();
    }

    public static void log(String message) {
        System.out.println("[" + getLogLevel() + "] " + message);
    }

    public static void main(String[] args) throws InterruptedException {
        setLogLevel("INFO");
        log("Starting process..."); //③

        Thread thread = new Thread(() -> {
            setLogLevel("DEBUG"); //④
            log("Thread-specific debug mode enabled");
        });
        thread.start();

        Thread.sleep(100); // Give the other thread time to run
        log("Main thread still at INFO level"); //⑤
    }
}
