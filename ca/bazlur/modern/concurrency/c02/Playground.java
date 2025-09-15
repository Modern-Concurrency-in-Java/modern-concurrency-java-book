package ca.bazlur.modern.concurrency.c02;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Playground {
    public static void main(String[] args) {
        try (ScheduledExecutorService scheduledPool
                 = Executors.newScheduledThreadPool(2)) {
            scheduledPool.scheduleAtFixedRate(() -> {
                System.out.println(Thread.currentThread().getName()
                    + " is running a scheduled task");
            }, 0, 5, TimeUnit.SECONDS);
        }
    }
}
