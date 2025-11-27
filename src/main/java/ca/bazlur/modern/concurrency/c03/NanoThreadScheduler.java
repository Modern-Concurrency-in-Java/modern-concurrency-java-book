package ca.bazlur.modern.concurrency.c03;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NanoThreadScheduler {

    public static final ThreadLocal<NanoThread> CURRENT_NANO_THREAD = new ThreadLocal<>();
    public static final ScheduledExecutorService IO_EVENT_SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService workStealingPool = Executors.newWorkStealingPool(2);

    public void schedule(NanoThread nanoThread) {
        workStealingPool.submit(() -> {
            CURRENT_NANO_THREAD.set(nanoThread);
            nanoThread.run();
            CURRENT_NANO_THREAD.remove();
        });
    }
}
