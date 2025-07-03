package ca.bazlur.modern.concurrency.c04;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static void log(String message) {
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.isVirtual()
                ? "VThread[#" + currentThread.threadId() + "]"
                : currentThread.getName();
        String currentTime = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.printf("%s %-12s: %s%n", currentTime, threadName, message);
    }
}
