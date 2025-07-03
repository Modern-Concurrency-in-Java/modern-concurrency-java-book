package ca.bazlur.modern.concurrency.c03;

import jdk.internal.vm.Continuation;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import static ca.bazlur.modern.concurrency.c03.NanoThread.NANO_THREAD_SCHEDULER;
import static ca.bazlur.modern.concurrency.c03.NanoThread.SCOPE;
import static ca.bazlur.modern.concurrency.c03.NanoThreadScheduler.CURRENT_NANO_THREAD;
import static ca.bazlur.modern.concurrency.c03.NanoThreadScheduler.IO_EVENT_SCHEDULER;

public class FileOperation {

    private final Random random = new Random();

    public void transfer(String filePath) {
        System.out.println("Start transferring file: " + filePath);
        NanoThread nanoThread = NanoThread.currentVThread();
        IO_EVENT_SCHEDULER.schedule(() -> NANO_THREAD_SCHEDULER.schedule(nanoThread),
                random.nextInt(1000), TimeUnit.MILLISECONDS);
        CURRENT_NANO_THREAD.remove();
        Continuation.yield(SCOPE);
        System.out.println("Transfer completed for file: " + filePath);
    }
}
